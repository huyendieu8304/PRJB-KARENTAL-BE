package com.mp.karental.validation.validator;

import com.mp.karental.service.ExcelService;
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

/**
 * test brand validator
 *
 * QuangPM20
 * version 1.0
 */
class BrandValidatorTest {

    @Mock
    private ExcelService excelService;

    private BrandValidator validator;

    @Mock
    private ConstraintValidatorContext context;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        validator = new BrandValidator(excelService);
    }

    @Test
    void testValidBrand() {
        List<String> validBrands = Arrays.asList("Toyota", "Honda", "Ford");

        when(excelService.getAllBrands()).thenReturn(validBrands);

        assertTrue(validator.isValid("Toyota", context));
        assertTrue(validator.isValid("Honda", context));
        assertTrue(validator.isValid("Ford", context));
    }

    @Test
    void testInvalidBrand() {
        List<String> validBrands = Arrays.asList("Toyota", "Honda", "Ford");

        when(excelService.getAllBrands()).thenReturn(validBrands);

        assertFalse(validator.isValid("BMW", context));
        assertFalse(validator.isValid("Mercedes", context));
        assertFalse(validator.isValid("Tesla", context));
    }

    @Test
    void testNullBrand() {
        List<String> validBrands = Arrays.asList("Toyota", "Honda", "Ford");

        when(excelService.getAllBrands()).thenReturn(validBrands);

        assertFalse(validator.isValid(null, context));
    }

    @Test
    void testEmptyBrand() {
        List<String> validBrands = Arrays.asList("Toyota", "Honda", "Ford");

        when(excelService.getAllBrands()).thenReturn(validBrands);

        assertFalse(validator.isValid("", context));
    }
}
