package com.mp.karental.validation.validator;

import com.mp.karental.constant.ECarStatus;
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
    void testValidStatus_NotVerified() {
        assertTrue(validator.isValid(ECarStatus.NOT_VERIFIED.name(), context), "Expected 'not verified' to be valid");
    }

    @Test
    void testValidStatus_Stopped() {
        assertTrue(validator.isValid(ECarStatus.STOPPED.name(), context), "Expected 'stopped' to be valid");
    }

    @Test
    void testValidStatus_CaseInsensitive() {
        assertTrue(validator.isValid(ECarStatus.NOT_VERIFIED.name(), context), "Expected 'not verified' to be valid");
        assertTrue(validator.isValid(ECarStatus.STOPPED.name(), context), "Expected 'stopped' to be valid");
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
