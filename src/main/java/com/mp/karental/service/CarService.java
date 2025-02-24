package com.mp.karental.service;

import ch.qos.logback.classic.spi.IThrowableProxy;
import com.mp.karental.constant.ECarStatus;
import com.mp.karental.dto.request.AddCarRequest;
import com.mp.karental.dto.response.CarResponse;
import com.mp.karental.dto.response.ViewMyCarResponse;
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
/*
        String accountId = SecurityUtil.getCurrentAccountId(); //only get when user has login
        UserProfile userProfile = userProfileRepository.findById(accountId)
                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND_IN_DB));
        Car car = carMapper.toCar(request);
        car.setAccountId(userProfile);

        //TODO: thêm key cho car id
        // 🖇 Upload tài liệu
            String s3KeyRegistration = String.format("car/%s/documents/registration-paper", accountId);
            fileService.uploadFile(request.getRegistrationPaper(), s3KeyRegistration);
            car.setRegistrationPaperUri(fileService.getFileUrl(s3KeyRegistration));

            String s3KeyCerticipate = String.format("car/%s/documents/certicipate-of-inspection", accountId);
            fileService.uploadFile(request.getCertificateOfInspection(), s3KeyCerticipate);
            car.setCertificateOfInspectionUri(fileService.getFileUrl(s3KeyCerticipate));

            String s3KeyInsurance = String.format("car/%s/documents/insurance", accountId);
            fileService.uploadFile(request.getInsurance(), s3KeyInsurance);
            car.setInsuranceUri(fileService.getFileUrl(s3KeyInsurance));

        // 🏎 Upload hình ảnh xe
            String s3KeyImageFront = String.format("car/%s/images/front", accountId);
            fileService.uploadFile(request.getCarImageFront(), s3KeyImageFront);
            car.setCarImageFront(fileService.getFileUrl(s3KeyImageFront));

            String s3KeyImageBack = String.format("car/%s/images/back", accountId);
            fileService.uploadFile(request.getCarImageBack(), s3KeyImageBack);
            car.setCarImageBack(fileService.getFileUrl(s3KeyImageBack));

            String s3KeyImageLeft = String.format("car/%s/images/left", accountId);
            fileService.uploadFile(request.getCarImageLeft(), s3KeyImageLeft);
            car.setCarImageLeft(fileService.getFileUrl(s3KeyImageLeft));

            String s3KeyImageRight = String.format("car/%s/images/right", accountId);
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
  */

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

        //sau khi upload xong rồi set luôn như m làm cũng dược thôi, đỡ bị loạn, code cũng tường minh hơn
        //nhưng t muốn giới thiệu thêm 1 cách nữa
        //đó là set sau khi up load hêt các file xong
        //vì nếu có file nào upload không thành công
        //method này sẽ roll back hết
        //set trước như thế thì sẽ không tối ưu lắm
        //còn m muốn dùng như nào thì tùy m, t ko bắt ép
        car.setRegistrationPaperUri(s3KeyRegistration);
        car.setCertificateOfInspectionUri(s3KeyCerticipate);
        car.setInsuranceUri(s3KeyInsurance);

        car.setCarImageFront(s3KeyImageFront);
        car.setCarImageBack(s3KeyImageBack);
        car.setCarImageLeft(s3KeyImageLeft);
        car.setCarImageRight(s3KeyImageRight);

        //update lại car trong db, tại trước đó là mới lưu thông tin thường thôi, chưa có file
        //làm này có vẻ hơi loằng ngờanfg, nhưng sẽ đảm bảo được tính integrity của dữ liệu
        car = carRepository.save(car);

        return carMapper.toCarResponse(car);
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
//        Page<Car> carsWithoutUserProfile = cars.map(car ->
//                new Car(
//                        car.getId(),
//                        car.getLicensePlate(),
//                        car.getBrand(),
//                        car.getModel(),
//                        car.getStatus(),
//                        car.getColor(),
//                        car.getNumberOfSeats(),
//                        car.getProductionYear(),
//                        car.getMileage(),
//                        car.getFuelConsumption(),
//                        car.getBasePrice(),
//                        car.getDeposit(),
//                        car.getAddress(),
//                        car.getDescription(),
//                        car.getAdditionalFunction(),
//                        car.getTermOfUse(),
//                        car.isAutomatic(),
//                        car.isGasoline(),
//                        car.getRegistrationPaperUri(),
//                        car.isRegistrationPaperUriIsVerified(),
//                        car.getCertificateOfInspectionUri(),
//                        car.isCertificateOfInspectionUriIsVerified(),
//                        car.getInsuranceUri(),
//                        car.isInsuranceUriIsVerified(),
//                        car.getCarImageFront(),
//                        car.getCarImageBack(),
//                        car.getCarImageLeft(),
//                        car.getCarImageRight(),
//                        null // Set UserProfile to null to tránh vòng lặp
//                )
//        );


        //cách dùng builder
        //tại sao dùng builder, đơn giản là vì, cái constructor kia chỉ cần 1 trường là null, hoặc viết sai thứ tự
        //là nó không chạy được
        //thêm vào đó, sau mình còn sửa đổi thông tin trả về, thay đổi constructor rất khó

        //MÀ FRONT END CẦN GÌ THÌ TRẢ VỀ NẤY THÔI, trả về 1 cái object to đùng như trên làm gì
        //thống nhất lại với thanh xem là cần những trường gì, ở đây t làm mẫu thôi

        //sau này đảm bảo cái function này phải sửa lại
        //bởi vì thằng Car nó là POJO, nó đâu có sẵn mấy cái thuộc tính như rate, noOfRide đâu
        //HỌC THÊM VỀ REQUEST VỚI RESPONSE DTO, đừng có học qua loa, phải hiểu tại sao dùng nó
        //t nhớ là t đã nói rồi
        // t chán nói lại
        Page<Car> carsWithoutUserProfile = cars.map(car -> {
                    return  Car.builder()
                            .id(car.getId())
                            .status(car.getStatus())
                            .mileage(car.getMileage())
                            .basePrice(car.getBasePrice())
                            .address(car.getAddress())
                            .carImageFront(fileService.getFileUrl(car.getCarImageFront())) //get url of the car front image
                            .carImageRight(fileService.getFileUrl(car.getCarImageRight()))
                            .carImageLeft(fileService.getFileUrl(car.getCarImageLeft()))
                            .carImageBack(fileService.getFileUrl(car.getCarImageBack()))
                            .build();
                }
        );

        return new ViewMyCarResponse(carsWithoutUserProfile);
    }


}
