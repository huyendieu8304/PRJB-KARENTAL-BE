package com.mp.karental.validation.validator;

import com.mp.karental.service.AllowedValuesService;
import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

class ColorValidatorTest {

    @Mock
    private AllowedValuesService allowedValuesService;

    private ColorValidator validator;

    @Mock
    private ConstraintValidatorContext context;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        validator = new ColorValidator(allowedValuesService);
    }

    @Test
    void testValidColor() {
        List<String> allowedColors = Arrays.asList("Red", "Blue", "Black", "White");

        when(allowedValuesService.getAllowedValuesOfColor()).thenReturn(allowedColors);

        assertTrue(validator.isValid("Red", context));
        assertTrue(validator.isValid("blue", context)); // Test case ignore case
        assertTrue(validator.isValid("BLACK", context));
    }

    @Test
    void testInvalidColor() {
        List<String> allowedColors = Arrays.asList("Red", "Blue", "Black", "White");

        when(allowedValuesService.getAllowedValuesOfColor()).thenReturn(allowedColors);

        assertFalse(validator.isValid("Green", context));
        assertFalse(validator.isValid("Pink", context));
        assertFalse(validator.isValid("Yellow", context));
    }

    @Test
    void testNullColor() {
        List<String> allowedColors = Arrays.asList("Red", "Blue", "Black", "White");

        when(allowedValuesService.getAllowedValuesOfColor()).thenReturn(allowedColors);

        assertFalse(validator.isValid(null, context));
    }

    @Test
    void testEmptyColor() {
        List<String> allowedColors = Arrays.asList("Red", "Blue", "Black", "White");

        when(allowedValuesService.getAllowedValuesOfColor()).thenReturn(allowedColors);

        assertFalse(validator.isValid("", context));
    }
}
