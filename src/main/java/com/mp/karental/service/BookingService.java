package com.mp.karental.service;

import com.mp.karental.constant.EBookingStatus;
import com.mp.karental.constant.EPaymentType;
import com.mp.karental.dto.request.booking.CreateBookingRequest;
import com.mp.karental.constant.ERole;
import com.mp.karental.dto.request.booking.EditBookingRequest;
import com.mp.karental.dto.response.booking.BookingResponse;
import com.mp.karental.dto.response.booking.BookingThumbnailResponse;
import com.mp.karental.dto.response.booking.BookingListResponse;
import com.mp.karental.dto.response.booking.WalletResponse;
import com.mp.karental.entity.*;
import com.mp.karental.exception.AppException;
import com.mp.karental.exception.ErrorCode;
import com.mp.karental.mapper.BookingMapper;
import com.mp.karental.repository.AccountRepository;
import com.mp.karental.repository.BookingRepository;
import com.mp.karental.repository.CarRepository;
import com.mp.karental.repository.WalletRepository;
import com.mp.karental.security.SecurityUtil;
import com.mp.karental.util.RedisUtil;
import jakarta.mail.MessagingException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

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
    AccountRepository accountRepository;
    FileService fileService;
    CarService carService;
    TransactionService transactionService;
    EmailService emailService;

    // Define constant field names to avoid repetition
    private static final String FIELD_UPDATED_AT = "updatedAt";
    private static final String FIELD_BASE_PRICE = "basePrice";

    /**
     * Creates a new booking for a car rental.
     *
     * @param createBookingRequest The booking request details.
     * @return BookingResponse containing booking details.
     * @throws AppException if there are validation issues or car availability problems.
     */
    public BookingResponse createBooking(CreateBookingRequest createBookingRequest) throws AppException {
        // Get the current logged-in user's account ID and account details
        Account customerAccount = prepareBooking();
        String customerAccountId = customerAccount.getId();

        // Retrieve car details from the database, throw an exception if not found.
        Car car = carRepository.findById(createBookingRequest.getCarId())
                .orElseThrow(() -> new AppException(ErrorCode.CAR_NOT_FOUND_IN_DB));

        // Retrieve the customer's wallet, throw an exception if not found.
        Wallet walletCustomer = walletRepository.findById(customerAccountId)
                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND_IN_DB));

        // Check if the car is available for the requested pickup and drop-off time.
        if (!carService.isCarAvailable(car.getId(), createBookingRequest.getPickUpTime(), createBookingRequest.getDropOffTime())) {
            throw new AppException(ErrorCode.CAR_NOT_AVAILABLE);
        }

        // Map the booking request to a Booking entity.
        Booking booking = bookingMapper.toBooking(createBookingRequest);
        booking.setBookingNumber(redisUtil.generateBookingNumber());

        // Upload the driver's license to S3 storage.
        String drivingLicenseKey;

        // Upload the driver's license to S3 storage if the driver information is provided.
        if (createBookingRequest.isDriver()) {
            //the renter is different from the driver
            //ensure that all driver's information is not null and not empty
            validateDriverInfo(
                    createBookingRequest.getDriverFullName(),
                    createBookingRequest.getDriverPhoneNumber(),
                    createBookingRequest.getDriverNationalId(),
                    createBookingRequest.getDriverDob(),
                    createBookingRequest.getDriverEmail(),
                    createBookingRequest.getDriverCityProvince(),
                    createBookingRequest.getDriverDistrict(),
                    createBookingRequest.getDriverWard(),
                    createBookingRequest.getDriverHouseNumberStreet()
            );

            MultipartFile drivingLicense = createBookingRequest.getDriverDrivingLicense();
            drivingLicenseKey = uploadDriverDrivingLicense(drivingLicense, booking);
        } else {
            // Use existing license URI from account profile if the driver is same as the renter
            drivingLicenseKey = customerAccount.getProfile().getDrivingLicenseUri();
            setAccountProfileToDriver(booking, customerAccount); // Set the profile information to the booking.
        }

        booking.setDriverDrivingLicenseUri(drivingLicenseKey);

        // Assign renter account and car to the booking.
        booking.setAccount(customerAccount);
        booking.setCar(car);

        // Store car deposit and base price at the time of booking.
        booking.setDeposit(car.getDeposit());
        booking.setBasePrice(car.getBasePrice());

        // Handle paying deposit.
        if (booking.getPaymentType().equals(EPaymentType.WALLET)
                && walletCustomer.getBalance() >= car.getDeposit()) {
            // If the customer using wallet and has enough balance in the wallet
            payBookingDepositUsingWallet(booking, customerAccount);
        } else {
            // the customer not using wallet to pay deposit or the wallet's balance is not enough
            booking.setStatus(EBookingStatus.PENDING_DEPOSIT);
            redisUtil.cachePendingDepositBooking(booking.getBookingNumber());
        }

        // Save the booking to the database.
        bookingRepository.save(booking);

        return buildBookingResponse(booking, drivingLicenseKey);
    }

    private String uploadDriverDrivingLicense(MultipartFile drivingLicense, Booking booking) {
        if (drivingLicense == null || drivingLicense.isEmpty()) {
            // renter did not provide driver's driving lícense
            throw new AppException(ErrorCode.INVALID_DRIVER_INFO); // Validate driver's license is provided.
        }
        //upload driver's driving lícense
        String drivingLicenseKey = "booking/" + booking.getBookingNumber() + "/driver-driving-license"
                + fileService.getFileExtension(drivingLicense);
        fileService.uploadFile(drivingLicense, drivingLicenseKey);
        return drivingLicenseKey;
    }

    private void payBookingDepositUsingWallet(Booking booking,Account customerAccount)  {
        transactionService.payDeposit(customerAccount.getId(), booking.getDeposit(), booking);
        booking.setStatus(EBookingStatus.WAITING_CONFIRMED);

        //cancelled pending_deposit bookings, which has pickUpTime and dropOffTime overlap this booking
        List<Booking> overlappingBookings = bookingRepository.findByCarIdAndStatusAndTimeOverlap(
                booking.getCar().getId(),
                EBookingStatus.PENDING_DEPOSIT,
                booking.getPickUpTime(),
                booking.getDropOffTime()
        );
        for (Booking pendingBooking : overlappingBookings) {

            String reason = "Your booking has been canceled because another customer has successfully placed a deposit for this car within the same rental period.";
            try {
                emailService.sendBookingEmail(EBookingStatus.CANCELLED, ERole.CUSTOMER,
                        pendingBooking.getAccount().getEmail(), pendingBooking.getCar().getBrand() + " " + pendingBooking.getCar().getModel(),
                        reason);
            } catch (MessagingException e) {
                throw new AppException(ErrorCode.SEND_SYSTEM_CANCEL_BOOKING_EMAIL_FAIL);
            }
            pendingBooking.setStatus(EBookingStatus.CANCELLED);
            bookingRepository.saveAndFlush(pendingBooking);
        }

        //remove the cache pending deposit
        redisUtil.removeCachePendingDepositBooking(booking.getBookingNumber());

        // Sending email to customer and carOwner
        try {
            emailService.sendBookingEmail(EBookingStatus.WAITING_CONFIRMED, ERole.CUSTOMER, booking.getAccount().getEmail(),
                    booking.getCar().getBrand() + " " + booking.getCar().getModel(),
                    booking.getBookingNumber());
        } catch (MessagingException e) {
            throw new AppException(ErrorCode.SEND_SYSTEM_WAITING_CONFIRM_EMAIL_FAIL);
        }
        try {
            emailService.sendBookingEmail(EBookingStatus.WAITING_CONFIRMED, ERole.CAR_OWNER, booking.getCar().getAccount().getEmail(),
                    booking.getCar().getBrand() + " " + booking.getCar().getModel(),
                    booking.getCar().getLicensePlate());
        } catch (MessagingException e) {
            throw new AppException(ErrorCode.SEND_SYSTEM_CONFIRM_DEPOSIT_EMAIL_FAIL);
        }
    }

    /**
     * Edits an existing booking based on the provided booking number and update request.
     *
     * @param editBookingRequest The request details for updating the booking, including new car information and driver’s license.
     * @param bookingNumber The booking number of the booking to be edited.
     * @return BookingResponse containing the updated booking details.
     * @throws AppException If there are any validation issues, the booking is not found, or the user doesn’t have access to edit the booking.
     */
    public BookingResponse editBooking(EditBookingRequest editBookingRequest, String bookingNumber) throws AppException {
        // Get the current logged-in user's account ID and account details
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
        if (!editBookingRequest.getCarId().equals(booking.getCar().getId())) {
            throw new AppException(ErrorCode.CAR_NOT_AVAILABLE);
        }
        //do not allow edit
        if (booking.getStatus() == EBookingStatus.IN_PROGRESS ||
                booking.getStatus() == EBookingStatus.PENDING_PAYMENT ||
                booking.getStatus() == EBookingStatus.COMPLETED ||
                booking.getStatus() == EBookingStatus.CANCELLED) {
            throw new AppException(ErrorCode.BOOKING_CANNOT_BE_EDITED);
        }

        // Update the booking details using the request data
        bookingMapper.editBooking(booking, editBookingRequest);

        //Update the booking uri
        String drivingLicenseKey;

        // If the driver is provided, validate and possibly upload a new driver's license.
        if (editBookingRequest.isDriver()) {
            // Ensure that the required driver fields are not null and not empty
            validateDriverInfo(
                    editBookingRequest.getDriverFullName(),
                    editBookingRequest.getDriverPhoneNumber(),
                    editBookingRequest.getDriverNationalId(),
                    editBookingRequest.getDriverDob(),
                    editBookingRequest.getDriverEmail(),
                    editBookingRequest.getDriverCityProvince(),
                    editBookingRequest.getDriverDistrict(),
                    editBookingRequest.getDriverWard(),
                    editBookingRequest.getDriverHouseNumberStreet()
            );

            MultipartFile drivingLicense = editBookingRequest.getDriverDrivingLicense();
            drivingLicenseKey = uploadDriverDrivingLicense(drivingLicense, booking);
        } else {
            // Use existing license URI from account profile if the driver is same as the renter
            drivingLicenseKey = accountCustomer.getProfile().getDrivingLicenseUri();
            setAccountProfileToDriver(booking, accountCustomer); // Set the profile information to the booking.
        }

        booking.setDriverDrivingLicenseUri(drivingLicenseKey);

        // Save the booking to the database.
        bookingRepository.saveAndFlush(booking);

        return buildBookingResponse(booking, drivingLicenseKey);
    }

    /**
     * Prepares the booking by ensuring the account is valid and the profile is complete.
     * This method updates the status of existing bookings and retrieves the current logged-in user's account details.
     * It also checks if the account's profile is complete before allowing further booking actions.
     *
     * @return The account of the currently logged-in user.
     * @throws AppException If the account's profile is incomplete or any error occurs during the booking preparation.
     */
    private Account prepareBooking() throws AppException {
        //TODO: remove this
//        // Update expired bookings before creating or editing a booking
//        updateStatusBookings();

        // Get the current logged-in user's account ID and account details
        Account accountCustomer = SecurityUtil.getCurrentAccount();

        // Ensure the account has completed the individual profile
        if (!isProfileComplete(accountCustomer.getProfile())) {
            throw new AppException(ErrorCode.FORBIDDEN_PROFILE_INCOMPLETE);
        }

        return accountCustomer;
    }

    /**
     * Builds a BookingResponse object containing the booking details and additional calculated data.
     * This method calculates the rental duration in days, converts the booking entity to a response DTO,
     * and sets the relevant fields such as the driver's license URL, car ID, and total price.
     * It also determines if the booking is for a driver or not based on the URI of the driver's license.
     *
     * @param booking The booking entity containing the booking details.
     * @param s3Key   The S3 key for the driver's license file.
     * @return A BookingResponse object containing the booking details and calculated values.
     */
    private BookingResponse buildBookingResponse(Booking booking, String s3Key) {
        // Calculate rental duration in minutes.
        long minutes = Duration.between(booking.getPickUpTime(), booking.getDropOffTime()).toMinutes();
        long days = (long) Math.ceil(minutes / (24.0 * 60)); // Convert minutes to full days.

        // Convert the booking entity to a response DTO.
        BookingResponse bookingResponse = bookingMapper.toBookingResponse(booking);
        bookingResponse.setDriverDrivingLicenseUrl(fileService.getFileUrl(s3Key));
        bookingResponse.setCarId(booking.getCar().getId());
        bookingResponse.setTotalPrice(booking.getBasePrice() * days);
        bookingResponse.setDriver(!booking.getDriverDrivingLicenseUri().startsWith("user/"));
        return bookingResponse;
    }

    /**
     * Checks if a given string is null or empty.
     * This method returns true if the string is either null or contains only whitespace characters;
     * otherwise, it returns false.
     *
     * @param str The string to check.
     * @return True if the string is null or empty (after trimming); otherwise, false.
     */
    private boolean isNullOrEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    /**
     * Sets the driver's profile information to the booking object.
     * This method takes an Account object, which contains the user's profile details, and updates the corresponding
     * driver information in the Booking object. The driver's personal details, such as full name, phone number,
     * national ID, date of birth, email, and address (city, district, ward, house number/street) are set to the
     * booking entity.
     *
     * @param booking The booking object that will be updated with the driver's information.
     * @param account The account object that contains the current logged-in user's profile details.
     */
    private void setAccountProfileToDriver(Booking booking, Account account) {
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

    /**
     * Validates driver information to ensure all required fields are provided.
     *
     * @param fullName        The full name of the driver (must not be null or empty).
     * @param phoneNumber     The phone number of the driver (must not be null or empty).
     * @param nationalId      The national ID of the driver (must not be null or empty).
     * @param dob             The date of birth of the driver (must not be null).
     * @param email           The email of the driver (must not be null or empty).
     * @param city            The city or province of the driver (must not be null or empty).
     * @param district        The district of the driver (must not be null or empty).
     * @param ward            The ward of the driver (must not be null or empty).
     * @param houseNumberStreet The house number and street address of the driver (must not be null or empty).
     * @throws AppException if any required field is missing or empty.
     */
    private void validateDriverInfo(String fullName, String phoneNumber, String nationalId,
                                    LocalDate dob, String email, String city, String district,
                                    String ward, String houseNumberStreet) {
        boolean isValid = Stream.of(
                fullName,
                phoneNumber,
                nationalId,
                email,
                city,
                district,
                ward,
                houseNumberStreet
        ).noneMatch(this::isNullOrEmpty);
        if (dob == null || !isValid) {
            throw new AppException(ErrorCode.INVALID_DRIVER_INFO);
        }
    }
    /**
     * to check the account must complete the profile before booking
     *
     * @param profile the profile of the current account
     * @return the profile with full information
     */
    private boolean isProfileComplete(UserProfile profile) {
//        return profile.getNationalId() != null && !profile.getNationalId().isEmpty()
//                && profile.getDrivingLicenseUri() != null && !profile.getDrivingLicenseUri().isEmpty()
//                && profile.getPhoneNumber() != null && !profile.getPhoneNumber().isEmpty()
//                && profile.getCityProvince() != null && !profile.getCityProvince().isEmpty()
//                && profile.getDistrict() != null && !profile.getDistrict().isEmpty()
//                && profile.getWard() != null && !profile.getWard().isEmpty()
//                && profile.getHouseNumberStreet() != null && !profile.getHouseNumberStreet().isEmpty()
//                && profile.getFullName() != null && !profile.getFullName().isEmpty();

        return Stream.of(
                profile.getNationalId(),
                profile.getDrivingLicenseUri(),
                profile.getPhoneNumber(),
                profile.getCityProvince(),
                profile.getDistrict(),
                profile.getWard(),
                profile.getHouseNumberStreet(),
                profile.getFullName()
        ).noneMatch(this::isNullOrEmpty);
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
    public BookingListResponse getBookingsOfCustomer(int page, int size, String sort, String status) {
        // Retrieve the currently logged-in user's account ID
        String accountId = SecurityUtil.getCurrentAccountId();

        // Create a Pageable object for pagination and sorting
        Pageable pageable = createPageable(page, size, sort);

        Page<Booking> bookings;

        // Convert the status string into the EBookingStatus enum
        EBookingStatus bookingStatus = parseStatus(status);

        // If the status is valid, fetch bookings filtered by status
        // Otherwise, fetch all bookings for the user
        bookings = (bookingStatus != null)
                ? bookingRepository.findByAccountIdAndStatus(accountId, bookingStatus, pageable)
                : bookingRepository.findByAccountId(accountId, pageable);

        // Convert the list of bookings into a BookingListResponse object to return
        return getBookingListResponse(bookings);
    }


    /**
     * Retrieves the list of bookings for the car owner (based on ownerId). (Car Owner view his/her rentals)
     * If the status is null or invalid, it returns all bookings except those in PENDING_DEPOSIT status.
     */
    public BookingListResponse getBookingsOfCarOwner(int page, int size, String sort, String status) {
        // Retrieve the car owner's account ID
        String ownerId = SecurityUtil.getCurrentAccountId();
        Pageable pageable = createPageable(page, size, sort);

        Page<Booking> bookings;
        // Parse the provided status string into an enum value
        EBookingStatus bookingStatus = parseStatus(status);
        bookings = (bookingStatus != null)
                ? bookingRepository.findBookingsByCarOwnerIdAndStatus(ownerId, bookingStatus, EBookingStatus.PENDING_DEPOSIT, pageable)
                : bookingRepository.findBookingsByCarOwnerId(ownerId, EBookingStatus.PENDING_DEPOSIT, pageable);

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
        // Ensure size is within a reasonable range (default to 10 if invalid)
        size = (size > 0 && size <= 100) ? size : 10;

        // Ensure page number is non-negative (default to 0 if negative)
        page = Math.max(page, 0);

        // Default sorting values
        String sortField = FIELD_UPDATED_AT;
        Sort.Direction sortDirection = Sort.Direction.DESC;

        // Parse sorting parameters if provided
        if (sort != null && !sort.isBlank()) {
            String[] sortParams = sort.split(",");

            // Validate and extract sorting field and direction
            if (sortParams.length == 2) {
                String requestedField = sortParams[0].trim();
                String requestedDirection = sortParams[1].trim().toUpperCase();

                // Validate field name (only allow predefined fields)
                if (FIELD_UPDATED_AT.equals(requestedField) || FIELD_BASE_PRICE.equals(requestedField)) {
                    sortField = requestedField;
                }

                // Validate sorting direction
                if ("ASC".equals(requestedDirection) || "DESC".equals(requestedDirection)) {
                    sortDirection = Sort.Direction.valueOf(requestedDirection);
                }
            }
        }

        // Create and return a pageable object with sorting
        return PageRequest.of(page, size, Sort.by(sortDirection, sortField));
    }


    /**
     * Converts a status string to an `EBookingStatus` enum.
     * If the status is invalid or not found, returns `null` (defaulting to all bookings).
     */
    private EBookingStatus parseStatus(String statusStr) {
        try {
            // Convert the input string to an EBookingStatus enum
            EBookingStatus bookingStatus = EBookingStatus.valueOf(statusStr.toUpperCase());
            return bookingStatus;

        } catch (Exception e){
            log.info("parsing booking status {} to enum fail", statusStr);
            return null;
        }
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

    /**
     * Retrieves booking details by booking number and ensures the user has access to the booking.
     * This method checks the current logged-in user's account ID and compares it with the booking's account ID
     * to ensure the user has access to the booking details. If the booking is not found or the user does not have
     * permission, an exception is thrown.
     * Ensures proper authorization for CAR_OWNER and CUSTOMER roles.
     *
     * @param bookingNumber The booking number to fetch the booking details.
     * @return A BookingResponse object containing the booking details.
     * @throws AppException If the booking is not found or the user does not have permission to access the booking.
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
            if(!booking.getCar().getAccount().getId().equals(accountId)) {
                throw new AppException(ErrorCode.FORBIDDEN_CAR_ACCESS);
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

        return buildBookingResponse(booking, booking.getDriverDrivingLicenseUri());

    }

    /**
     * Confirms a booking by the car owner and returns updated booking details.
     * <p>
     *     Car Owner approve a booking, equivalent to allow the customer to rent the car as booking information
     * </p>
     *
     * @return BookingResponse containing updated booking details.
     * @throws AppException if validation fails.
     */
    public BookingResponse confirmBooking(String bookingNumber)  {
        log.info("Car owner {} is confirming booking {}", SecurityUtil.getCurrentAccount().getId(), bookingNumber);

        // Get booking by bookingNumber
        Booking booking = bookingRepository.findBookingByBookingNumber(bookingNumber);
        if (booking == null) {
            throw new AppException(ErrorCode.BOOKING_NOT_FOUND_IN_DB);
        }

        // Get the current logged-in account
        Account account = SecurityUtil.getCurrentAccount();


        // Ensure the booking belongs to the current car owner
        if (!booking.getCar().getAccount().getId().equals(account.getId())) {
            throw new AppException(ErrorCode.FORBIDDEN_BOOKING_ACCESS);
        }

        // Ensure the booking status is valid for confirmation
        if (booking.getStatus() == null || !EBookingStatus.WAITING_CONFIRMED.equals(booking.getStatus())) {
            throw new AppException(ErrorCode.INVALID_BOOKING_STATUS);
        }

        // Ensure the booking has not expired
        if (booking.getPickUpTime() == null || LocalDateTime.now().isAfter(booking.getPickUpTime())) {
            booking.setStatus(EBookingStatus.CANCELLED);
            bookingRepository.save(booking);
            throw new AppException(ErrorCode.BOOKING_EXPIRED);
        }

        // Update the booking status to CONFIRMED
        booking.setStatus(EBookingStatus.CONFIRMED);
        bookingRepository.saveAndFlush(booking);
        try {
            emailService.sendConfirmPickUpEmail(booking.getAccount().getEmail(),
                    booking.getCar().getBrand() + " " + booking.getCar().getModel(),
                    booking.getBookingNumber());
        }
        catch (MessagingException e) {
            throw new AppException(ErrorCode.SEND_CONFIRMED_BOOKING_EMAIL_FAIL);
        }

        log.info("Booking {} confirmed successfully by car owner {}", bookingNumber, account.getId());

        return buildBookingResponse(booking, booking.getDriverDrivingLicenseUri());
    }

    /**
     * Cancels a booking based on the provided booking number.
     * This method ensures that only the booking owner can cancel the booking.
     * Depending on the current booking status, it processes refunds, updates wallet balances,
     * and sends appropriate email notifications to the customer and car owner.
     *
     * @param bookingNumber The unique identifier of the booking to be canceled.
     * @return A {@link BookingResponse} containing the updated booking details.
     * @throws AppException If the booking is not found, the user is unauthorized to cancel,
     *                      or the booking is in a non-cancellable state.
     */
    public BookingResponse cancelBooking(String bookingNumber) {
        // Get the currently logged-in account ID
        String accountId = SecurityUtil.getCurrentAccountId();

        // Find the booking by booking number
        Booking booking = bookingRepository.findBookingByBookingNumber(bookingNumber);
        if (booking == null) {
            throw new AppException(ErrorCode.BOOKING_NOT_FOUND_IN_DB); // Throw an error if booking is not found
        }

        // Ensure that the booking belongs to the logged-in user
        if (!booking.getAccount().getId().equals(accountId)) {
            throw new AppException(ErrorCode.FORBIDDEN_BOOKING_ACCESS); // Throw an error if user tries to cancel someone else's booking
        }


        // Retrieve the customer's wallet, throw an error if not found
        Wallet walletCustomer = walletRepository.findById(accountId)
                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND_IN_DB));

        // Get the admin account ID (assuming the admin has roleId = 3)
        String accountIdAdmin = accountRepository.findByRoleId(3).getId();
        Wallet walletAdmin = walletRepository.findById(accountIdAdmin)
                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND_IN_DB));

        // Check if the booking is in a state that cannot be canceled
        if (booking.getStatus() == EBookingStatus.IN_PROGRESS ||
                booking.getStatus() == EBookingStatus.PENDING_PAYMENT ||
                booking.getStatus() == EBookingStatus.COMPLETED ||
                booking.getStatus() == EBookingStatus.CANCELLED) {
            throw new AppException(ErrorCode.BOOKING_CANNOT_CANCEL);
        }

        // If the booking is in PENDING_DEPOSIT status, only send a cancellation email to the customer
        if (booking.getStatus() == EBookingStatus.PENDING_DEPOSIT) {
            try {
                emailService.sendBookingCancellationEmail(booking.getAccount().getEmail(), ERole.CUSTOMER,
                        EBookingStatus.PENDING_DEPOSIT, booking.getCar().getBrand() + " " + booking.getCar().getModel());
            }
            catch (MessagingException e) {
                throw new AppException(ErrorCode.SEND_SYSTEM_CANCEL_BOOKING_EMAIL_FAIL);
            }
        }

        // If the booking is in WAITING_CONFIRMED status, refund the full deposit to the customer
        else if (booking.getStatus() == EBookingStatus.WAITING_CONFIRMED) {

            // Send an email notification to the customer about the full refund
            try {
                emailService.sendBookingCancellationEmail(booking.getAccount().getEmail(), ERole.CUSTOMER,
                        EBookingStatus.WAITING_CONFIRMED, booking.getCar().getBrand() + " " + booking.getCar().getModel());
            }
            catch (MessagingException e) {
                throw new AppException(ErrorCode.SEND_SYSTEM_CANCEL_BOOKING_EMAIL_FAIL);
            }
            // Refund the full deposit amount to the customer's wallet
            walletCustomer.setBalance(booking.getDeposit() + walletCustomer.getBalance());
            // Deduct the refunded amount from the admin's wallet
            walletAdmin.setBalance(walletAdmin.getBalance() - booking.getDeposit());
        }

        // If the booking is in CONFIRMED status, refund 70% of the deposit and notify the car owner
        else if (booking.getStatus() == EBookingStatus.CONFIRMED) {

            // Send an email notification to the customer about the partial refund (70%)
            try {
                emailService.sendBookingCancellationEmail(booking.getAccount().getEmail(), ERole.CUSTOMER,
                        EBookingStatus.CONFIRMED, booking.getCar().getBrand() + " " + booking.getCar().getModel());
                emailService.sendBookingCancellationEmail(booking.getAccount().getEmail(), ERole.CAR_OWNER,
                        EBookingStatus.CONFIRMED, booking.getCar().getBrand() + " " + booking.getCar().getModel());
            }catch (MessagingException e) {
                throw new AppException(ErrorCode.SEND_SYSTEM_CANCEL_BOOKING_EMAIL_FAIL);
            }
            // Refund 70% of the deposit amount to the customer's wallet
            walletCustomer.setBalance((long) (booking.getDeposit() * 0.7) + walletCustomer.getBalance());
            // Deduct the refunded amount from the admin's wallet
            walletAdmin.setBalance(walletAdmin.getBalance() - (long) (booking.getDeposit() * 0.7));
        }

        // Update the booking status to CANCELLED
        booking.setStatus(EBookingStatus.CANCELLED);
        bookingRepository.saveAndFlush(booking);

        // Return the updated booking details
        return buildBookingResponse(booking, booking.getDriverDrivingLicenseUri());
    }

    /**
     * Confirms the pick-up of a booking if it meets the necessary conditions.
     *
     * @param bookingNumber The unique booking number.
     * @return BookingResponse containing updated booking details.
     * @throws AppException If the booking is not found, access is denied, or conditions are not met.
     */
    public BookingResponse confirmPickUp(String bookingNumber) {
        // Get the currently logged-in account ID
        String accountId = SecurityUtil.getCurrentAccountId();

        // Find the booking by its booking number
        Booking booking = bookingRepository.findBookingByBookingNumber(bookingNumber);
        if (booking == null) {
            // Throw an exception if the booking does not exist
            throw new AppException(ErrorCode.BOOKING_NOT_FOUND_IN_DB);
        }

        // Ensure the logged-in user is the owner of the booking
        if (!booking.getAccount().getId().equals(accountId)) {
            // Throw an exception if the user does not have permission to confirm this booking
            throw new AppException(ErrorCode.FORBIDDEN_BOOKING_ACCESS);
        }

        // Validate if the booking is eligible for pick-up confirmation
        if (booking.getStatus() != EBookingStatus.CONFIRMED || // Must be in CONFIRMED status
                LocalDateTime.now().isBefore(booking.getPickUpTime().minusMinutes(30)) || // Cannot confirm pick-up too early
                LocalDateTime.now().isAfter(booking.getDropOffTime())) { // Cannot confirm pick-up after drop-off time
            throw new AppException(ErrorCode.BOOKING_CANNOT_PICKUP);
        }

        // Update the booking status to IN_PROGRESS to indicate the pick-up process has started
        booking.setStatus(EBookingStatus.IN_PROGRESS);

        // Save the updated booking status to the database
        bookingRepository.saveAndFlush(booking);

        // Return the updated booking details along with the driver's license URI (if applicable)
        return buildBookingResponse(booking, booking.getDriverDrivingLicenseUri());
    }

    public BookingResponse returnCar(String bookingNumber) {
        // Get the currently logged-in account ID
        String accountId = SecurityUtil.getCurrentAccountId();

        // Find the booking by its booking number
        Booking booking = bookingRepository.findBookingByBookingNumber(bookingNumber);
        if (booking == null) {
            // Throw an exception if the booking does not exist
            throw new AppException(ErrorCode.BOOKING_NOT_FOUND_IN_DB);
        }

        // Ensure the logged-in user is the owner of the booking
        if (!booking.getAccount().getId().equals(accountId)) {
            // Throw an exception if the user does not have permission to confirm this booking
            throw new AppException(ErrorCode.FORBIDDEN_BOOKING_ACCESS);
        }
        // Validate if the booking is eligible for pick-up confirmation
        if (booking.getStatus() != EBookingStatus.IN_PROGRESS || // Must be in CONFIRMED status
                LocalDateTime.now().isAfter(booking.getDropOffTime())) { // Cannot confirm pick-up after drop-off time
            throw new AppException(ErrorCode.BOOKING_CANNOT_PICKUP);
        }
        // Retrieve the user's wallet, throw an exception if not found.
        Wallet walletCustomer = walletRepository.findById(accountId)
                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND_IN_DB));
        // Get the admin account ID (assuming the admin has roleId = 3)
        String accountIdAdmin = accountRepository.findByRoleId(3).getId();
        Wallet walletAdmin = walletRepository.findById(accountIdAdmin)
                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND_IN_DB));
        String accountIdCarOwner = booking.getCar().getAccount().getId();
        Wallet walletCarOwner = walletRepository.findById(accountIdCarOwner)
                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND_IN_DB));
        // Return the updated booking details along with the driver's license URI (if applicable)
        BookingResponse response = buildBookingResponse(booking, booking.getDriverDrivingLicenseUri());
        long totalPayment = response.getTotalPrice();
        if (booking.getDeposit() >= totalPayment) {
            long remainingMoney = booking.getDeposit() - totalPayment;

            walletCustomer.setBalance(walletCustomer.getBalance() + remainingMoney);
            walletAdmin.setBalance(walletAdmin.getBalance() - remainingMoney);
            // Calculate the shares for car owner and admin
            long carOwnerShare = (long) (0.92 * totalPayment);
            long adminShare = totalPayment - carOwnerShare; // 8% of the total payment
            // Update wallet balances
            walletCarOwner.setBalance(walletCarOwner.getBalance() + carOwnerShare);
            walletAdmin.setBalance(walletAdmin.getBalance() + adminShare);
            booking.setStatus(EBookingStatus.COMPLETED);
            try {
                emailService.sendPaymentEmail(
                        booking.getAccount().getEmail(), ERole.CUSTOMER, booking.getBookingNumber(), remainingMoney, "REFUND");

                emailService.sendPaymentEmail(
                        booking.getCar().getAccount().getEmail(), ERole.CAR_OWNER, booking.getBookingNumber(), carOwnerShare, "COMPLETED");
            }
            catch (MessagingException e) {
                throw new AppException(ErrorCode.SEND_COMPLETED_BOOKING_EMAIL_FAIL);
            }
        } else {
            long paymentMoney = totalPayment - booking.getDeposit();
            if (walletCustomer.getBalance() >= paymentMoney) {
                walletCustomer.setBalance(walletCustomer.getBalance() - paymentMoney);
                booking.setStatus(EBookingStatus.COMPLETED);
                long carOwnerShare = (long) (0.92 * totalPayment);
                long adminShare = totalPayment - carOwnerShare; // 8% of the total payment
                // Update wallet balances
                walletCarOwner.setBalance(walletCarOwner.getBalance() + carOwnerShare);
                walletAdmin.setBalance(walletAdmin.getBalance() + adminShare);
                try {
                    emailService.sendPaymentEmail(
                            booking.getAccount().getEmail(), ERole.CUSTOMER, booking.getBookingNumber(), paymentMoney, "DEDUCT");

                    emailService.sendPaymentEmail(
                            booking.getCar().getAccount().getEmail(), ERole.CAR_OWNER, booking.getBookingNumber(), carOwnerShare, "COMPLETED");
                }
                catch (MessagingException e) {
                    throw new AppException(ErrorCode.SEND_COMPLETED_BOOKING_EMAIL_FAIL);
                }
            } else {
                booking.setStatus(EBookingStatus.PENDING_PAYMENT);
                try {
                    emailService.sendPaymentEmail(
                            booking.getAccount().getEmail(), ERole.CUSTOMER, booking.getBookingNumber(), paymentMoney, "PENDING_PAYMENT");
                }catch (MessagingException e) {
                    throw new AppException(ErrorCode.SEND_PENDING_PAYMENT_BOOKING_EMAIL_FAIL);
                }
            }
        }
        bookingRepository.saveAndFlush(booking); // Save the updated booking

        // Now map to BookingResponse with updated status
        return buildBookingResponse(booking, booking.getDriverDrivingLicenseUri());
    }

}

