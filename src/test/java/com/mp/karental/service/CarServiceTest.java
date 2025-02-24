package com.mp.karental.service;

import com.mp.karental.dto.request.AddCarRequest;
import com.mp.karental.dto.response.CarResponse;
import com.mp.karental.dto.response.ViewMyCarResponse;
import com.mp.karental.entity.Account;
import com.mp.karental.entity.Car;
import com.mp.karental.exception.AppException;
import com.mp.karental.mapper.CarMapper;
import com.mp.karental.repository.AccountRepository;
import com.mp.karental.repository.CarRepository;
import com.mp.karental.security.SecurityUtil;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CarServiceTest {

    @InjectMocks
    private CarService carService;

    @Mock
    private CarRepository carRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private CarMapper carMapper;

    @Mock
    private FileService fileService;
    @Mock
    private AddCarRequest addCarRequest;

    private MockedStatic<SecurityUtil> mockedSecurityUtil;

    @BeforeEach
    void setUp() {
        mockedSecurityUtil = Mockito.mockStatic(SecurityUtil.class);
    }

    @AfterEach
    void tearDown() {
        mockedSecurityUtil.close();
    }

    @Test
    void testGetCarsByUserId_WhenUserHasCars_ShouldReturnCarList() {
        String accountId = "user-123";
        mockedSecurityUtil.when(SecurityUtil::getCurrentAccountId).thenReturn(accountId);

        Account account = new Account();
        account.setId(accountId);

        Car car1 = Car.builder()
                .id("car-1")
                .brand("Toyota")
                .model("Corolla")
                .productionYear(2020)
                .status("AVAILABLE")
                .mileage(10000)
                .basePrice(500000)
                .address("Hanoi, Vietnam")
                .carImageFront("car/user-123/car-1/images/front")
                .carImageBack("car/user-123/car-1/images/back")
                .carImageLeft("car/user-123/car-1/images/left")
                .carImageRight("car/user-123/car-1/images/right")
                .account(account)
                .build();

        when(carRepository.findByAccountId(eq(accountId), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(car1)));
        when(fileService.getFileUrl(anyString())).thenReturn("https://example.com/image.jpg");

        ViewMyCarResponse response = carService.getCarsByUserId(0, 10, "mileage,asc");

        assertNotNull(response);
        assertEquals(1, response.getCars().getTotalElements());
        assertEquals("https://example.com/image.jpg", response.getCars().getContent().get(0).getCarImageFront());

        verify(carRepository, times(1)).findByAccountId(eq(accountId), any(Pageable.class));
        verify(fileService, times(4)).getFileUrl(anyString());
    }

    @Test
    void testGetCarsByUserId_WhenUserHasNoCars_ShouldReturnEmptyList() {
        String accountId = "user-123";
        mockedSecurityUtil.when(SecurityUtil::getCurrentAccountId).thenReturn(accountId);

        when(carRepository.findByAccountId(eq(accountId), any(Pageable.class)))
                .thenReturn(Page.empty(PageRequest.of(0, 10)));

        ViewMyCarResponse response = carService.getCarsByUserId(0, 10, "mileage,asc");

        assertNotNull(response);
        assertEquals(0, response.getCars().getTotalElements());

        verify(carRepository, times(1)).findByAccountId(eq(accountId), any(Pageable.class));
    }

    @Test
    void addCar_validRequest_success() throws AppException {
        // Mock SecurityUtil trả về account ID hợp lệ
        String accountId = "user-123";
        mockedSecurityUtil.when(SecurityUtil::getCurrentAccountId).thenReturn(accountId);

        // Mock account tồn tại
        Account mockAccount = new Account();
        mockAccount.setId(accountId);
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(mockAccount));

        // Tạo request giả lập
        AddCarRequest addCarRequest = new AddCarRequest();
        addCarRequest.setLicensePlate("49F-123.45");
        addCarRequest.setAutomatic(true);
        addCarRequest.setGasoline(true);

        // Mock carMapper
        Car mockCar = new Car();
        mockCar.setLicensePlate("49F-123.45");
        when(carMapper.toCar(any(AddCarRequest.class))).thenReturn(mockCar);

        // Mock save xe vào database
        when(carRepository.save(any(Car.class))).thenAnswer(invocation -> {
            Car car = invocation.getArgument(0);
            car.setId("car-123"); // Gán ID giả lập
            return car;
        });


        // Mock chuyển đổi từ Car -> CarResponse
        CarResponse mockResponse = new CarResponse();
        mockResponse.setLicensePlate("49F-123.45");
        when(carMapper.toCarResponse(any(Car.class))).thenReturn(mockResponse);

        // Thực thi test
        CarResponse response = carService.addNewCar(addCarRequest);

        // Kiểm tra kết quả
        assertNotNull(response, "Response should not be null");
        assertEquals("49F-123.45", response.getLicensePlate());
    }

    @Test
    @WithMockUser
    void addCar_userNotFound() {
        mockedSecurityUtil.when(SecurityUtil::getCurrentAccountId).thenReturn("not_exist");

            when(accountRepository.findById("not_exist")).thenReturn(Optional.empty()); // Không tìm thấy tài khoản

            // Khi tài khoản không tồn tại, mong đợi ngoại lệ xảy ra
            Assertions.assertThrows(AppException.class, () -> carService.addNewCar(addCarRequest));

    }




}