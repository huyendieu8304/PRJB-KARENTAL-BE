package com.mp.karental.validation.validator;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * This is the class used to test custom validator
 * <p>
 *     Check whether the validator could check the exist of data in required field
 * </p>
 * @author  DieutTTH4
 *
 * @version 1.0
 */
@ExtendWith(MockitoExtension.class)
class RequireFieldValidatorTest {
    @Mock
    private ConstraintValidatorContext context;

    @InjectMocks
    private RequiredFieldValidator requireFieldValidator;

    @BeforeEach
    void setUp() {
        requireFieldValidator.initialize(null); // not thing special
    }

    @Test
    void shouldReturnTrueForValidString() {
        assertTrue(requireFieldValidator.isValid("Hello", context));
    }

    @Test
    void shouldReturnFalseForEmptyString() {
        assertFalse(requireFieldValidator.isValid("", context));
    }

    @Test
    void shouldReturnFalseForBlankString() {
        assertFalse(requireFieldValidator.isValid("   ", context));
    }

    @Test
    void shouldReturnFalseForNullValue() {
        assertFalse(requireFieldValidator.isValid(null, context));
    }

    @Test
    void shouldReturnTrueForNonStringObject() {
        assertTrue(requireFieldValidator.isValid(123, context));
        assertTrue(requireFieldValidator.isValid(new Object(), context));
    }

}