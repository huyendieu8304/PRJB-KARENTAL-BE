package com.mp.karental.service;

import com.mp.karental.constant.EBookingStatus;
import com.mp.karental.constant.EPaymentType;
import com.mp.karental.dto.request.BookingRequest;
import com.mp.karental.dto.response.BookingResponse;
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
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

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

    public BookingResponse createBooking(BookingRequest bookingRequest, String carId) throws AppException,Exception{
        updateExpiredBookings();

        // Get the current user account Id
        String accountId = SecurityUtil.getCurrentAccountId();

        // Retrieve the account from the database, throw an exception if not found
        Account accountCustomer = SecurityUtil.getCurrentAccount();

        Car car = carRepository.findById(carId)
                .orElseThrow(() -> new AppException(ErrorCode.CAR_NOT_FOUND_IN_DB));
        Wallet walletCustomer = walletRepository.findById(accountId)
                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND_IN_DB));
        if(!carService.isCarAvailable(car.getId(), bookingRequest.getPickUpTime(), bookingRequest.getDropOffTime())){
            throw new AppException(ErrorCode.CAR_NOT_AVAILABLE);
        }

        LocalDateTime now = LocalDateTime.now();
        LocalTime nowTime = now.toLocalTime();
        LocalDate today = now.toLocalDate();
        LocalDate pickUpDate = bookingRequest.getPickUpTime().toLocalDate();
        LocalTime pickUpTime = bookingRequest.getPickUpTime().toLocalTime();
        LocalTime dropOffTime = bookingRequest.getDropOffTime().toLocalTime();
        long daysBetween = Duration.between(bookingRequest.getPickUpTime(), bookingRequest.getDropOffTime()).toDays();

        if(nowTime.isAfter(LocalTime.of(4,0)) && nowTime.isBefore(LocalTime.of(20,0))){
            if(pickUpTime.isBefore(nowTime.plusHours(2)) || pickUpDate.isAfter(today.plusDays(60))){
                throw new AppException(ErrorCode.INVALID_PICK_UP_TIME);
            }
            if(dropOffTime.isBefore(nowTime.plusHours(4)) || daysBetween > 30){
                throw new AppException(ErrorCode.INVALID_DROP_OFF_TIME);
            }
        } else if (nowTime.isAfter(LocalTime.of(20, 0)) && nowTime.isBefore(LocalTime.of(0, 0))) {
            if(pickUpTime.isBefore(LocalTime.of(6,0)) || pickUpDate.isAfter(today.plusDays(60))){
                throw new AppException(ErrorCode.INVALID_PICK_UP_TIME);
            }
            if(dropOffTime.isBefore(LocalTime.of(8,0)) || daysBetween > 30){
                throw new AppException(ErrorCode.INVALID_DROP_OFF_TIME);
            }
        } else if(nowTime.isAfter(LocalTime.of(0, 0)) && nowTime.isBefore(LocalTime.of(4, 0))){
            if(pickUpTime.isBefore(LocalTime.of(6,0)) || pickUpDate.isAfter(today.plusDays(60))){
                throw new AppException(ErrorCode.INVALID_PICK_UP_TIME);
            }
            if(dropOffTime.isBefore(LocalTime.of(8,0)) || daysBetween > 30){
                throw new AppException(ErrorCode.INVALID_DROP_OFF_TIME);
            }
        }

        Booking booking = bookingMapper.toBooking(bookingRequest);
        booking.setBookingNumber(redisUtil.generateBookingNumber());
        MultipartFile drivingLicense = bookingRequest.getDriverDrivingLicense();
        String s3Key = "user/" + accountId + "/driving-license" + fileService.getFileExtension(bookingRequest.getDriverDrivingLicense());
        fileService.uploadFile(drivingLicense, s3Key);
        booking.setDriverDrivingLicenseUri(s3Key);

        booking.setAccount(accountCustomer);
        booking.setCar(car);

        long depositAtBookingTime = car.getDeposit();
        long basePriceAtBookingTime = car.getBasePrice();

        booking.setDeposit(depositAtBookingTime);

        long minutes = Duration.between(booking.getPickUpTime(), booking.getDropOffTime()).toMinutes();

        long days = (long) Math.ceil(minutes / (24.0 * 60));

        booking.setBasePrice(basePriceAtBookingTime * days);

        booking.setCreatedAt(LocalDateTime.now());
        booking.setUpdatedAt(booking.getCreatedAt());

        if (booking.getPaymentType().equals(EPaymentType.WALLET)) {
            if (walletCustomer.getBalance() >= car.getDeposit()) {
                walletCustomer.setBalance(walletCustomer.getBalance() - car.getDeposit());
                booking.setStatus(EBookingStatus.WAITING_CONFIRM);
            }else{
                booking.setStatus(EBookingStatus.PENDING_DEPOSIT);
            }
        }
        else if (booking.getPaymentType().equals(EPaymentType.CASH) || booking.getPaymentType().equals(EPaymentType.BANK_TRANSFER)) {
            booking.setStatus(EBookingStatus.PENDING_DEPOSIT);
        }
        BookingResponse bookingResponse = bookingMapper.toBookingResponse(booking);
        bookingResponse.setDriverDrivingLicenseUrl(fileService.getFileUrl(s3Key));

        bookingResponse.setCarId(booking.getCar().getId());

        bookingRepository.save(booking);

        return bookingResponse;
    }

    @Scheduled(fixedRate = 60000)
    public void updateExpiredBookings() {
        LocalDateTime expiredTime = LocalDateTime.now().minusMinutes(2);
        List<Booking> expiredBookings = bookingRepository.findExpiredBookings(expiredTime);

        for (Booking booking : expiredBookings) {
            booking.setStatus(EBookingStatus.CANCELLED);
            booking.setUpdatedAt(LocalDateTime.now());
            bookingRepository.saveAndFlush(booking);
        }
    }
}
