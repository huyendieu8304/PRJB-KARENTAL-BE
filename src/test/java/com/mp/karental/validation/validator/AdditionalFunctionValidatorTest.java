package com.mp.karental.validation.validator;
import com.mp.karental.constant.EAdditionalFunctions;
import com.mp.karental.validation.ValidAdditionalFunction;
import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * test addtional function validator
 *
 * QuangPM20
 * version 1.0
 */

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
        String validFunction = EAdditionalFunctions.BLUETOOTH.getName();
        assertTrue(validator.isValid(validFunction, context));
    }

    @Test
    void whenMultipleValidFunctions_thenShouldReturnTrue() {
        String validFunctions = EAdditionalFunctions.BLUETOOTH.getName() + ", " + EAdditionalFunctions.GPS.getName();
        assertTrue(validator.isValid(validFunctions, context));
    }

    @Test
    void whenInvalidFunction_thenShouldReturnFalse() {
        String invalidFunction = "INVALID_FUNCTION";
        assertFalse(validator.isValid(invalidFunction, context));
    }

    @Test
    void whenValidAndInvalidFunction_thenShouldReturnFalse() {
        String mixedFunctions = EAdditionalFunctions.BLUETOOTH.getName() + ", INVALID_FUNCTION";
        assertFalse(validator.isValid(mixedFunctions, context));
    }

    @Test
    void whenNullValue_thenShouldReturnTrue() {
        assertTrue(validator.isValid(null, context));
    }
    @Test
    void whenEmptyValue_thenShouldReturnTrue() {
        assertTrue(validator.isValid("", context));
    }

}
