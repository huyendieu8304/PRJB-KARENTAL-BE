package com.mp.karental.service;

import com.mp.karental.constant.EBookingStatus;
import com.mp.karental.constant.EPaymentType;
import com.mp.karental.dto.request.booking.BookingRequest;
import com.mp.karental.dto.request.booking.EditBookingRequest;
import com.mp.karental.dto.response.booking.BookingResponse;
import com.mp.karental.dto.response.booking.BookingThumbnailResponse;
import com.mp.karental.dto.response.booking.WalletResponse;
import com.mp.karental.entity.Account;
import com.mp.karental.entity.Booking;
import com.mp.karental.entity.Car;
import com.mp.karental.entity.Wallet;
import com.mp.karental.entity.UserProfile;
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
    private static final String FIELD_CREATED_AT = "createdAt";
    private static final String FIELD_BASE_PRICE = "basePrice";

    /**
     * Creates a new booking for a car rental.
     *
     * @param bookingRequest The booking request details.
     * @return BookingResponse containing booking details.
     * @throws AppException if there are validation issues or car availability problems.
     */
    public BookingResponse createBooking(BookingRequest bookingRequest) throws AppException {
        Account accountCustomer = prepareBooking();
        String accountId = accountCustomer.getId();

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
        String s3Key = handleDriverLicense(bookingRequest, booking, accountCustomer);

        booking.setDriverDrivingLicenseUri(s3Key);

        // Assign account and car to the booking.
        booking.setAccount(accountCustomer);
        booking.setCar(car);

        // Store car deposit and base price at the time of booking.
        booking.setDeposit(car.getDeposit());
        booking.setBasePrice(car.getBasePrice());

        // Handle different payment types.
        if (booking.getPaymentType().equals(EPaymentType.WALLET)
                && walletCustomer.getBalance() >= car.getDeposit()) {
            // If the user has enough balance in the wallet, deduct the deposit and proceed.
            transactionService.payDeposit(accountId, booking.getDeposit(), booking);
            booking.setStatus(EBookingStatus.WAITING_CONFIRM);
            walletRepository.save(walletCustomer);
        } else {
            booking.setStatus(EBookingStatus.PENDING_DEPOSIT);
        }
        // Save the booking to the database.
        bookingRepository.save(booking);

        return buildBookingResponse(booking, s3Key);
    }


    public BookingResponse editBooking(EditBookingRequest editBookingRequest, String bookingNumber) throws AppException {
        Account accountCustomer = prepareBooking();
        String accountId = accountCustomer.getId();

        // Retrieve booking details from the database
        Booking booking = bookingRepository.findBookingByBookingNumber(bookingNumber);
        if (booking == null) {
            throw new AppException(ErrorCode.BOOKING_NOT_FOUND_IN_DB);
        }
        if (!booking.getAccount().getId().equals(accountId)) {
            throw new AppException(ErrorCode.FORBIDDEN_BOOKING_ACCESS);
        }
        if(!editBookingRequest.getCarId().equals(booking.getCar().getId())) {
            throw new AppException(ErrorCode.CAR_NOT_AVAILABLE);
        }

        if (booking.getStatus() == EBookingStatus.IN_PROGRESS ||
                booking.getStatus() == EBookingStatus.PENDING_PAYMENT ||
                booking.getStatus() == EBookingStatus.COMPLETED ||
                booking.getStatus() == EBookingStatus.CANCELLED) {
            throw new AppException(ErrorCode.BOOKING_CANNOT_BE_EDITED);
        }

        // Update the car details using the request data
        bookingMapper.editBooking(booking, editBookingRequest);
        String s3Key = handleDriverLicense(editBookingRequest, booking, accountCustomer);

        booking.setDriverDrivingLicenseUri(s3Key);

        // Save the booking to the database.
        bookingRepository.saveAndFlush(booking);

        return buildBookingResponse(booking, s3Key);
    }

    private String handleDriverLicense(Object request, Booking booking, Account accountCustomer) throws AppException {
        MultipartFile drivingLicense ;
        String s3Key = "";
        if(request instanceof BookingRequest bookingRequest){
            drivingLicense = bookingRequest.getDriverDrivingLicense();
            // Upload the driver's license to S3 storage.
            if (bookingRequest.isDriver()) {
                if (drivingLicense == null || drivingLicense.isEmpty()) {
                    throw new AppException(ErrorCode.INVALID_DRIVER_INFO);
                }
                checkNullPointerDriver(bookingRequest);
                s3Key = "booking/" + booking.getBookingNumber() + "/driver-driving-license" + fileService.getFileExtension(bookingRequest.getDriverDrivingLicense());
                fileService.uploadFile(drivingLicense, s3Key);
            } else {
                s3Key = accountCustomer.getProfile().getDrivingLicenseUri();

                booking.setDriverFullName(accountCustomer.getProfile().getFullName());
                setAccountProfileToDriver(booking, accountCustomer);
            }
        }
        else if(request instanceof EditBookingRequest editBookingRequest){
            drivingLicense = editBookingRequest.getDriverDrivingLicense();
            String existingUri = booking.getDriverDrivingLicenseUri();
            if (existingUri == null) {
                existingUri = "";
            }
            if (editBookingRequest.isDriver()) {
                if ((drivingLicense == null || drivingLicense.isEmpty()) && existingUri.startsWith("user/")) {
                    throw new AppException(ErrorCode.INVALID_DRIVER_INFO);
                }
                checkNullPointerDriver(editBookingRequest);
                if (drivingLicense != null && !drivingLicense.isEmpty()) {
                    s3Key = "booking/" + booking.getBookingNumber() + "/driver-driving-license" + fileService.getFileExtension(drivingLicense);
                    fileService.uploadFile(drivingLicense, s3Key);  // Only upload when there is a file
                } else {
                    s3Key = existingUri; // Keep existing URI
                }
            } else {
                s3Key = accountCustomer.getProfile().getDrivingLicenseUri();
                setAccountProfileToDriver(booking, accountCustomer);
            }
        }
        return s3Key;
    }

    private Account prepareBooking() throws AppException {
        // Update status bookings before creating or editing a booking
        updateStatusBookings();

        // Get the current logged-in user's account ID and account details
        Account accountCustomer = SecurityUtil.getCurrentAccount();

        // Ensure the account has completed the individual profile
        if (!isProfileComplete(accountCustomer.getProfile())) {
            throw new AppException(ErrorCode.FORBIDDEN_PROFILE_INCOMPLETE);
        }

        return accountCustomer;
    }


    private BookingResponse buildBookingResponse(Booking booking, String s3Key) {
        // Calculate rental duration in minutes.
        long minutes = Duration.between(booking.getPickUpTime(), booking.getDropOffTime()).toMinutes();
        long days = (long) Math.ceil(minutes / (24.0 * 60)); // Convert minutes to full days.

        // Convert the booking entity to a response DTO.
        BookingResponse bookingResponse = bookingMapper.toBookingResponse(booking);
        bookingResponse.setDriverDrivingLicenseUrl(fileService.getFileUrl(s3Key));
        bookingResponse.setCarId(booking.getCar().getId());
        bookingResponse.setTotalPrice(booking.getBasePrice() * days);
        if(booking.getDriverDrivingLicenseUri().startsWith("user/")){
            bookingResponse.setDriver(false);
        }
        else{
            bookingResponse.setDriver(true);
        }
        return bookingResponse;
    }

    private boolean isNullOrEmpty(String str) {
        return str == null || str.trim().isEmpty();
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
                booking.setStatus(EBookingStatus.WAITING_CONFIRM);
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

    private void setAccountProfileToDriver(Booking booking,Account account) {
        booking.setDriverFullName(account.getProfile().getFullName());
        booking.setDriverPhoneNumber(account.getProfile().getPhoneNumber());
        booking.setDriverNationalId(account.getProfile().getNationalId());
        booking.setDriverDob(account.getProfile().getDob());
        booking.setDriverEmail(account.getEmail());
        booking.setDriverCityProvince(account.getProfile().getCityProvince());
        booking.setDriverDistrict(account.getProfile().getDistrict());
        booking.setDriverWard(account.getProfile().getWard());
        booking.setDriverHouseNumberStreet(account.getProfile().getHouseNumberStreet());
    }

    private void checkNullPointerDriver(Object request){
        if(request instanceof BookingRequest bookingRequest){
            if (isNullOrEmpty(bookingRequest.getDriverFullName()) ||
                    isNullOrEmpty(bookingRequest.getDriverPhoneNumber()) ||
                    isNullOrEmpty(bookingRequest.getDriverNationalId()) ||
                    bookingRequest.getDriverDob() == null ||
                    isNullOrEmpty(bookingRequest.getDriverEmail()) ||
                    isNullOrEmpty(bookingRequest.getDriverCityProvince()) ||
                    isNullOrEmpty(bookingRequest.getDriverDistrict()) ||
                    isNullOrEmpty(bookingRequest.getDriverWard()) ||
                    isNullOrEmpty(bookingRequest.getDriverHouseNumberStreet())) {
                throw new AppException(ErrorCode.INVALID_DRIVER_INFO);
            }
        }
        else if(request instanceof EditBookingRequest editBookingRequest){
            if (isNullOrEmpty(editBookingRequest.getDriverFullName()) ||
                    isNullOrEmpty(editBookingRequest.getDriverPhoneNumber()) ||
                    isNullOrEmpty(editBookingRequest.getDriverNationalId()) ||
                    editBookingRequest.getDriverDob() == null ||
                    isNullOrEmpty(editBookingRequest.getDriverEmail()) ||
                    isNullOrEmpty(editBookingRequest.getDriverCityProvince()) ||
                    isNullOrEmpty(editBookingRequest.getDriverDistrict()) ||
                    isNullOrEmpty(editBookingRequest.getDriverWard()) ||
                    isNullOrEmpty(editBookingRequest.getDriverHouseNumberStreet())) {
                throw new AppException(ErrorCode.INVALID_DRIVER_INFO);
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
     * Retrieves the list of bookings for the currently logged-in user, with pagination and sorting.
     *
     * @param page the page number to retrieve
     * @param size the number of records per page
     * @param sort sorting field and direction in the format "field,DIRECTION"
     * @return a paginated list of `BookingThumbnailResponse`
     */
    public Page<BookingThumbnailResponse> getBookingsByUserId(int page, int size, String sort) {
        // Get the currently authenticated user's account ID
        String accountId = SecurityUtil.getCurrentAccountId();

        // Validate and limit size (maximum 100)
        if (size <= 0 || size > 100) {
            size = 10; // Default value if client provides an invalid input
        }

        // Ensure page number is non-negative (set to 0 if negative)
        if (page < 0) {
            page = 0;
        }

        // Define default sorting field and direction
        String sortField = FIELD_CREATED_AT;
        Sort.Direction sortDirection = Sort.Direction.DESC;

        if (sort != null && !sort.isBlank()) {
            String[] sortParams = sort.split(",");
            if (sortParams.length == 2) {
                String requestedField = sortParams[0].trim();
                String requestedDirection = sortParams[1].trim().toUpperCase();

                // Check if requestedField valid
                if (List.of(FIELD_CREATED_AT, FIELD_BASE_PRICE).contains(requestedField)) {
                    sortField = requestedField;
                }

                // Check if requestedDirection valid
                if (requestedDirection.equals("ASC") || requestedDirection.equals("DESC")) {
                    sortDirection = Sort.Direction.valueOf(requestedDirection);
                }
            }
        }

        // Create pageable object with sorting
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortField));

        // Retrieve bookings from the repository
        Page<Booking> bookings = bookingRepository.findByAccountId(accountId, pageable);

        return bookings.map(booking -> {
            // Map the Booking entity to a BookingThumbnailResponse DTO
            BookingThumbnailResponse response = bookingMapper.toBookingThumbnailResponse(booking);

            // Calculate the total rental period in days
            long totalHours = Duration.between(booking.getPickUpTime(), booking.getDropOffTime()).toHours();
            long numberOfDay = (long) Math.ceil((double) totalHours / 24); // Round up to full days

            // Calculate the total price based on the base price and rental period
            long totalPrice = numberOfDay * booking.getBasePrice();

            response.setNumberOfDay((int) numberOfDay);
            response.setTotalPrice(totalPrice);
            response.setCreatedAt(booking.getCreatedAt());

            // Retrieve car images from the file storage service
            response.setCarImageFrontUrl(fileService.getFileUrl(booking.getCar().getCarImageFront()));
            response.setCarImageBackUrl(fileService.getFileUrl(booking.getCar().getCarImageBack()));
            response.setCarImageLeftUrl(fileService.getFileUrl(booking.getCar().getCarImageLeft()));
            response.setCarImageRightUrl(fileService.getFileUrl(booking.getCar().getCarImageRight()));

            return response;
        });

    }

    /**
     * this method to get the wallet by account login
     *
     * @return wallet of that account
     */
    public WalletResponse getWallet() {
        // Get the current logged-in user's account ID.
        String accountId = SecurityUtil.getCurrentAccountId();
        Wallet wallet = walletRepository.findById(accountId)
                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND_IN_DB));
        return new WalletResponse(
                wallet.getId(),
                wallet.getBalance()
        );
    }

    public BookingResponse getBookingDetailsByBookingNumber(String bookingNumber) {
        // Retrieve the current user account ID to ensure the user is logged in
        String accountId = SecurityUtil.getCurrentAccountId();
        // Fetch the booking details from the database, or throw an error if the booking is not found
        Booking booking = bookingRepository.findBookingByBookingNumber(bookingNumber);
        if (booking == null) {
            throw new AppException(ErrorCode.BOOKING_NOT_FOUND_IN_DB);
        }
        if (!booking.getAccount().getId().equals(accountId)) {
            throw new AppException(ErrorCode.FORBIDDEN_BOOKING_ACCESS);
        }
        return buildBookingResponse(booking, booking.getDriverDrivingLicenseUri());
    }

}

