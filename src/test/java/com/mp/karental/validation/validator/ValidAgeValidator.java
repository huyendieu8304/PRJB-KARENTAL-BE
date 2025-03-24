package com.mp.karental.validation.validator;

import com.mp.karental.validation.ValidAge;
import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ValidAgeValidatorTest {

    private ValidAgeValidator validator;

    @Mock
    private ConstraintValidatorContext context;

    @BeforeEach
    void setUp() {
        validator = new ValidAgeValidator();
        ValidAge annotation = mock(ValidAge.class);
        when(annotation.min()).thenReturn(18); // Giả sử tuổi tối thiểu là 18
        validator.initialize(annotation);
    }

    @Test
    void isValid_ValidAge_ShouldReturnTrue() {
        LocalDate validDob = LocalDate.now().minusYears(20); // Người 20 tuổi
        assertTrue(validator.isValid(validDob, context));
    }

    @Test
    void isValid_InvalidAge_ShouldReturnFalse() {
        LocalDate invalidDob = LocalDate.now().minusYears(16); // Người 16 tuổi
        assertFalse(validator.isValid(invalidDob, context));
    }

    @Test
    void isValid_NullDate_ShouldReturnFalse() {
        assertFalse(validator.isValid(null, context));
    }

    @Test
    void isValid_ExactlyMinAge_ShouldReturnTrue() {
        LocalDate exactMinAgeDob = LocalDate.now().minusYears(18); // Đúng 18 tuổi
        assertTrue(validator.isValid(exactMinAgeDob, context));
    }
}
