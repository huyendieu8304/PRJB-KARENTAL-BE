package com.mp.karental.service;

import com.mp.karental.constant.ECarStatus;
import com.mp.karental.dto.request.AddCarRequest;
import com.mp.karental.dto.response.CarResponse;
import com.mp.karental.dto.response.ViewMyCarResponse;
import com.mp.karental.entity.Car;
import com.mp.karental.exception.AppException;
import com.mp.karental.exception.ErrorCode;
import com.mp.karental.mapper.CarMapper;
import com.mp.karental.repository.CarRepository;
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
 *
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

    public CarResponse addNewCar(AddCarRequest request) throws AppException {

        Car car = carMapper.toCar(request);

        // 🖇 Upload tài liệu
            String s3KeyRegistration = String.format("car/%s/documents/registration-paper", car.getAccountId());
            fileService.uploadFile(request.getRegistrationPaper(), s3KeyRegistration);
            car.setRegistrationPaperUri(fileService.getFileUrl(s3KeyRegistration));

            String s3KeyCerticipate = String.format("car/%s/documents/certicipate-of-inspection", car.getAccountId());
            fileService.uploadFile(request.getCertificateOfInspection(), s3KeyCerticipate);
            car.setCertificateOfInspectionUri(fileService.getFileUrl(s3KeyCerticipate));

            String s3KeyInsurance = String.format("car/%s/documents/insurance", car.getAccountId());
            fileService.uploadFile(request.getInsurance(), s3KeyInsurance);
            car.setInsuranceUri(fileService.getFileUrl(s3KeyInsurance));

        // 🏎 Upload hình ảnh xe
            String s3KeyImageFront = String.format("car/%s/images/front", car.getAccountId());
            fileService.uploadFile(request.getCarImageFront(), s3KeyImageFront);
            car.setCarImageFront(fileService.getFileUrl(s3KeyImageFront));

            String s3KeyImageBack = String.format("car/%s/images/back", car.getAccountId());
            fileService.uploadFile(request.getCarImageBack(), s3KeyImageBack);
            car.setCarImageBack(fileService.getFileUrl(s3KeyImageBack));

            String s3KeyImageLeft = String.format("car/%s/images/left", car.getAccountId());
            fileService.uploadFile(request.getCarImageLeft(), s3KeyImageLeft);
            car.setCarImageLeft(fileService.getFileUrl(s3KeyImageLeft));

            String s3KeyImageRight = String.format("car/%s/images/right", car.getAccountId());
            fileService.uploadFile(request.getCarImageRight(), s3KeyImageRight);
            car.setCarImageRight(fileService.getFileUrl(s3KeyImageRight));

        // Lưu vào DB
        car.setLicensePlate(request.getLicensePlate());
        car.setColor(request.getColor());
        car.setBrand(request.getBrand());
        car.setModel(request.getModel());

        car.setAdditionalFunction(request.getAdditionalFunction() );

        car.setDeposit(request.getDeposit());
        car.setMileage(request.getMileage());
        car.setBasePrice(request.getBasePrice());
        car.setAddress(request.getAddress());
        car.setDescription(request.getDescription());
        car.setFuelConsumption(request.getFuelConsumption());
        car.setNumberOfSeats(request.getNumberOfSeats());
        car.setProductionYear(request.getProductionYear());
        car.setTermOfUse(request.getTermOfUse());
        car.setStatus(ECarStatus.AVAILABLE.name());
        car.setAutomatic(request.isAutomatic());
        car.setGasoline(request.isGasoline());


        // Lưu vào database
        Car savedCar = carRepository.save(car);

        CarResponse response = carMapper.toCarResponse(savedCar);

        return response;
    }

    public ViewMyCarResponse getCarsByUserId(int page, int size, String sort) {
        String accountId = SecurityUtil.getCurrentAccountId();

        // Define sort direction

        String[] sortParams = sort.split(",");
        String sortField = sortParams[0];
        Sort.Direction sortDirection = Sort.Direction.fromString(sortParams[1]);

        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortField));

        // Get list cars
        Page<Car> cars = carRepository.findByAccountId(accountId, pageable);

        // Ánh xạ dữ liệu để loại bỏ UserProfile
        Page<Car> carsWithoutUserProfile = cars.map(car ->
                new Car(
                        car.getId(),
                        car.getLicensePlate(),
                        car.getBrand(),
                        car.getModel(),
                        car.getStatus(),
                        car.getColor(),
                        car.getNumberOfSeats(),
                        car.getProductionYear(),
                        car.getMileage(),
                        car.getFuelConsumption(),
                        car.getBasePrice(),
                        car.getDeposit(),
                        car.getReservationPrice(),
                        car.getAddress(),
                        car.getDescription(),
                        car.getAdditionalFunction(),
                        car.getTermOfUse(),
                        car.isAutomatic(),
                        car.isGasoline(),
                        car.getRegistrationPaperUri(),
                        car.isRegistrationPaperUriIsVerified(),
                        car.getCertificateOfInspectionUri(),
                        car.isCertificateOfInspectionUriIsVerified(),
                        car.getInsuranceUri(),
                        car.isInsuranceUriIsVerified(),
                        car.getCarImageFront(),
                        car.getCarImageBack(),
                        car.getCarImageLeft(),
                        car.getCarImageRight(),
                        null // Set UserProfile to null to tránh vòng lặp
                )
        );

        return new ViewMyCarResponse(carsWithoutUserProfile);
    }



}
