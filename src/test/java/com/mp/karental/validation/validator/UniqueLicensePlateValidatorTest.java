package com.mp.karental.validation.validator;

import com.mp.karental.repository.CarRepository;
import com.mp.karental.entity.Car;
import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class UniqueLicensePlateValidatorTest {

    @Mock
    private CarRepository carRepository;
    @Mock
    private ConstraintValidatorContext context;
    @InjectMocks
    private UniqueLicensePlateValidator validator;

    @ParameterizedTest(name = "[{index}] licensePlate={0} -> expected valid? {1}") // Set name for test cases
    @CsvSource({
            "ABC-1234, true", // License plate does not exist (valid)
            "XYZ-5678, false" // License plate exists (invalid)
    })
    void isValid(String licensePlate, boolean expected) {
        // Mock repository behavior
        if (expected) {
            when(carRepository.findByLicensePlate(licensePlate)).thenReturn(Optional.empty());
        } else {
            when(carRepository.findByLicensePlate(licensePlate)).thenReturn(Optional.of(new Car()));
        }

        // Execute validator method
        boolean result = validator.isValid(licensePlate, context);

        // Assert the result
        assertEquals(expected, result);
    }
}
