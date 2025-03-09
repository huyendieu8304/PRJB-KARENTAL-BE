package com.mp.karental.service;

import com.mp.karental.constant.EBookingStatus;
import com.mp.karental.constant.ECarStatus;
import com.mp.karental.dto.request.AddCarRequest;
import com.mp.karental.dto.request.EditCarRequest;
import com.mp.karental.dto.response.CarDetailResponse;
import com.mp.karental.dto.response.CarResponse;
import com.mp.karental.dto.response.CarThumbnailResponse;
import com.mp.karental.entity.Account;
import com.mp.karental.entity.Booking;
import com.mp.karental.entity.Car;
import com.mp.karental.exception.AppException;
import com.mp.karental.exception.ErrorCode;
import com.mp.karental.mapper.CarMapper;
import com.mp.karental.repository.BookingRepository;
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

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * Service class for handling car operations.
 *
 * @author QuangPM20, AnhPH9
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
    BookingRepository bookingRepository;

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

        // Retrieve the account from the database, throw an exception if not found
        Account account = SecurityUtil.getCurrentAccount();

        // Map the request data to a Car entity
        Car car = carMapper.toCar(request);

        // Associate the car with the current account
        car.setAccount(account);

        // Set default status for the new car
        car.setStatus(ECarStatus.NOT_VERIFIED);

        // Set transmission and fuel type based on request
        car.setAutomatic(request.isAutomatic());
        car.setGasoline(request.isGasoline());

        // Set car address components from request
        setCarAddress(request, car);

        // Save the initial car entity in the database
        car = carRepository.save(car);

        // Process and upload car-related files
        car = processUploadFiles(request, accountId, car);

        // Save the updated car entity after processing files
        car = carRepository.save(car);

        // Map the saved car entity to a response object
        CarResponse carResponse = carMapper.toCarResponse(car);

        // Set address information in the response
        carResponse.setAddress(request.getAddress());

        // Set car ID in the response
        carResponse.setId(car.getId());

        // Generate and set file URLs for the response
        setCarResponseUrls(carResponse, car);

        // Return the response with the saved car details
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
        // Retrieve the current user account ID to ensure the user is logged in
        String accountId = SecurityUtil.getCurrentAccountId();

        // Retrieve the car entity from the database using the provided car ID
        // If the car does not exist, throw an exception
        Car car = carRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.CAR_NOT_FOUND_IN_DB));

        // Ensure that the currently logged-in user is the owner of the car
        // Prevents unauthorized users from modifying someone else's car
        if (!car.getAccount().getId().equals(accountId)) {
            throw new AppException(ErrorCode.FORBIDDEN_CAR_ACCESS);
        }

        // Update the car details using the request data
        carMapper.editCar(car, request);

        // Update the car's status
        String status = request.getStatus();
        if (status == null) {
            status = car.getStatus().name(); // Keep the existing status if none is provided
        }
        car.setStatus(ECarStatus.valueOf(status)); // Convert status to uppercase for consistency

        // Update the car's address details
        setCarAddress(request, car);

        // Process and upload any new files associated with the car
        car = processUploadFiles(request, accountId, car);

        // Save the updated car details in the database
        car = carRepository.save(car);

        // Convert the updated car entity into a response object
        CarResponse carResponse = carMapper.toCarResponse(car);

        // Set the address in the response object
        carResponse.setAddress(request.getAddress());

        // Generate and attach file URLs to the response
        setCarResponseUrls(carResponse, car);

        // Return the updated car response
        return carResponse;
    }


    /**
     * Set URLs for car response to avoid duplication.
     */
    private void setCarResponseUrls(CarResponse response, Car car) {
        response.setRegistrationPaperUrl(fileService.getFileUrl(car.getRegistrationPaperUri()));
        response.setCertificateOfInspectionUrl(fileService.getFileUrl(car.getCertificateOfInspectionUri()));
        response.setInsuranceUrl(fileService.getFileUrl(car.getInsuranceUri()));
        response.setCarImageFrontUrl(fileService.getFileUrl(car.getCarImageFront()));
        response.setCarImageBackUrl(fileService.getFileUrl(car.getCarImageBack()));
        response.setCarImageLeftUrl(fileService.getFileUrl(car.getCarImageLeft()));
        response.setCarImageRightUrl(fileService.getFileUrl(car.getCarImageRight()));
    }

    /**
     * Extracts and sets the address components of a car from the request object.
     *
     * @param request The request object containing the address string.
     * @param car The car entity to which the address will be assigned.
     * @throws AppException If the address format is incorrect.
     */
    private void setCarAddress(Object request, Car car) throws AppException {
        String address = null;

        // Check if the request is an instance of AddCarRequest and retrieve the address
        if (request instanceof AddCarRequest) {
            address = ((AddCarRequest) request).getAddress();
        }
        // Check if the request is an instance of EditCarRequest and retrieve the address
        else if (request instanceof EditCarRequest) {
            address = ((EditCarRequest) request).getAddress();
        }

        // If an address is provided and is not empty, split it into components
        if (address != null && !address.isEmpty()) {
            // Split the address into 4 parts: City/Province, District, Ward, House Number & Street
            String[] addressParts = address.split(",", 4);

            // Assign each part to the corresponding car attributes, ensuring to trim whitespace
            car.setCityProvince(addressParts[0].trim());   // First part: City/Province
            car.setDistrict(addressParts[1].trim());       // Second part: District
            car.setWard(addressParts[2].trim());           // Third part: Ward
            car.setHouseNumberStreet(addressParts[3].trim()); // Fourth part: House Number & Street
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
    private Car processUploadFiles(Object request, String accountId, Car car) throws AppException {

        // Generate base URIs for storing documents and images in S3
        String baseDocumentsUri = String.format("car/%s/%s/documents/", accountId, car.getId());
        String baseImagesUri = String.format("car/%s/%s/images/", accountId, car.getId());

        // Initialize file storage keys for car images
        String s3KeyImageFront = "";
        String s3KeyImageBack = "";
        String s3KeyImageLeft = "";
        String s3KeyImageRight = "";

        // Initialize document and image file variables
        MultipartFile registrationPaper = null;
        MultipartFile certificateOfInspection = null;
        MultipartFile insurance = null;
        MultipartFile imageFront = null;
        MultipartFile imageBack = null;
        MultipartFile imageLeft = null;
        MultipartFile imageRight = null;

        // If the request is an AddCarRequest, handle document and image uploads
        if (request instanceof AddCarRequest) {
            registrationPaper = ((AddCarRequest) request).getRegistrationPaper();
            certificateOfInspection = ((AddCarRequest) request).getCertificateOfInspection();
            insurance = ((AddCarRequest) request).getInsurance();
            imageFront = ((AddCarRequest) request).getCarImageFront();
            imageBack = ((AddCarRequest) request).getCarImageBack();
            imageLeft = ((AddCarRequest) request).getCarImageLeft();
            imageRight = ((AddCarRequest) request).getCarImageRight();

            // Construct S3 keys for document files
            String s3KeyRegistration = baseDocumentsUri + "registration-paper" + fileService.getFileExtension(registrationPaper);
            String s3KeyCertificate = baseDocumentsUri + "certificate-of-inspection" + fileService.getFileExtension(certificateOfInspection);
            String s3KeyInsurance = baseDocumentsUri + "insurance" + fileService.getFileExtension(insurance);

            s3KeyImageFront = baseImagesUri + "front" + fileService.getFileExtension(imageFront);
            s3KeyImageBack = baseImagesUri + "back" + fileService.getFileExtension(imageBack);
            s3KeyImageLeft = baseImagesUri + "left" + fileService.getFileExtension(imageLeft);
            s3KeyImageRight = baseImagesUri + "right" + fileService.getFileExtension(imageRight);

            // Upload document files to S3
            fileService.uploadFile(registrationPaper, s3KeyRegistration);
            fileService.uploadFile(certificateOfInspection, s3KeyCertificate);
            fileService.uploadFile(insurance, s3KeyInsurance);

            fileService.uploadFile(imageFront, s3KeyImageFront);
            fileService.uploadFile(imageBack, s3KeyImageBack);
            fileService.uploadFile(imageLeft, s3KeyImageLeft);
            fileService.uploadFile(imageRight, s3KeyImageRight);

            // Set document URIs in the car object
            car.setRegistrationPaperUri(s3KeyRegistration);
            car.setCertificateOfInspectionUri(s3KeyCertificate);
            car.setInsuranceUri(s3KeyInsurance);
            // Set image URIs in the car object
            car.setCarImageFront(s3KeyImageFront);
            car.setCarImageBack(s3KeyImageBack);
            car.setCarImageLeft(s3KeyImageLeft);
            car.setCarImageRight(s3KeyImageRight);
        }

        // If the request is an EditCarRequest, only update image files
        if (request instanceof EditCarRequest) {
            if (((EditCarRequest) request).getCarImageFront() != null && !((EditCarRequest) request).getCarImageFront().isEmpty()) {
                s3KeyImageFront = baseImagesUri + "front" + fileService.getFileExtension(((EditCarRequest) request).getCarImageFront());
                fileService.uploadFile(((EditCarRequest) request).getCarImageFront(), s3KeyImageFront);
                car.setCarImageFront(s3KeyImageFront);
            }
            if (((EditCarRequest) request).getCarImageBack() != null && !((EditCarRequest) request).getCarImageBack().isEmpty()) {
                s3KeyImageBack = baseImagesUri + "back" + fileService.getFileExtension(((EditCarRequest) request).getCarImageBack());
                fileService.uploadFile(((EditCarRequest) request).getCarImageBack(), s3KeyImageBack);
                car.setCarImageBack(s3KeyImageBack);
            }
            if (((EditCarRequest) request).getCarImageLeft() != null && !((EditCarRequest) request).getCarImageLeft().isEmpty()) {
                s3KeyImageLeft = baseImagesUri + "left" + fileService.getFileExtension(((EditCarRequest) request).getCarImageLeft());
                fileService.uploadFile(((EditCarRequest) request).getCarImageLeft(), s3KeyImageLeft);
                car.setCarImageLeft(s3KeyImageLeft);
            }
            if (((EditCarRequest) request).getCarImageRight() != null && !((EditCarRequest) request).getCarImageRight().isEmpty()) {
                s3KeyImageRight = baseImagesUri + "right" + fileService.getFileExtension(((EditCarRequest) request).getCarImageRight());
                fileService.uploadFile(((EditCarRequest) request).getCarImageRight(), s3KeyImageRight);
                car.setCarImageRight(s3KeyImageRight);
            }
        }

        return car; // Return the updated car entity with assigned file URIs
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
        // Map cars to CarThumbnailResponse using fromCar method
        Page<Car> cars = carRepository.findByAccountId(accountId, pageable);
        Page<CarThumbnailResponse> responses = cars.map(car -> {
            CarThumbnailResponse response = carMapper.toCarThumbnailResponse(car);
            response.setAddress(car.getWard() + ", " + car.getCityProvince());

            response.setCarImageFront(fileService.getFileUrl(car.getCarImageFront()));
            response.setCarImageBack(fileService.getFileUrl(car.getCarImageBack()));
            response.setCarImageLeft(fileService.getFileUrl(car.getCarImageLeft()));
            response.setCarImageRight(fileService.getFileUrl(car.getCarImageRight()));

            return response;
        });

        return responses;
    }

    /**
     * Retrieves detailed information of a car by its ID.
     *
     * @param carId The unique identifier of the car.
     * @return CarDetailResponse containing detailed information of the car.
     * @throws AppException if the car is not found.
     */
    public CarDetailResponse getCarDetail(String carId) {
        // Get the currently authenticated account ID
        String accountId = SecurityUtil.getCurrentAccountId();

        // Retrieve the car entity from the repository, throw an exception if not found
        Car car = carRepository.findById(carId)
                .orElseThrow(() -> new AppException(ErrorCode.CAR_NOT_FOUND_IN_DB));

        // Check if the current user has booked this car
        boolean isBooked = bookingRepository.isCarBookedByAccount(carId, accountId);

        // Map the car entity to a CarDetailResponse DTO
        CarDetailResponse response = carMapper.toCarDetailResponse(car, isBooked);

        if (isBooked) {
            // If the booking_status is COMPLETE, display the full address
            response.setAddress(car.getHouseNumberStreet() + ", "
                    + car.getWard() + ", "
                    + car.getDistrict() + ", "
                    + car.getCityProvince());

            // Allow viewing and downloading car-related documents
            response.setCertificateOfInspectionUrl(fileService.getFileUrl(car.getCertificateOfInspectionUri()));
            response.setInsuranceUrl(fileService.getFileUrl(car.getInsuranceUri()));
            response.setRegistrationPaperUrl(fileService.getFileUrl(car.getRegistrationPaperUri()));

        } else {
            // If the booking_status is not COMPLETE, hide document URLs and provide a partial address
            // Hide document URLs and show "Verified" instead
            response.setCertificateOfInspectionUrl(null);
            response.setInsuranceUrl(null);
            response.setRegistrationPaperUrl(null);

            // Provide a partial address with a message for unbooked cars
            response.setAddress(car.getDistrict() + ", " + car.getCityProvince()
                    + " (Full address will be available after you've paid the deposit to rent).");
        }

        // Retrieve verification status from the database instead of setting it to true
        response.setRegistrationPaperIsVerified(car.isRegistrationPaperUriIsVerified());
        response.setCertificateOfInspectionIsVerified(car.isCertificateOfInspectionUriIsVerified());
        response.setInsuranceIsVerified(car.isInsuranceUriIsVerified());

        // Retrieve and set the URLs for car-related images (these should always be visible)
        response.setCarImageFront(fileService.getFileUrl(car.getCarImageFront()));
        response.setCarImageBack(fileService.getFileUrl(car.getCarImageBack()));
        response.setCarImageLeft(fileService.getFileUrl(car.getCarImageLeft()));
        response.setCarImageRight(fileService.getFileUrl(car.getCarImageRight()));

        // Count the number of completed bookings for this car and set it
        long noOfRides = bookingRepository.countCompletedBookingsByCar(carId);
        response.setNoOfRides(noOfRides);

        return response;
    }

    /**
     * Retrieves a car by its ID and ensures that the current logged-in user owns the car.
     *
     * @param id The ID of the car to retrieve.
     * @return A CarResponse object containing the car details.
     * @throws AppException If the account is not found, the car is not found, or the user is unauthorized.
     */
    public CarResponse getCarById(String id) {
        // Retrieve the current user account ID to ensure the user is logged in
        String accountId = SecurityUtil.getCurrentAccountId();

        // Fetch the car details from the database, or throw an error if the car is not found
        Car car = carRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.CAR_NOT_FOUND_IN_DB));

        // Check if the car belongs to the current logged-in user
        if (!car.getAccount().getId().equals(accountId)) {
            throw new AppException(ErrorCode.FORBIDDEN_CAR_ACCESS); // Custom error for unauthorized access
        }

        // Convert Car entity to CarResponse DTO
        CarResponse carResponse = carMapper.toCarResponse(car);

        // Concatenate address fields into a single string and set it in CarResponse
        carResponse.setAddress(car.getCityProvince() + ", " + car.getDistrict() + ", "
                + car.getWard() + ", " + car.getHouseNumberStreet());

        // Set file URLs for car images and documents
        setCarResponseUrls(carResponse, car);

        return carResponse;
    }

    /**
     * Checks if a car is available within the given time range.
     *
     * @param carId      the ID of the car
     * @param pickUpTime the start time of the requested booking
     * @param dropOffTime the end time of the requested booking
     * @return true if the car is available, false otherwise
     */
    public boolean isCarAvailable(String carId, LocalDateTime pickUpTime, LocalDateTime dropOffTime) {
        // get list booking in range (pickUp - 1 day) to (dropOff + 1 day)
        LocalDateTime searchStart = pickUpTime.minusDays(1);
        LocalDateTime searchEnd = dropOffTime.plusDays(1);

        List<Booking> bookings = bookingRepository.findActiveBookingsByCarIdAndTimeRange(carId, searchStart, searchEnd);
        log.info("Checking availability for Car ID: {} - Search range: {} to {}", carId, searchStart, searchEnd);

        // If there isn't any booking -> Available
        if (bookings.isEmpty()) {
            return true;
        }
        // Check whether all booking is CANCELED OR  PENDING_DEPOSIT
        return bookings.stream()
                .allMatch(booking -> booking.getStatus() == EBookingStatus.CANCELLED
                        || booking.getStatus() == EBookingStatus.PENDING_DEPOSIT);
    }


    /**
     * Checks if a car is booked by customer.
     * @param carId the ID of the car
     * @param accountId the ID of the customer
     * @return true if the car is booked by account ID, false otherwise
     */
    public boolean isCarBooked(String carId, String accountId) {
        List<EBookingStatus> activeStatuses = Arrays.asList(
                EBookingStatus.CONFIRMED,
                EBookingStatus.IN_PROGRESS,
                EBookingStatus.PENDING_PAYMENT,
                EBookingStatus.COMPLETED
        );

        return bookingRepository.existsByCarIdAndAccountIdAndBookingStatusIn(carId, accountId, activeStatuses);
    }



}
