package com.mp.karental.service;

import com.mp.karental.constant.EBookingStatus;
import com.mp.karental.constant.EPaymentType;
import com.mp.karental.dto.request.booking.CreateBookingRequest;
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
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;
import java.time.LocalDate;
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
    AccountRepository accountRepository;
    FileService fileService;
    CarService carService;
    TransactionService transactionService;
    EmailService emailService;

    // Define constant field names to avoid repetition
    private static final String FIELD_CREATED_AT = "createdAt";
    private static final String FIELD_BASE_PRICE = "basePrice";

    /**
     * Creates a new booking for a car rental.
     *
     * @param CreateBookingRequest The booking request details.
     * @return BookingResponse containing booking details.
     * @throws AppException if there are validation issues or car availability problems.
     * @throws MessagingException if booking successful or expired booking
     */
    public BookingResponse createBooking(CreateBookingRequest CreateBookingRequest) throws AppException, MessagingException {
        // Get the current logged-in user's account ID and account details
        Account accountCustomer = prepareBooking();
        String accountId = accountCustomer.getId();

        // Retrieve car details from the database, throw an exception if not found.
        Car car = carRepository.findById(CreateBookingRequest.getCarId())
                .orElseThrow(() -> new AppException(ErrorCode.CAR_NOT_FOUND_IN_DB));

        // Retrieve the user's wallet, throw an exception if not found.
        Wallet walletCustomer = walletRepository.findById(accountId)
                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND_IN_DB));

        // Check if the car is available for the requested pickup and drop-off time.
        if (!carService.isCarAvailable(car.getId(), CreateBookingRequest.getPickUpTime(), CreateBookingRequest.getDropOffTime())) {
            throw new AppException(ErrorCode.CAR_NOT_AVAILABLE);
        }

        // Map the booking request to a Booking entity.
        Booking booking = bookingMapper.toBooking(CreateBookingRequest);
        booking.setBookingNumber(redisUtil.generateBookingNumber());

        // Upload the driver's license to S3 storage.
        String s3Key = handleDriverLicense(CreateBookingRequest, booking, accountCustomer);

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
            emailService.sendBookingWaitingForConfirmationEmail(accountCustomer.getEmail(),
                    car.getBrand() + " " + car.getModel(), booking.getBookingNumber());
            emailService.sendCarOwnerConfirmationRequestEmail(car.getAccount().getEmail(),
                    car.getBrand() + " " + car.getModel(), car.getLicensePlate());
            walletRepository.save(walletCustomer);
        } else {
            booking.setStatus(EBookingStatus.PENDING_DEPOSIT);
        }
        // Save the booking to the database.
        bookingRepository.save(booking);

        return buildBookingResponse(booking, s3Key);
    }


    /**
     * Edits an existing booking based on the provided booking number and update request.
     *
     * @param EditBookingRequest The request details for updating the booking, including new car information and driver’s license.
     * @param bookingNumber The booking number of the booking to be edited.
     * @return BookingResponse containing the updated booking details.
     * @throws AppException If there are any validation issues, the booking is not found, or the user doesn’t have access to edit the booking.
     * @throws MessagingException if have any expired booking
     */
    public BookingResponse editBooking(EditBookingRequest EditBookingRequest, String bookingNumber) throws AppException, MessagingException {
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
        if(!EditBookingRequest.getCarId().equals(booking.getCar().getId())) {
            throw new AppException(ErrorCode.CAR_NOT_AVAILABLE);
        }
        //do not allow edit
        if (booking.getStatus() == EBookingStatus.IN_PROGRESS ||
                booking.getStatus() == EBookingStatus.PENDING_PAYMENT ||
                booking.getStatus() == EBookingStatus.COMPLETED ||
                booking.getStatus() == EBookingStatus.CANCELLED) {
            throw new AppException(ErrorCode.BOOKING_CANNOT_BE_EDITED);
        }

        // Update the car details using the request data
        bookingMapper.editBooking(booking, EditBookingRequest);
        String s3Key = handleDriverLicense(EditBookingRequest, booking, accountCustomer);

        booking.setDriverDrivingLicenseUri(s3Key);

        // Save the booking to the database.
        bookingRepository.saveAndFlush(booking);

        return buildBookingResponse(booking, s3Key);
    }

    /**
     * Handles the upload of the driver's license and updates the booking with the license URI.
     *
     * @param request The request object containing the driver's license details (CreateBookingRequest or EditBookingRequest).
     * @param booking The booking to be updated with the driver's license URI.
     * @param accountCustomer The account of the customer making the booking.
     * @return The S3 key where the driver's license is stored.
     * @throws AppException If the driver's license is invalid or any other error occurs during file upload.
     */
    private String handleDriverLicense(Object request, Booking booking, Account accountCustomer) throws AppException {
        MultipartFile drivingLicense;
        String s3Key = "";

        // Handle the case when the request is a CreateBookingRequest
        if (request instanceof CreateBookingRequest CreateBookingRequest) {
            drivingLicense = CreateBookingRequest.getDriverDrivingLicense();

            // Upload the driver's license to S3 storage if the driver information is provided.
            if (CreateBookingRequest.isDriver()) {
                if (drivingLicense == null || drivingLicense.isEmpty()) {
                    throw new AppException(ErrorCode.INVALID_DRIVER_INFO); // Validate driver's license is provided.
                }
                checkNullPointerDriver(CreateBookingRequest); // Ensure that the required driver fields are not null.
                s3Key = "booking/" + booking.getBookingNumber() + "/driver-driving-license"
                        + fileService.getFileExtension(CreateBookingRequest.getDriverDrivingLicense());
                fileService.uploadFile(drivingLicense, s3Key); // Upload the file to S3.
            } else {
                // Use existing license URI from account profile if the driver is not provided.
                s3Key = accountCustomer.getProfile().getDrivingLicenseUri();
                booking.setDriverFullName(accountCustomer.getProfile().getFullName()); // Set full name of the driver.
                setAccountProfileToDriver(booking, accountCustomer); // Set the profile information to the booking.
            }
        }
        // Handle the case when the request is an EditBookingRequest
        else if (request instanceof EditBookingRequest EditBookingRequest) {
            drivingLicense = EditBookingRequest.getDriverDrivingLicense();
            String existingUri = booking.getDriverDrivingLicenseUri() == null ? "" : booking.getDriverDrivingLicenseUri();

            // If the driver is provided, validate and possibly upload a new driver's license.
            if (EditBookingRequest.isDriver()) {
                if ((drivingLicense == null || drivingLicense.isEmpty()) && existingUri.startsWith("user/")) {
                    throw new AppException(ErrorCode.INVALID_DRIVER_INFO); // Validate driver information.
                }
                checkNullPointerDriver(EditBookingRequest); // Ensure that the required driver fields are not null.
                if (drivingLicense != null && !drivingLicense.isEmpty()) {
                    s3Key = "booking/" + booking.getBookingNumber() + "/driver-driving-license"
                            + fileService.getFileExtension(drivingLicense);
                    fileService.uploadFile(drivingLicense, s3Key); // Upload the file to S3.
                } else {
                    s3Key = existingUri; // If no new file, retain the existing URI.
                }
            } else {
                // Use existing license URI from account profile if the driver is not provided.
                s3Key = accountCustomer.getProfile().getDrivingLicenseUri();
                setAccountProfileToDriver(booking, accountCustomer); // Set the profile information to the booking.
            }
        }
        return s3Key; // Return the S3 key for the driver's license.
    }

    /**
     * Prepares the booking by ensuring the account is valid and the profile is complete.
     * This method updates the status of existing bookings and retrieves the current logged-in user's account details.
     * It also checks if the account's profile is complete before allowing further booking actions.
     *
     * @return The account of the currently logged-in user.
     * @throws AppException If the account's profile is incomplete or any error occurs during the booking preparation.
     * @throws MessagingException if booking expired booking or cancel booking with any reason
     */
    private Account prepareBooking() throws AppException, MessagingException {
        // Update expired bookings before creating or editing a booking
        updateStatusBookings();

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
     * @param s3Key The S3 key for the driver's license file.
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
     * Scheduled task to update expired bookings.
     * Runs every 10 seconds to check and cancel bookings that are not confirmed within 2 minutes.
     * @throws MessagingException if booking expired booking or cancel booking with any reason
     */
    @Scheduled(fixedRate = 10000)
    public void updateStatusBookings() throws MessagingException {
        LocalDateTime now = LocalDateTime.now();
        String reason = "";
        // Find bookings that have expired (not confirmed within 1 hour).
        List<Booking> expiredBookings = bookingRepository.findExpiredBookings(now.minusMinutes(2));

        // Cancel expired bookings.
        for (Booking booking : expiredBookings) {
            if (booking.getCreatedAt().plusHours(1).isBefore(now)) {
                booking.setStatus(EBookingStatus.CANCELLED);
                booking.setUpdatedAt(now);
                bookingRepository.saveAndFlush(booking);
                reason = "Your booking was automatically canceled because the deposit was not paid within 1 hour.";
                emailService.sendSystemCanceledBookingEmail(booking.getAccount().getEmail(),
                        booking.getCar().getBrand() + " " + booking.getCar().getModel(),
                        reason);
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
                    reason = "Your booking has been canceled because another customer has successfully placed a deposit for this car within the same rental period.";
                    emailService.sendSystemCanceledBookingEmail(booking.getAccount().getEmail(),
                            booking.getCar().getBrand() + " " + booking.getCar().getModel(),
                            reason);
                }

                break; // stopped when a booking change to waiting confirm
            }
        }
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

    /**
     * Checks whether the driver information in the request is complete.
     * This method checks that all the required driver details are present in the request (either a CreateBookingRequest
     * or EditBookingRequest). If any of the required fields (such as full name, phone number, national ID, etc.)
     * are missing or empty, an AppException with the error code INVALID_DRIVER_INFO is thrown.
     *
     * @param request The request object (either CreateBookingRequest or EditBookingRequest) that contains the driver's information to be validated.
     * @throws AppException If any required driver field is missing or empty, throws an AppException with the error code INVALID_DRIVER_INFO.
     */
    private void checkNullPointerDriver(Object request) {
        if (request instanceof CreateBookingRequest CreateBookingRequest) {
            validateDriverInfo(
                    CreateBookingRequest.getDriverFullName(),
                    CreateBookingRequest.getDriverPhoneNumber(),
                    CreateBookingRequest.getDriverNationalId(),
                    CreateBookingRequest.getDriverDob(),
                    CreateBookingRequest.getDriverEmail(),
                    CreateBookingRequest.getDriverCityProvince(),
                    CreateBookingRequest.getDriverDistrict(),
                    CreateBookingRequest.getDriverWard(),
                    CreateBookingRequest.getDriverHouseNumberStreet()
            );
        } else if (request instanceof EditBookingRequest EditBookingRequest) {
            validateDriverInfo(
                    EditBookingRequest.getDriverFullName(),
                    EditBookingRequest.getDriverPhoneNumber(),
                    EditBookingRequest.getDriverNationalId(),
                    EditBookingRequest.getDriverDob(),
                    EditBookingRequest.getDriverEmail(),
                    EditBookingRequest.getDriverCityProvince(),
                    EditBookingRequest.getDriverDistrict(),
                    EditBookingRequest.getDriverWard(),
                    EditBookingRequest.getDriverHouseNumberStreet()
            );
        }
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
     *
     * @throws AppException if any required field is missing or empty.
     */
    private void validateDriverInfo(String fullName, String phoneNumber, String nationalId,
                                    LocalDate dob, String email, String city, String district,
                                    String ward, String houseNumberStreet) {
        if (isNullOrEmpty(fullName) ||
                isNullOrEmpty(phoneNumber) ||
                isNullOrEmpty(nationalId) ||
                dob == null ||
                isNullOrEmpty(email) ||
                isNullOrEmpty(city) ||
                isNullOrEmpty(district) ||
                isNullOrEmpty(ward) ||
                isNullOrEmpty(houseNumberStreet)) {
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

    /**
     * Retrieves booking details by booking number and ensures the user has access to the booking.
     * This method checks the current logged-in user's account ID and compares it with the booking's account ID
     * to ensure the user has access to the booking details. If the booking is not found or the user does not have
     * permission, an exception is thrown.
     *
     * @param bookingNumber The booking number to fetch the booking details.
     * @return A BookingResponse object containing the booking details.
     * @throws AppException If the booking is not found or the user does not have permission to access the booking.
     */
    public BookingResponse getBookingDetailsByBookingNumber(String bookingNumber) {
        // Retrieve the current user account ID to ensure the user is logged in
        String accountId = SecurityUtil.getCurrentAccountId();
        // Fetch the booking details from the database, or throw an error if the booking is not found
        Booking booking = bookingRepository.findBookingByBookingNumber(bookingNumber);
        if (booking == null) {
            throw new AppException(ErrorCode.BOOKING_NOT_FOUND_IN_DB);
        }
        //the booking is of another account
        if (!booking.getAccount().getId().equals(accountId)) {
            throw new AppException(ErrorCode.FORBIDDEN_BOOKING_ACCESS);
        }
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
     * @throws MessagingException If there is an issue sending the cancellation email.
     * @throws AppException If the booking is not found, the user is unauthorized to cancel,
     *                      or the booking is in a non-cancellable state.
     */
    public BookingResponse cancelBooking(String bookingNumber) throws MessagingException {
        // Get the currently logged-in account ID
        String accountId = SecurityUtil.getCurrentAccountId();
        // Retrieve the account details of the logged-in user
        Account accountCustomer = SecurityUtil.getCurrentAccount();

        // Find the booking by booking number
        Booking booking = bookingRepository.findBookingByBookingNumber(bookingNumber);
        if (booking == null) {
            throw new AppException(ErrorCode.BOOKING_NOT_FOUND_IN_DB); // Throw an error if booking is not found
        }

        // Ensure that the booking belongs to the logged-in user
        if (!booking.getAccount().getId().equals(accountId)) {
            throw new AppException(ErrorCode.FORBIDDEN_BOOKING_ACCESS); // Throw an error if user tries to cancel someone else's booking
        }

        // Retrieve car and car owner information from the booking
        Car car = booking.getCar();
        Account accountCarOwner = car.getAccount();

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
            emailService.sendCustomerBookingCanceledEmail(accountCustomer.getEmail(),
                    booking.getCar().getBrand() + " " + booking.getCar().getModel());
        }

        // If the booking is in WAITING_CONFIRM status, refund the full deposit to the customer
        else if (booking.getStatus() == EBookingStatus.WAITING_CONFIRM) {
            // Refund the full deposit amount to the customer's wallet
            walletCustomer.setBalance(booking.getDeposit() + walletCustomer.getBalance());
            // Deduct the refunded amount from the admin's wallet
            walletAdmin.setBalance(walletAdmin.getBalance() - booking.getDeposit());

            // Send an email notification to the customer about the full refund
            emailService.sendCustomerBookingCanceledWithFullRefundEmail(accountCustomer.getEmail(),
                    booking.getCar().getBrand() + " " + booking.getCar().getModel());
        }

        // If the booking is in CONFIRMED status, refund 70% of the deposit and notify the car owner
        else if (booking.getStatus() == EBookingStatus.CONFIRMED) {
            // Refund 70% of the deposit amount to the customer's wallet
            walletCustomer.setBalance((long) (booking.getDeposit() * 0.7) + walletCustomer.getBalance());
            // Deduct the refunded amount from the admin's wallet
            walletAdmin.setBalance(walletAdmin.getBalance() - (long) (booking.getDeposit() * 0.7));

            // Send an email notification to the customer about the partial refund (70%)
            emailService.sendCustomerBookingCanceledWithPartialRefundEmail(accountCustomer.getEmail(),
                    booking.getCar().getBrand() + " " + booking.getCar().getModel());

            // Notify the car owner about the cancellation and inform them about the system's revenue share
            emailService.sendCarOwnerBookingCanceledEmail(accountCarOwner.getEmail(),
                    booking.getCar().getBrand() + " " + booking.getCar().getModel());
        }

        // Update the booking status to CANCELLED
        booking.setStatus(EBookingStatus.CANCELLED);
        bookingRepository.saveAndFlush(booking);

        // Return the updated booking details
        return buildBookingResponse(booking, booking.getDriverDrivingLicenseUri());
    }

}

