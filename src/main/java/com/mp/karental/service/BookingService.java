package com.mp.karental.service;

import com.mp.karental.constant.EBookingStatus;
import com.mp.karental.constant.EPaymentType;
import com.mp.karental.dto.request.BookingRequest;
import com.mp.karental.dto.response.BookingResponse;
import com.mp.karental.dto.response.BookingThumbnailResponse;
import com.mp.karental.dto.response.CarThumbnailResponse;
import com.mp.karental.entity.Account;
import com.mp.karental.entity.Booking;
import com.mp.karental.entity.Car;
import com.mp.karental.entity.Wallet;
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

import java.awt.print.Book;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

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

    /**
     * Creates a new booking for a car rental.
     *
     * @param bookingRequest The booking request details.
     * @param carId The ID of the car being booked.
     * @return BookingResponse containing booking details.
     * @throws AppException if there are validation issues or car availability problems.
     * @throws Exception for unexpected errors.
     */
    public BookingResponse createBooking(BookingRequest bookingRequest, String carId) throws AppException, Exception {
        // Update expired bookings before creating a new one.
        updateExpiredBookings();

        // Get the current logged-in user's account ID.
        String accountId = SecurityUtil.getCurrentAccountId();

        // Retrieve the account details of the logged-in user.
        Account accountCustomer = SecurityUtil.getCurrentAccount();

        // Retrieve car details from the database, throw an exception if not found.
        Car car = carRepository.findById(carId)
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
        String s3Key = "user/" + accountId + "/driving-license" + fileService.getFileExtension(bookingRequest.getDriverDrivingLicense());
        fileService.uploadFile(drivingLicense, s3Key);
        booking.setDriverDrivingLicenseUri(s3Key);

        // Assign account and car to the booking.
        booking.setAccount(accountCustomer);
        booking.setCar(car);

        // Store car deposit and base price at the time of booking.
        long depositAtBookingTime = car.getDeposit();
        long basePriceAtBookingTime = car.getBasePrice();

        booking.setDeposit(depositAtBookingTime);

        // Calculate rental duration in minutes.
        long minutes = Duration.between(booking.getPickUpTime(), booking.getDropOffTime()).toMinutes();
        long days = (long) Math.ceil(minutes / (24.0 * 60)); // Convert minutes to full days.

        // Set the total base price based on the rental duration.
        booking.setBasePrice(basePriceAtBookingTime * days);

        // Set timestamps for booking creation and update.
        booking.setCreatedAt(LocalDateTime.now());
        booking.setUpdatedAt(booking.getCreatedAt());

        // Handle different payment types.
        if (booking.getPaymentType().equals(EPaymentType.WALLET)) {
            // If the user has enough balance in the wallet, deduct the deposit and proceed.
            if (walletCustomer.getBalance() >= car.getDeposit()) {
                walletCustomer.setBalance(walletCustomer.getBalance() - car.getDeposit());
                booking.setStatus(EBookingStatus.WAITING_CONFIRM);
            } else {
                booking.setStatus(EBookingStatus.PENDING_DEPOSIT);
            }
        } else if (booking.getPaymentType().equals(EPaymentType.CASH) || booking.getPaymentType().equals(EPaymentType.BANK_TRANSFER)) {
            booking.setStatus(EBookingStatus.PENDING_DEPOSIT);
        }

        // Convert the booking entity to a response DTO.
        BookingResponse bookingResponse = bookingMapper.toBookingResponse(booking);
        bookingResponse.setDriverDrivingLicenseUrl(fileService.getFileUrl(s3Key));
        bookingResponse.setCarId(booking.getCar().getId());

        // Save the booking to the database.
        bookingRepository.save(booking);

        return bookingResponse;
    }

    /**
     * Scheduled task to update expired bookings.
     * Runs every 10 seconds to check and cancel bookings that are not confirmed within 2 minutes.
     */
    @Scheduled(fixedRate = 10000)
    public void updateExpiredBookings() {
        LocalDateTime now = LocalDateTime.now();

        // Find bookings that have expired (not confirmed within 2 minutes).
        List<Booking> expiredBookings = bookingRepository.findExpiredBookings(now.minusMinutes(2));

        // Cancel expired bookings.
        for (Booking booking : expiredBookings) {
            if (booking.getCreatedAt().plusMinutes(2).isBefore(now)) {
                booking.setStatus(EBookingStatus.CANCELLED);
                booking.setUpdatedAt(now);
                bookingRepository.saveAndFlush(booking);
            }
        }
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

        // Default sorting field and direction
        // Sorting by createdAt in descending order (latest first)
        String sortField = "createdAt";
        Sort.Direction sortDirection = Sort.Direction.DESC;

        // Validate and process sorting input
        if (sort != null && !sort.isEmpty()) {
            String[] sortParams = sort.split(",");
            if (sortParams.length == 2) {
                if (sortParams[0].equals("createdAt")) {
                    sortField = "createdAt"; // Only allow sorting by createdAt
                }
                try {
                    sortDirection = Sort.Direction.fromString(sortParams[1].toUpperCase());
                } catch (IllegalArgumentException e) {
                    sortDirection = Sort.Direction.DESC; // Default to DESC if the format is invalid
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

            // Retrieve car images from the file storage service
            response.setCarImageFrontUrl(fileService.getFileUrl(booking.getCar().getCarImageFront()));
            response.setCarImageBackUrl(fileService.getFileUrl(booking.getCar().getCarImageBack()));
            response.setCarImageLeftUrl(fileService.getFileUrl(booking.getCar().getCarImageLeft()));
            response.setCarImageRightUrl(fileService.getFileUrl(booking.getCar().getCarImageRight()));

            return response;
        });
    }
}
