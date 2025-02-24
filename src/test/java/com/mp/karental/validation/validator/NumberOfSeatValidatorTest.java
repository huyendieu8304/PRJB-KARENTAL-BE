package com.mp.karental.validation.validator;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NumberOfSeatValidatorTest {

    private NumberOfSeatValidator validator;

    @Mock
    private ConstraintValidatorContext context;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        validator = new NumberOfSeatValidator();
    }

    @Test
    void testValidSeatNumbers() {
        assertTrue(validator.isValid(4, context));
        assertTrue(validator.isValid(5, context));
        assertTrue(validator.isValid(7, context));
    }

    @Test
    void testInvalidSeatNumbers() {
        assertFalse(validator.isValid(2, context));
        assertFalse(validator.isValid(6, context));
        assertFalse(validator.isValid(8, context));
        assertFalse(validator.isValid(10, context));
    }

    @Test
    void testNullSeatNumber() {
        assertFalse(validator.isValid(null, context));
    }
}
