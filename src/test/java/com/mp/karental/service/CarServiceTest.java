package com.mp.karental.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.mp.karental.dto.response.ViewMyCarResponse;
import com.mp.karental.entity.Car;
import com.mp.karental.repository.CarRepository;
import com.mp.karental.security.SecurityUtil;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.util.Collections;
import java.util.List;

@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS) // Để @BeforeAll và @AfterAll có thể gọi method non-static
public class CarServiceTest {

    @Mock
    private CarRepository carRepository;

    @InjectMocks
    private CarService carService;

    private static MockedStatic<SecurityUtil> mockedSecurityUtil;
    private final String testUserId = "test-user-id";

    private Page<Car> carPage;

    @BeforeAll
    void beforeAll() {
        // Mock SecurityUtil static method để tránh lỗi đăng ký nhiều lần
        mockedSecurityUtil = mockStatic(SecurityUtil.class);
        mockedSecurityUtil.when(SecurityUtil::getCurrentAccountId).thenReturn(testUserId);
    }

    @BeforeEach
    void setUp() {
        // Tạo danh sách xe giả lập
        Car car1 = new Car();
        car1.setId("1");
        car1.setLicensePlate("ABC123");
        car1.setBrand("Toyota");

        Car car2 = new Car();
        car2.setId("2");
        car2.setLicensePlate("XYZ789");
        car2.setBrand("Honda");

        List<Car> carList = List.of(car1, car2);
        carPage = new PageImpl<>(carList, PageRequest.of(0, 2), carList.size());
    }

//    @Test
//    void testGetCarsByUserId_ValidCase() {
//        when(carRepository.findByAccountId(eq(testUserId), any(Pageable.class))).thenReturn(carPage);
//
//        ViewMyCarResponse response = carService.getCarsByUserId(0, 2);
//
//        assertNotNull(response);
//        assertEquals(2, response.getCars().getContent().size());
//        assertEquals("Toyota", response.getCars().getContent().get(0).getBrand());
//        assertEquals("Honda", response.getCars().getContent().get(1).getBrand());
//        System.out.println("Số lượng xe trả về từ repo: " + carPage.getContent().size());
//
//        verify(carRepository, times(1)).findByAccountId(eq(testUserId), any(Pageable.class));
//    }
//
//    @Test
//    void testGetCarsByUserId_NoCarsFound() {
//        when(carRepository.findByAccountId(eq(testUserId), any(Pageable.class))).thenReturn(Page.empty());
//
//        ViewMyCarResponse response = carService.getCarsByUserId(0, 2);
//
//        assertNotNull(response);
//        assertTrue(response.getCars().isEmpty());
//    }

    @Test
    void testGetCarsByUserId_InvalidPageSize() {
        assertThrows(IllegalArgumentException.class, () -> carService.getCarsByUserId(-1, 2));
        assertThrows(IllegalArgumentException.class, () -> carService.getCarsByUserId(0, -5));
    }

    @Test
    void testGetCarsByUserId_MaxPageSize() {
        Page<Car> largePage = new PageImpl<>(Collections.nCopies(100, new Car()), PageRequest.of(0, 100), 100);
        when(carRepository.findByAccountId(eq(testUserId), any(Pageable.class))).thenReturn(largePage);

        ViewMyCarResponse response = carService.getCarsByUserId(0, 100);
        assertNotNull(response);
        assertEquals(100, response.getCars().getContent().size());
    }

    @AfterAll
    void afterAll() {
        if (mockedSecurityUtil != null) {
            mockedSecurityUtil.close();
        }
    }
}