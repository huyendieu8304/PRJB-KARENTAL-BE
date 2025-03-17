package com.mp.karental.service;

import com.mp.karental.constant.EBookingStatus;
import com.mp.karental.constant.EPaymentType;
import com.mp.karental.constant.ERole;
import com.mp.karental.dto.request.booking.BookingRequest;
import com.mp.karental.dto.request.booking.EditBookingRequest;
import com.mp.karental.dto.response.ApiResponse;
import com.mp.karental.dto.response.booking.BookingResponse;
import com.mp.karental.dto.response.booking.BookingThumbnailResponse;
import com.mp.karental.dto.response.booking.BookingListResponse;
import com.mp.karental.dto.response.booking.WalletResponse;
import com.mp.karental.entity.*;
import com.mp.karental.exception.AppException;
import com.mp.karental.exception.ErrorCode;
import com.mp.karental.mapper.BookingMapper;
import com.mp.karental.repository.BookingRepository;
import com.mp.karental.repository.CarRepository;
import com.mp.karental.repository.WalletRepository;
import com.mp.karental.security.SecurityUtil;
import com.mp.karental.util.RedisUtil;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.apache.xmlbeans.impl.xb.ltgfmt.FileDesc;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * Service class for handling booking operations.
 *
 * @author QuangPM20
 * @version 1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BookingService {
    BookingRepository bookingRepository;
    CarRepository carRepository;
    BookingMapper bookingMapper;
    RedisUtil redisUtil;
    WalletRepository walletRepository;
    FileService fileService;
    CarService carService;
    TransactionService transactionService;

    // Define constant field names to avoid repetition
    private static final String FIELD_UPDATED_AT = "updatedAt";
    private static final String FIELD_BASE_PRICE = "basePrice";

    /**
     * Creates a new booking for a car rental.
     *
     * @param bookingRequest The booking request details.
     * @return BookingResponse containing booking details.
     * @throws AppException if there are validation issues or car availability problems.
     */
    public BookingResponse createBooking(BookingRequest bookingRequest) throws AppException {
        // Update status bookings before creating a new one.
        updateStatusBookings();

        // Get the current logged-in user's account ID.
        String accountId = SecurityUtil.getCurrentAccountId();

        // Retrieve the account details of the logged-in user.
        Account accountCustomer = SecurityUtil.getCurrentAccount();

        // account must have completed the individual profile to booking
        // Validate profile completion
        if (!isProfileComplete(accountCustomer.getProfile())) {
            throw new AppException(ErrorCode.FORBIDDEN_PROFILE_INCOMPLETE);
        }

        // Retrieve car details from the database, throw an exception if not found.
        Car car = carRepository.findById(bookingRequest.getCarId())
                .orElseThrow(() -> new AppException(ErrorCode.CAR_NOT_FOUND_IN_DB));

        // Retrieve the user's wallet, throw an exception if not found.
        Wallet walletCustomer = walletRepository.findById(accountId)
                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND_IN_DB));

        // Check if the car is available for the requested pickup and drop-off time.
        if (!carService.isCarAvailable(car.getId(), bookingRequest.getPickUpTime(), bookingRequest.getDropOffTime())) {
            throw new AppException(ErrorCode.CAR_NOT_AVAILABLE);
        }

        // Map the booking request to a Booking entity.
        Booking booking = bookingMapper.toBooking(bookingRequest);
        booking.setBookingNumber(redisUtil.generateBookingNumber());

        // Upload the driver's license to S3 storage.
        MultipartFile drivingLicense = bookingRequest.getDriverDrivingLicense();
        String s3Key;
        if (bookingRequest.isDriver()) {
            if (drivingLicense == null || drivingLicense.isEmpty()) {
                throw new AppException(ErrorCode.INVALID_DRIVER_INFO);
            }
            s3Key = "booking/" + booking.getBookingNumber() + "/driver-driving-license" + fileService.getFileExtension(bookingRequest.getDriverDrivingLicense());
        } else {
            s3Key = accountCustomer.getProfile().getDrivingLicenseUri();
        }
        fileService.uploadFile(drivingLicense, s3Key);

        booking.setDriverDrivingLicenseUri(s3Key);

        // Assign account and car to the booking.
        booking.setAccount(accountCustomer);
        booking.setCar(car);

        // Store car deposit and base price at the time of booking.
        booking.setDeposit(car.getDeposit());
        booking.setBasePrice(car.getBasePrice());

        // Calculate rental duration in minutes.
        long minutes = Duration.between(booking.getPickUpTime(), booking.getDropOffTime()).toMinutes();
        long days = (long) Math.ceil(minutes / (24.0 * 60)); // Convert minutes to full days.

        // Handle different payment types.
        if (booking.getPaymentType().equals(EPaymentType.WALLET)
                && walletCustomer.getBalance() >= car.getDeposit()) {
            // If the user has enough balance in the wallet, deduct the deposit and proceed.
            transactionService.payDeposit(accountId, booking.getDeposit(), booking);
            booking.setStatus(EBookingStatus.WAITING_CONFIRMED);
            walletRepository.save(walletCustomer);
        } else {
            booking.setStatus(EBookingStatus.PENDING_DEPOSIT);
        }


        // Save the booking to the database.
        bookingRepository.save(booking);

        // Convert the booking entity to a response DTO.
        BookingResponse bookingResponse = bookingMapper.toBookingResponse(booking);
        bookingResponse.setDriverDrivingLicenseUrl(fileService.getFileUrl(s3Key));
        bookingResponse.setCarId(booking.getCar().getId());
        bookingResponse.setTotalPrice(booking.getBasePrice() * days);

        return bookingResponse;
    }


    public BookingResponse editBooking(EditBookingRequest editBookingRequest, String bookingNumber) throws AppException {
        // Update status bookings before creating a new one.
        updateStatusBookings();

        // Get the current logged-in user's account ID.
        String accountId = SecurityUtil.getCurrentAccountId();
        // Retrieve the account details of the logged-in user.
        Account accountCustomer = SecurityUtil.getCurrentAccount();

        // account must have completed the individual profile to booking
        // Validate profile completion
        if (!isProfileComplete(accountCustomer.getProfile())) {
            throw new AppException(ErrorCode.FORBIDDEN_PROFILE_INCOMPLETE);
        }

        // Retrieve booking details from the database
        Booking booking = bookingRepository.findBookingByBookingNumber(bookingNumber);
        if (booking == null) {
            throw new AppException(ErrorCode.BOOKING_NOT_FOUND_IN_DB);
        }
        if (!booking.getAccount().getId().equals(accountId)) {
            throw new AppException(ErrorCode.FORBIDDEN_BOOKING_ACCESS);
        }

        // Update the car details using the request data
        bookingMapper.editBooking(booking, editBookingRequest);
        MultipartFile drivingLicense = editBookingRequest.getDriverDrivingLicense();
        String s3Key;
        String existingUri = booking.getDriverDrivingLicenseUri();
        if (existingUri == null) {
            existingUri = "";
        }
        if (editBookingRequest.isDriver()) {
            if ((drivingLicense == null || drivingLicense.isEmpty()) && existingUri.startsWith("user/")) {
                throw new AppException(ErrorCode.INVALID_DRIVER_INFO);
            }
            if (drivingLicense != null && !drivingLicense.isEmpty()) {
                s3Key = "booking/" + booking.getBookingNumber() + "/driver-driving-license" + fileService.getFileExtension(drivingLicense);
                fileService.uploadFile(drivingLicense, s3Key);  // Only upload when there is a file
            } else {
                s3Key = existingUri; // Keep existing URI
            }
        } else {
            s3Key = accountCustomer.getProfile().getDrivingLicenseUri();
        }
        // Only upload if there is a file to avoid NullPointerException
        if (drivingLicense != null && !drivingLicense.isEmpty()) {
            fileService.uploadFile(drivingLicense, s3Key);
        }

        booking.setDriverDrivingLicenseUri(s3Key);

        // Save the booking to the database.
        bookingRepository.saveAndFlush(booking);

        // Calculate rental duration in minutes.
        long minutes = Duration.between(booking.getPickUpTime(), booking.getDropOffTime()).toMinutes();
        long days = (long) Math.ceil(minutes / (24.0 * 60)); // Convert minutes to full days.

        // Convert the booking entity to a response DTO.
        BookingResponse bookingResponse = bookingMapper.toBookingResponse(booking);
        bookingResponse.setDriverDrivingLicenseUrl(fileService.getFileUrl(s3Key));
        bookingResponse.setCarId(booking.getCar().getId());
        bookingResponse.setTotalPrice(booking.getBasePrice() * days);

        return bookingResponse;

    }

    /**
     * Scheduled task to update expired bookings.
     * Runs every 10 seconds to check and cancel bookings that are not confirmed within 2 minutes.
     */
    @Scheduled(fixedRate = 10000)
    public void updateStatusBookings() {
        LocalDateTime now = LocalDateTime.now();

        // Find bookings that have expired (not confirmed within 1 hour).
        List<Booking> expiredBookings = bookingRepository.findExpiredBookings(now.minusMinutes(2));

        // Cancel expired bookings.
        for (Booking booking : expiredBookings) {
            if (booking.getCreatedAt().plusHours(1).isBefore(now)) {
                booking.setStatus(EBookingStatus.CANCELLED);
                booking.setUpdatedAt(now);
                bookingRepository.saveAndFlush(booking);
            }
        }

        // find all booking not expired
        List<Booking> pendingBookings = bookingRepository.findPendingDepositBookings(now.minusHours(1));
        for (Booking booking : pendingBookings) {
            Wallet wallet = walletRepository.findById(booking.getAccount().getId())
                    .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND_IN_DB));

            // check when customer top up wallet
            if (wallet.getBalance() >= booking.getDeposit()) {
                transactionService.payDeposit(booking.getAccount().getId(), booking.getDeposit(), booking);
                booking.setStatus(EBookingStatus.WAITING_CONFIRMED);
                walletRepository.save(wallet);
                bookingRepository.save(booking);
                // cancel booking overlap
                List<Booking> overlappingBookings = bookingRepository.findByCarIdAndStatusAndTimeOverlap(
                        booking.getCar().getId(),
                        EBookingStatus.PENDING_DEPOSIT,
                        booking.getPickUpTime(),
                        booking.getDropOffTime()
                );

                for (Booking pendingBooking : overlappingBookings) {
                    pendingBooking.setStatus(EBookingStatus.CANCELLED);
                    bookingRepository.saveAndFlush(pendingBooking);
                }

                break; // stopped when a booking change to waiting confirm
            }
        }
    }

    /**
     * to check the account must complete the profile before booking
     *
     * @param profile the profile of the current account
     * @return the profile with full information
     */
    private boolean isProfileComplete(UserProfile profile) {
        return profile != null
                && profile.getFullName() != null && !profile.getFullName().isEmpty()
                && profile.getDob() != null
                && profile.getNationalId() != null && !profile.getNationalId().isEmpty()
                && profile.getPhoneNumber() != null && !profile.getPhoneNumber().isEmpty()
                && profile.getCityProvince() != null && !profile.getCityProvince().isEmpty()
                && profile.getDistrict() != null && !profile.getDistrict().isEmpty()
                && profile.getWard() != null && !profile.getWard().isEmpty()
                && profile.getHouseNumberStreet() != null && !profile.getHouseNumberStreet().isEmpty()
                && profile.getDrivingLicenseUri() != null && !profile.getDrivingLicenseUri().isEmpty();
    }


    /**
     * Retrieves the list of bookings for the renter (based on accountId).
     * If the status is null or invalid, it returns all bookings.
     *
     * @param page   the page number (starting from 0)
     * @param size   the number of records per page
     * @param sort   sorting string in the format "field,DIRECTION" (e.g., "updatedAt,DESC")
     * @param status booking status (nullable to fetch all bookings)
     * @return list of user bookings wrapped in `BookingListResponse`
     */
    public BookingListResponse getBookingsByUserId(int page, int size, String sort, String status) {
        // Retrieve the account ID of the currently logged-in user
        String accountId = SecurityUtil.getCurrentAccountId();
        Pageable pageable = createPageable(page, size, sort);

        Page<Booking> bookings;
        if (status == null || status.isBlank()) {
            // If no status is provided, retrieve all bookings for the user
            bookings = bookingRepository.findByAccountId(accountId, pageable);
        } else {
            // Parse the provided status string into an enum value
            EBookingStatus bookingStatus = parseStatus(status);
            bookings = (bookingStatus != null)
                    ? bookingRepository.findByAccountIdAndStatus(accountId, bookingStatus, pageable)
                    : bookingRepository.findByAccountId(accountId, pageable);
        }

        return getBookingListResponse(bookings);
    }

    /**
     * Retrieves the list of bookings for the car owner (based on ownerId).
     * If the status is null or invalid, it returns all bookings except those in PENDING_DEPOSIT status.
     */
    public BookingListResponse getBookingsByCarOwner(int page, int size, String sort, String status) {
        // Retrieve the car owner's account ID
        String ownerId = SecurityUtil.getCurrentAccountId();
        Pageable pageable = createPageable(page, size, sort);

        Page<Booking> bookings;
        if (status == null || status.isBlank()) {
            // Fetch all bookings that do not have PENDING_DEPOSIT status
            bookings = bookingRepository.findBookingsByCarOwnerId(ownerId, EBookingStatus.PENDING_DEPOSIT, pageable);
        } else {
            // Parse the provided status string into an enum value
            EBookingStatus bookingStatus = parseStatus(status);
            bookings = (bookingStatus != null)
                    ? bookingRepository.findBookingsByCarOwnerIdAndStatus(ownerId, bookingStatus, EBookingStatus.PENDING_DEPOSIT, pageable)
                    : bookingRepository.findBookingsByCarOwnerId(ownerId, EBookingStatus.PENDING_DEPOSIT, pageable);
        }

        return getBookingListResponse(bookings);
    }

    /**
     * Converts a page of `Booking` objects into `BookingListResponse`.
     * Includes booking details, car images, and additional computed fields.
     */
    private BookingListResponse getBookingListResponse(Page<Booking> bookings) {
        // Retrieve the current user's account ID
        String currentUserId = SecurityUtil.getCurrentAccountId();

        // Map booking entities to response DTOs
        Page<BookingThumbnailResponse> bookingResponses = bookings.map(booking -> {
            BookingThumbnailResponse response = bookingMapper.toBookingThumbnailResponse(booking);

            // Calculate rental duration in days and total price
            long totalHours = Duration.between(booking.getPickUpTime(), booking.getDropOffTime()).toHours();
            long numberOfDay = (long) Math.ceil((double) totalHours / 24);
            long totalPrice = numberOfDay * booking.getBasePrice();

            response.setNumberOfDay((int) numberOfDay);
            response.setTotalPrice(totalPrice);
            response.setUpdatedAt(booking.getUpdatedAt());

            // Retrieve car images
            response.setCarImageFrontUrl(fileService.getFileUrl(booking.getCar().getCarImageFront()));
            response.setCarImageBackUrl(fileService.getFileUrl(booking.getCar().getCarImageBack()));
            response.setCarImageLeftUrl(fileService.getFileUrl(booking.getCar().getCarImageLeft()));
            response.setCarImageRightUrl(fileService.getFileUrl(booking.getCar().getCarImageRight()));

            return response;
        });

        // Count the number of ongoing bookings for the current user
        int totalOnGoingBookings = bookingRepository.countOngoingBookingsByCar(
                currentUserId,
                List.of(
                        EBookingStatus.PENDING_DEPOSIT,
                        EBookingStatus.WAITING_CONFIRMED,
                        EBookingStatus.CONFIRMED,
                        EBookingStatus.IN_PROGRESS,
                        EBookingStatus.PENDING_PAYMENT
                )
        );

        // Count the number of bookings that are waiting for confirmation for the car owner
        int totalWaitingConfirmBooking = bookingRepository.countBookingsByOwnerAndStatus(
                currentUserId,
                EBookingStatus.WAITING_CONFIRMED
        );

        return new BookingListResponse(totalOnGoingBookings, totalWaitingConfirmBooking, bookingResponses);
    }

    /**
     * Creates a `Pageable` object for pagination and sorting.
     * Ensures default values are set if the input is invalid.
     */
    private Pageable createPageable(int page, int size, String sort) {
        if (size <= 0 || size > 100) {
            size = 10; // Default page size
        }
        if (page < 0) {
            page = 0;
        }

        // Default sorting values
        String sortField = FIELD_UPDATED_AT;
        Sort.Direction sortDirection = Sort.Direction.DESC;

        // Parse sorting parameters if provided
        // Check if the 'sort' parameter is valid (not null or blank)
        if (sort != null && !sort.isBlank()) {
            // Split the 'sort' string by commas to extract the sorting field and direction
            String[] sortParams = sort.split(",");

            // Ensure that the 'sort' parameter has exactly two parts: [field_name, sort_direction]
            if (sortParams.length == 2) {
                // Trim any extra spaces from the field name
                String requestedField = sortParams[0].trim();

                // Convert the sorting direction to uppercase to ensure consistency (e.g., "asc" -> "ASC")
                String requestedDirection = sortParams[1].trim().toUpperCase();

                // Check if the requested sorting field is valid (only allows "updatedAt" or "basePrice")
                if (List.of(FIELD_UPDATED_AT, FIELD_BASE_PRICE).contains(requestedField)) {
                    sortField = requestedField; // Assign the requested sorting field if valid
                }

                // Validate sorting direction, only allowing "ASC" or "DESC"
                if (requestedDirection.equals("ASC") || requestedDirection.equals("DESC")) {
                    sortDirection = Sort.Direction.valueOf(requestedDirection); // Set sorting direction
                }
            }
        }

        return PageRequest.of(page, size, Sort.by(sortDirection, sortField));
    }

    /**
     * Converts a status string to an `EBookingStatus` enum.
     * If the status is invalid or not found, returns `null` (defaulting to all bookings).
     */
    private EBookingStatus parseStatus(String statusStr) {
        // Check if the input status is null or blank
        if (statusStr == null || statusStr.isBlank()) {
            return null; // Return null to indicate that all statuses should be considered
        }

        // Convert the input string to an EBookingStatus enum by matching it with defined values
        return Arrays.stream(EBookingStatus.values())  // Convert enum values to a stream
                .filter(e -> e.name().equalsIgnoreCase(statusStr)) // Compare ignoring case
                .findFirst() // Return the first match if found
                .orElse(null); // Return null if no match is found
    }

    /**
     * Retrieve the booking details based on the given booking number.
     * Ensures proper authorization for CAR_OWNER and CUSTOMER roles.
     */
    public BookingResponse getBookingDetailsByBookingNumber(String bookingNumber) {
        // Retrieve the currently logged-in user's account ID
        String accountId = SecurityUtil.getCurrentAccountId();

        // Get the full account details of the currently logged-in user
        Account account = SecurityUtil.getCurrentAccount();

        Booking booking;

        // Check if the user is a CAR_OWNER
        if (ERole.CAR_OWNER.equals(account.getRole().getName())) {
            // Retrieve the booking details only if the booking is associated with the car owner's vehicles
            booking = bookingRepository.findBookingByBookingNumberAndOwnerId(bookingNumber, accountId);

            // If no booking is found, throw an exception indicating that it does not exist in the database
            if (booking == null) {
                throw new AppException(ErrorCode.BOOKING_NOT_FOUND_IN_DB);
            }

            // Check if the user is a CUSTOMER
        } else if (ERole.CUSTOMER.equals(account.getRole().getName())) {
            // Retrieve the booking details (without filtering by owner)
            booking = bookingRepository.findBookingByBookingNumber(bookingNumber);

            // If the booking does not exist OR it does not belong to the current user, deny access
            if (booking == null || !booking.getAccount().getId().equals(accountId)) {
                throw new AppException(ErrorCode.FORBIDDEN_BOOKING_ACCESS);
            }

            // If the user role is neither CAR_OWNER nor CUSTOMER, throw an unauthorized error
        } else {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        // Calculate the rental duration in total minutes
        long minutes = Duration.between(booking.getPickUpTime(), booking.getDropOffTime()).toMinutes();

        // Convert minutes to full days (rounded up)
        long days = (long) Math.ceil(minutes / (24.0 * 60));

        // Convert the booking entity to a response DTO
        BookingResponse bookingResponse = bookingMapper.toBookingResponse(booking);

        // Set the driver's driving license URL
        bookingResponse.setDriverDrivingLicenseUrl(fileService.getFileUrl(booking.getDriverDrivingLicenseUri()));

        // Set the ID of the car associated with the booking
        bookingResponse.setCarId(booking.getCar().getId());

        // Calculate and set the total price for the rental period
        bookingResponse.setTotalPrice(booking.getBasePrice() * days);

        return bookingResponse;
    }
}