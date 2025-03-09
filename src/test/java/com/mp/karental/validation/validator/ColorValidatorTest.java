package com.mp.karental.validation.validator;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
/**
 * test color validator
 *
 * QuangPM20
 * version 1.0
 */
class ColorValidatorTest {

    private ColorValidator validator;
    private ConstraintValidatorContext context;

    @BeforeEach
    void setUp() {
        validator = new ColorValidator();
        context = mock(ConstraintValidatorContext.class); // Mocking context
    }

    @Test
    void testValidColor() {
        assertTrue(validator.isValid("Red", context));
        assertTrue(validator.isValid("blue", context)); // Case-insensitive
        assertTrue(validator.isValid("BLACK", context));
    }

    @Test
    void testNullColor() {
        assertFalse(validator.isValid(null, context)); // Should return false
    }

    @Test
    void testEmptyColor() {
        assertFalse(validator.isValid("", context)); // Empty string should be invalid
    }
}
