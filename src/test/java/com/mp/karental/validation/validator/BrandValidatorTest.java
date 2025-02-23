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
        // Mock dữ liệu từ ExcelService
        List<String> validBrands = Arrays.asList("Toyota", "Honda", "Ford");

        when(excelService.getAllBrands()).thenReturn(validBrands);

        // Kiểm tra thương hiệu hợp lệ
        assertTrue(validator.isValid("Toyota", context));
        assertTrue(validator.isValid("Honda", context));
        assertTrue(validator.isValid("Ford", context));
    }

    @Test
    void testInvalidBrand() {
        // Mock dữ liệu từ ExcelService
        List<String> validBrands = Arrays.asList("Toyota", "Honda", "Ford");

        when(excelService.getAllBrands()).thenReturn(validBrands);

        // Kiểm tra thương hiệu không hợp lệ
        assertFalse(validator.isValid("BMW", context));
        assertFalse(validator.isValid("Mercedes", context));
        assertFalse(validator.isValid("Tesla", context));
    }

    @Test
    void testNullBrand() {
        // Mock dữ liệu từ ExcelService
        List<String> validBrands = Arrays.asList("Toyota", "Honda", "Ford");

        when(excelService.getAllBrands()).thenReturn(validBrands);

        // Kiểm tra khi giá trị là null
        assertFalse(validator.isValid(null, context));
    }

    @Test
    void testEmptyBrand() {
        // Mock dữ liệu từ ExcelService
        List<String> validBrands = Arrays.asList("Toyota", "Honda", "Ford");

        when(excelService.getAllBrands()).thenReturn(validBrands);

        // Kiểm tra khi giá trị là chuỗi rỗng
        assertFalse(validator.isValid("", context));
    }
}
