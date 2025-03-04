package com.mp.karental.service;

import com.mp.karental.constant.ECarStatus;
import com.mp.karental.dto.request.AddCarRequest;
import com.mp.karental.dto.response.CarDetailResponse;
import com.mp.karental.dto.response.CarResponse;
import com.mp.karental.dto.response.CarThumbnailResponse;
import com.mp.karental.entity.Account;
import com.mp.karental.entity.Car;
import com.mp.karental.exception.AppException;
import com.mp.karental.exception.ErrorCode;
import com.mp.karental.mapper.CarMapper;
import com.mp.karental.repository.AccountRepository;
import com.mp.karental.repository.BookingRepository;
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
    UserProfileRepository userProfileRepository;
    BookingRepository bookingRepository;
    private final AccountRepository accountRepository;

    public CarResponse addNewCar(AddCarRequest request) throws AppException {
        //get the current user account Id
        String accountId = SecurityUtil.getCurrentAccountId(); //only get when user has login
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND_IN_DB));
        Car car = carMapper.toCar(request);
        car.setAccount(account);
        car.setStatus(ECarStatus.AVAILABLE.name());
        String[] address = request.getAddress().split(",");
        car.setCityProvince(address[0].trim());
        car.setDistrict(address[1].trim());
        car.setWard(address[2].trim());
        car.setHouseNumberStreet(address[3].trim() + ", " + address[4].trim());

        //save car to db
        //this need to be done before upload file to s3, because the id of the car is generated in db
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
        CarResponse carResponse = carMapper.toCarResponse(car);
        return carResponse;
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
                .orElseThrow(() -> new AppException(ErrorCode.CAR_NOT_FOUND));

        // Check if the current user has booked this car
        boolean isBooked = bookingRepository.isCarBookedByAccount(carId, accountId);

        // Map the car entity to a CarDetailResponse DTO
        CarDetailResponse response = carMapper.toCarDetailResponse(car, isBooked);

        if (isBooked) {
            // If the car is booked, display the full address
            response.setAddress(car.getHouseNumberStreet() + ", "
                    + car.getWard() + ", "
                    + car.getDistrict() + ", "
                    + car.getCityProvince());

            // Allow viewing and downloading car-related documents
            response.setCertificateOfInspectionUrl(fileService.getFileUrl(car.getCertificateOfInspectionUri()));
            response.setInsuranceUrl(fileService.getFileUrl(car.getInsuranceUri()));
            response.setRegistrationPaperUrl(fileService.getFileUrl(car.getRegistrationPaperUri()));

            // Mark documents as verified
            //Phần này xem lại, tại sao lại set cho nó true, làm gì được quyền set ở đây, verified hay kooong thì phải xem trong db chuws
            //thường thì trong db nó là true, nhưng mà cũng kodđược để true luoon ở đây, mà phải get từ db ra
            response.setRegistrationPaperIsVerified(true);
            response.setCertificateOfInspectionIsVerified(true);
            response.setInsuranceIsVerified(true);
        } else {
            // nên bổ sung cmt cho else này, cho tường minh
            // Hide document URLs and show "Verified" instead
            response.setCertificateOfInspectionUrl(null);
            response.setInsuranceUrl(null);
            response.setRegistrationPaperUrl(null);

            // Display verification status only
            //tương tuwj như trn, vì hai cái sẽ giống nhau nên t đề xuất bỏ nó ra ngoài vòng if else
            response.setRegistrationPaperIsVerified(true);
            response.setCertificateOfInspectionIsVerified(true);
            response.setInsuranceIsVerified(true);

            // Provide a partial address with a message for unbooked cars
            //nếu mà viết như này thì thằng nào book rồi sẽ ko thấy address luôn à
            response.setAddress(car.getDistrict() + ", " + car.getCityProvince()
                    + " (Full address will be available after you've paid the deposit to rent).");
        }

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




}
