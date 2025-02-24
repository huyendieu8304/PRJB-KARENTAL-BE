package com.mp.karental.service;

import com.mp.karental.dto.response.ViewMyCarResponse;
import com.mp.karental.entity.Account;
import com.mp.karental.entity.Car;
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
    private FileService fileService;

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


}