package com.mp.karental.service;

import ch.qos.logback.classic.spi.IThrowableProxy;
import com.mp.karental.constant.ECarStatus;
import com.mp.karental.dto.request.AddCarRequest;
import com.mp.karental.dto.response.CarResponse;
import com.mp.karental.dto.response.CarThumbnailResponse;
import com.mp.karental.entity.Account;
import com.mp.karental.entity.Car;
import com.mp.karental.entity.UserProfile;
import com.mp.karental.exception.AppException;
import com.mp.karental.exception.ErrorCode;
import com.mp.karental.mapper.CarMapper;
import com.mp.karental.repository.AccountRepository;
import com.mp.karental.repository.CarRepository;
import com.mp.karental.repository.UserProfileRepository;
import com.mp.karental.security.SecurityUtil;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Sort;

/**
 * Service class for handling car operations.
 *
 * @author QuangPM20
 * @version 1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CarService {
    CarRepository carRepository;
    CarMapper carMapper;
    FileService fileService;
    UserProfileRepository userProfileRepository;
    private final AccountRepository accountRepository;

    public CarResponse addNewCar(AddCarRequest request) throws AppException {
        //get the current user account Id
        String accountId = SecurityUtil.getCurrentAccountId(); //only get when user has login
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND_IN_DB));
        Car car = carMapper.toCar(request);
        car.setAccount(account);
        car.setStatus(ECarStatus.AVAILABLE.name());

        //save car to db
        //this need to be done before upload file to s3, because the id of the car is generate in db
        car = carRepository.save(car);

        //upload file to cloud
        String carId = car.getId();
        //these string below to make sure less repeated code
        String baseDocumentsUri = String.format("car/%s/%s/documents", accountId, carId);
        String baseImagesUri = String.format("car/%s/%s/images", accountId, carId);

        StringBuilder sbDocuments = new StringBuilder(baseDocumentsUri);
        StringBuilder sbImanges = new StringBuilder(baseImagesUri);

        // 🖇 Upload tài liệu
        String s3KeyRegistration = sbDocuments.append("registration-paper").toString();
        fileService.uploadFile(request.getRegistrationPaper(), s3KeyRegistration);

        String s3KeyCerticipate = sbDocuments.append("certicipate-of-inspection").toString();
        fileService.uploadFile(request.getCertificateOfInspection(), s3KeyCerticipate);

        String s3KeyInsurance = sbDocuments.append("insurance").toString();
        fileService.uploadFile(request.getInsurance(), s3KeyInsurance);

        // 🏎 Upload hình ảnh xe
        String s3KeyImageFront = sbImanges.append("front").toString();
        fileService.uploadFile(request.getCarImageFront(), s3KeyImageFront);

        String s3KeyImageBack = sbImanges.append("back").toString();
        fileService.uploadFile(request.getCarImageBack(), s3KeyImageBack);

        String s3KeyImageLeft = sbImanges.append("left").toString();
        fileService.uploadFile(request.getCarImageLeft(), s3KeyImageLeft);

        String s3KeyImageRight = sbImanges.append("right").toString();
        fileService.uploadFile(request.getCarImageRight(), s3KeyImageRight);

        car.setRegistrationPaperUri(s3KeyRegistration);
        car.setCertificateOfInspectionUri(s3KeyCerticipate);
        car.setInsuranceUri(s3KeyInsurance);

        car.setCarImageFront(s3KeyImageFront);
        car.setCarImageBack(s3KeyImageBack);
        car.setCarImageLeft(s3KeyImageLeft);
        car.setCarImageRight(s3KeyImageRight);

        car.setAutomatic(request.isAutomatic());
        car.setGasoline(request.isGasoline());

        car = carRepository.save(car);

        return carMapper.toCarResponse(car);
    }

    public Page<CarThumbnailResponse> getCarsByUserId(int page, int size, String sort) {
        String accountId = SecurityUtil.getCurrentAccountId();

        // Define sort direction
        String[] sortParams = sort.split(",");
        String sortField = sortParams[0];
        Sort.Direction sortDirection = Sort.Direction.fromString(sortParams[1]);

        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortField));

        // Get list of cars
        // Map cars to CarThumbnailResponse using fromCar method
        Page<Car> cars = carRepository.findByAccountId(accountId, pageable);
        Page<CarThumbnailResponse> responses = cars.map(car -> {
            CarThumbnailResponse response = carMapper.toCarThumbnailResponse(car);
            response.setAddress(car.getWard() + ", " + car.getCityProvince());
            return response;
        });

        return responses;
    }

}
