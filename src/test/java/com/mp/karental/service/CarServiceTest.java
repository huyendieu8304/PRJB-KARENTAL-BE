package com.mp.karental.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.mp.karental.dto.response.ViewMyCarResponse;
import com.mp.karental.entity.Car;
import com.mp.karental.repository.CarRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import java.util.List;
import java.util.Collections;

/**
 * This is a class used to test CarService, include view list user'cars
 *
 * @author AnhHP9
 *
 * @version 1.0
 */
@ExtendWith(MockitoExtension.class)
public class CarServiceTest {

    @Mock
    private CarRepository carRepository;

    @InjectMocks
    private CarService carService;

    private Page<Car> carPage;

    @BeforeEach
    void setUp() {
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

    @Test
    void testGetCarsByUserId_ValidCase() {
        when(carRepository.findByAccountId(anyString(), any(Pageable.class))).thenReturn(carPage);

        ViewMyCarResponse response = carService.getCarsByUserId(0, 2);

        assertNotNull(response);
        assertEquals(2, response.getCars().getContent().size());
        assertEquals("Toyota", response.getCars().getContent().get(0).getBrand());
        assertEquals("Honda", response.getCars().getContent().get(1).getBrand());

        verify(carRepository, times(1)).findByAccountId(anyString(), any(Pageable.class));
    }

    @Test
    void testGetCarsByUserId_NoCarsFound() {
        when(carRepository.findByAccountId(anyString(), any(Pageable.class))).thenReturn(Page.empty());

        ViewMyCarResponse response = carService.getCarsByUserId(0, 2);

        assertNotNull(response);
        assertTrue(response.getCars().isEmpty());
    }

    @Test
    void testGetCarsByUserId_InvalidPageSize() {
        assertThrows(IllegalArgumentException.class, () -> carService.getCarsByUserId(-1, 2));
        assertThrows(IllegalArgumentException.class, () -> carService.getCarsByUserId(0, -5));
    }

    @Test
    void testGetCarsByUserId_MaxPageSize() {
        Page<Car> largePage = new PageImpl<>(Collections.nCopies(100, new Car()), PageRequest.of(0, 100), 100);
        when(carRepository.findByAccountId(anyString(), any(Pageable.class))).thenReturn(largePage);

        ViewMyCarResponse response = carService.getCarsByUserId(0, 100);
        assertNotNull(response);
        assertEquals(100, response.getCars().getContent().size());
    }


}
