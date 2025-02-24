package com.mp.karental.service;

import com.mp.karental.dto.response.ViewMyCarResponse;
import com.mp.karental.entity.Car;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CarServiceTest {

    @InjectMocks
    private CarService carService;

    @Mock
    private CarRepository carRepository;

    private MockedStatic<SecurityUtil> mockedSecurityUtil;

    @BeforeEach
    void setUp() {
        // Mock static SecurityUtil
        mockedSecurityUtil = Mockito.mockStatic(SecurityUtil.class);
    }

    @AfterEach
    void tearDown() {
        // Close mock để tránh lỗi đăng ký nhiều lần
        mockedSecurityUtil.close();
    }

    // Positive Test Case: Khi user có xe -> Trả về danh sách xe hợp lệ
    @Test
    void testGetCarsByUserId_WhenUserHasCars_ShouldReturnCarList() {
        String accountId = "user-123";
        mockedSecurityUtil.when(SecurityUtil::getCurrentAccountId).thenReturn(accountId);

        Car car1 = Car.builder().id("car-1").brand("Toyota").model("Camry").licensePlate("XYZ-123").accountId(null).build();
        Car car2 = Car.builder().id("car-2").brand("Honda").model("Civic").licensePlate("ABC-456").accountId(null).build();

        List<Car> carList = List.of(car1, car2);
        Page<Car> carPage = new PageImpl<>(carList, PageRequest.of(0, 10), carList.size());

        when(carRepository.findByAccountId(eq(accountId), any(Pageable.class))).thenReturn(carPage);

        ViewMyCarResponse response = carService.getCarsByUserId(0, 10);

        assertNotNull(response);
        assertEquals(2, response.getCars().getTotalElements());
        assertEquals("Toyota", response.getCars().getContent().get(0).getBrand());
        assertEquals("Honda", response.getCars().getContent().get(1).getBrand());

        verify(carRepository, times(1)).findByAccountId(eq(accountId), any(Pageable.class));
    }

    // Negative Test Case: Khi user không có xe -> Trả về danh sách rỗng
    @Test
    void testGetCarsByUserId_WhenUserHasNoCars_ShouldReturnEmptyList() {
        String accountId = "user-123";
        mockedSecurityUtil.when(SecurityUtil::getCurrentAccountId).thenReturn(accountId);

        Page<Car> emptyPage = Page.empty(PageRequest.of(0, 10));
        when(carRepository.findByAccountId(eq(accountId), any(Pageable.class))).thenReturn(emptyPage);

        ViewMyCarResponse response = carService.getCarsByUserId(0, 10);

        assertNotNull(response);
        assertEquals(0, response.getCars().getTotalElements());

        verify(carRepository, times(1)).findByAccountId(eq(accountId), any(Pageable.class));
    }

    //️Edge Case: Khi danh sách xe quá lớn (size max value)
    @Test
    void testGetCarsByUserId_WhenSizeIsMaxValue_ShouldReturnValidList() {
        String accountId = "user-123";
        mockedSecurityUtil.when(SecurityUtil::getCurrentAccountId).thenReturn(accountId);

        Car car = Car.builder().id("car-1").brand("Toyota").model("Camry").licensePlate("XYZ-123").accountId(null).build();
        List<Car> largeCarList = List.of(car); // Giả lập 1 danh sách lớn
        Page<Car> carPage = new PageImpl<>(largeCarList, PageRequest.of(0, Integer.MAX_VALUE), largeCarList.size());

        when(carRepository.findByAccountId(eq(accountId), any(Pageable.class))).thenReturn(carPage);

        ViewMyCarResponse response = carService.getCarsByUserId(0, Integer.MAX_VALUE);

        assertNotNull(response);
        assertTrue(response.getCars().getTotalElements() > 0);

        verify(carRepository, times(1)).findByAccountId(eq(accountId), any(Pageable.class));
    }
}
