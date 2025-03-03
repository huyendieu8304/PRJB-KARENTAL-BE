package com.mp.karental.service;

import com.mp.karental.constant.ECarStatus;
import com.mp.karental.dto.request.AddCarRequest;
import com.mp.karental.dto.request.EditCarRequest;
import com.mp.karental.dto.response.CarResponse;
import com.mp.karental.dto.response.CarThumbnailResponse;
import com.mp.karental.entity.Account;
import com.mp.karental.entity.Car;
import com.mp.karental.exception.AppException;
import com.mp.karental.exception.ErrorCode;
import com.mp.karental.mapper.CarMapper;
import com.mp.karental.repository.AccountRepository;
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
import org.springframework.web.multipart.MultipartFile;

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
    private final AccountRepository accountRepository;

    /**
     * Adds a new car to the system.
     *
     * @param request The request object containing car details.
     * @return The response object containing the newly added car details.
     * @throws AppException If the account is not found in the database.
     */
    public CarResponse addNewCar(AddCarRequest request) throws AppException {
        // Get the current user account Id
        String accountId = SecurityUtil.getCurrentAccountId();
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND_IN_DB));

        // Map request to car entity
        Car car = carMapper.toCar(request);
        car.setAccount(account);
        car.setStatus(ECarStatus.AVAILABLE.name());
        car.setAutomatic(request.isAutomatic());
        car.setGasoline(request.isGasoline());
        // Set car address components
        setCarAddress(request, car);
        car = carRepository.save(car);

        // Process car and upload files using the common method
        car = processUploadFiles(request, accountId, car);

        // Save the new car entity in the database
        car = carRepository.save(car);
        CarResponse carResponse = carMapper.toCarResponse(car);
        carResponse.setAddress(request.getAddress());
        carResponse.setId(car.getId());
        // Return the response after saving
        return carResponse;
    }

    /**
     * Edits an existing car's details.
     *
     * @param request The request object containing the updated car details.
     * @param id The ID of the car to be edited.
     * @return The response object containing the updated car details.
     * @throws AppException If the account or car is not found in the database.
     */
    public CarResponse editCar(EditCarRequest request, String id) throws AppException {
        // Get the current user account Id
        String accountId = SecurityUtil.getCurrentAccountId(); // Ensure user is logged in
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND_IN_DB));

        Car car = carRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.CAR_NOT_FOUND_IN_DB));

        carMapper.editCar(car, request);

        String status = request.getStatus();
        if (status == null) {
            status = ECarStatus.AVAILABLE.name();  // Default to AVAILABLE if status is null
        } else {
            status = status.toUpperCase();
        }
        car.setStatus(status.toUpperCase());

        car.setAccount(account);

        setCarAddress(request, car);

        car = processUploadFiles(request, accountId, car);

        car = carRepository.save(car);

        CarResponse carResponse = carMapper.toCarResponse(car);
        carResponse.setAddress(request.getAddress());
        carResponse.setId(car.getId());

        return carResponse;
    }

    /**
     * Extracts and sets the address components of a car from the request object.
     *
     * @param request The request object containing the address string.
     * @param car The car entity to which the address will be assigned.
     * @throws AppException If the address format is incorrect.
     */
    public void setCarAddress(Object request, Car car) throws AppException {
        String address = null;
        // Check if the request is an instance of AddCarRequest
        if (request instanceof AddCarRequest) {
            address = ((AddCarRequest) request).getAddress();
        }
        // Check if the request is an instance of EditCarRequest
        else if (request instanceof EditCarRequest) {
            address = ((EditCarRequest) request).getAddress();
        }

        if (address != null && !address.isEmpty()) {
            String[] addressParts = address.split(",",4);

            car.setCityProvince(addressParts[0].trim());
            car.setDistrict(addressParts[1].trim());
            car.setWard(addressParts[2].trim());
            car.setHouseNumberStreet(addressParts[3].trim());

        }
    }
    /**
     * Handles file uploads for a car and assigns the corresponding URIs.
     *
     * @param request The request object containing the uploaded files.
     * @param accountId The ID of the account uploading the files.
     * @param car The car entity to which the file URIs will be assigned.
     * @return The updated car entity with assigned file URIs.
     * @throws AppException If file upload fails.
     */
    public Car processUploadFiles(Object request, String accountId, Car car) throws AppException {

        // Generate base URIs for document and images
        String baseDocumentsUri = String.format("car/%s/%s/documents/", accountId, car.getId());
        String baseImagesUri = String.format("car/%s/%s/images/", accountId, car.getId());

        // Upload documents
        String s3KeyImageFront = "";
        String s3KeyImageBack = "";
        String s3KeyImageLeft = "";
        String s3KeyImageRight = "";

        MultipartFile registrationPaper = null;
        MultipartFile certificateOfInspection = null;
        MultipartFile insurance = null;
        MultipartFile imageFront = null;
        MultipartFile imageBack = null;
        MultipartFile imageLeft = null;
        MultipartFile imageRight = null;
        if(request instanceof AddCarRequest) {
            registrationPaper = ((AddCarRequest) request).getRegistrationPaper();
            certificateOfInspection = ((AddCarRequest) request).getCertificateOfInspection();
            insurance = ((AddCarRequest) request).getInsurance();
            imageFront = ((AddCarRequest) request).getCarImageFront();
            imageBack = ((AddCarRequest) request).getCarImageBack();
            imageLeft = ((AddCarRequest) request).getCarImageLeft();
            imageRight = ((AddCarRequest) request).getCarImageRight();

            String s3KeyRegistration = baseDocumentsUri + "registration-paper" + fileService.getFileExtension(registrationPaper);
            String s3KeyCerticipate = baseDocumentsUri + "certicipate-of-inspection" + fileService.getFileExtension(certificateOfInspection);
            String s3KeyInsurance = baseDocumentsUri + "insurance" + fileService.getFileExtension(insurance);

            fileService.uploadFile(registrationPaper, s3KeyRegistration);
            fileService.uploadFile(certificateOfInspection, s3KeyCerticipate);
            fileService.uploadFile(insurance, s3KeyInsurance);

            car.setRegistrationPaperUri(s3KeyRegistration);
            car.setCertificateOfInspectionUri(s3KeyCerticipate);
            car.setInsuranceUri(s3KeyInsurance);
        }
        if(request instanceof EditCarRequest) {
            imageFront = ((EditCarRequest) request).getCarImageFront();
            imageBack = ((EditCarRequest) request).getCarImageBack();
            imageLeft = ((EditCarRequest) request).getCarImageLeft();
            imageRight = ((EditCarRequest) request).getCarImageRight();
        }

        s3KeyImageFront = baseImagesUri + "front" + fileService.getFileExtension(imageFront);
        s3KeyImageBack = baseImagesUri + "back" + fileService.getFileExtension(imageBack);
        s3KeyImageLeft = baseImagesUri + "left" + fileService.getFileExtension(imageLeft);
        s3KeyImageRight = baseImagesUri + "right" + fileService.getFileExtension(imageRight);

        fileService.uploadFile(imageFront, s3KeyImageFront);
        fileService.uploadFile(imageBack, s3KeyImageBack);
        fileService.uploadFile(imageLeft, s3KeyImageLeft);
        fileService.uploadFile(imageRight, s3KeyImageRight);

        // Set file URIs in car object
        car.setCarImageFront(s3KeyImageFront);
        car.setCarImageBack(s3KeyImageBack);
        car.setCarImageLeft(s3KeyImageLeft);
        car.setCarImageRight(s3KeyImageRight);

        return car;
    }

    /**
     * Retrieves a paginated list of cars belonging to the current user.
     *
     * @param page The page number.
     * @param size The number of cars per page.
     * @param sort The sorting criteria in the format "field,direction".
     * @return A paginated list of car thumbnails.
     */
    public Page<CarThumbnailResponse> getCarsByUserId(int page, int size, String sort) {
        String accountId = SecurityUtil.getCurrentAccountId();

        // Define sort direction
        String[] sortParams = sort.split(",");
        String sortField = sortParams[0];
        Sort.Direction sortDirection = Sort.Direction.fromString(sortParams[1]);

        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortField));

        // Get list of cars
        Page<Car> cars = carRepository.findByAccountId(accountId, pageable);

        // Map cars to CarThumbnailResponse using fromCar method
        return cars.map(car -> CarThumbnailResponse.fromCar(car, fileService));
    }

}
