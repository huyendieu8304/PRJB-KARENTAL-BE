package com.mp.karental.validation.validator;
import com.mp.karental.constant.EAdditionalFunctions;
import com.mp.karental.validation.ValidAdditionalFunction;
import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AdditionalFunctionValidatorTest {
    private AdditionalFunctionValidator validator;
    private ConstraintValidatorContext context;

    @BeforeEach
    void setUp() {
        validator = new AdditionalFunctionValidator();
        context = mock(ConstraintValidatorContext.class);
    }

    @Test
    void whenValidFunction_thenShouldReturnTrue() {
        // Lấy một giá trị hợp lệ từ enum
        String validFunction = EAdditionalFunctions.BLUETOOTH.getName();
        assertTrue(validator.isValid(validFunction, context));
    }

    @Test
    void whenMultipleValidFunctions_thenShouldReturnTrue() {
        // Lấy hai giá trị hợp lệ từ enum
        String validFunctions = EAdditionalFunctions.BLUETOOTH.getName() + ", " + EAdditionalFunctions.GPS.getName();
        assertTrue(validator.isValid(validFunctions, context));
    }

    @Test
    void whenInvalidFunction_thenShouldReturnFalse() {
        // Giá trị không hợp lệ
        String invalidFunction = "INVALID_FUNCTION";
        assertFalse(validator.isValid(invalidFunction, context));
    }

    @Test
    void whenValidAndInvalidFunction_thenShouldReturnFalse() {
        // Một giá trị hợp lệ và một giá trị không hợp lệ
        String mixedFunctions = EAdditionalFunctions.BLUETOOTH.getName() + ", INVALID_FUNCTION";
        assertFalse(validator.isValid(mixedFunctions, context));
    }

    @Test
    void whenNullValue_thenShouldReturnTrue() {
        assertTrue(validator.isValid(null, context));
    }

}
