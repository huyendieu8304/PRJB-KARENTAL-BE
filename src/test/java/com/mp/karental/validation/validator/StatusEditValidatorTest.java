package com.mp.karental.validation.validator;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 *Test status when edit
 *
 * @author QuangPM20
 * @version 1.0
 */
class StatusEditValidatorTest {

    private StatusEditValidator validator;
    private ConstraintValidatorContext context;

    @BeforeEach
    void setUp() {
        validator = new StatusEditValidator();
        context = mock(ConstraintValidatorContext.class);
    }

    @Test
    void testValidStatus_Available() {
        assertTrue(validator.isValid("available", context), "Expected 'available' to be valid");
    }

    @Test
    void testValidStatus_Stopped() {
        assertTrue(validator.isValid("stopped", context), "Expected 'stopped' to be valid");
    }

    @Test
    void testValidStatus_CaseInsensitive() {
        assertTrue(validator.isValid("AVAILABLE", context), "Expected 'AVAILABLE' to be valid");
        assertTrue(validator.isValid("STOPPED", context), "Expected 'STOPPED' to be valid");
    }

    @Test
    void testInvalidStatus() {
        assertFalse(validator.isValid("active", context), "Expected 'active' to be invalid");
        assertFalse(validator.isValid("inactive", context), "Expected 'inactive' to be invalid");
        assertFalse(validator.isValid("paused", context), "Expected 'paused' to be invalid");
    }

    @Test
    void testNullStatus() {
        assertFalse(validator.isValid(null, context), "Expected null to be invalid");
    }

    @Test
    void testEmptyString() {
        assertFalse(validator.isValid("", context), "Expected empty string to be invalid");
    }

    @Test
    void testWhitespaceString() {
        assertFalse(validator.isValid(" ", context), "Expected whitespace to be invalid");
    }
}
