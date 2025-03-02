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
 * test model validator
 *
 * QuangPM20
 * version 1.0
 */
class ModelValidatorTest {

    @Mock
    private ExcelService excelService;

    private ModelValidator validator;

    @Mock
    private ConstraintValidatorContext context;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        validator = new ModelValidator(excelService);
    }

    @Test
    void testValidModel() {
        // Mock dữ liệu từ ExcelService
        List<String> validModels = Arrays.asList("Camry", "Civic", "Mustang");

        when(excelService.getAllModels()).thenReturn(validModels);

        // Kiểm tra model hợp lệ
        assertTrue(validator.isValid("Camry", context));
        assertTrue(validator.isValid("Civic", context));
        assertTrue(validator.isValid("Mustang", context));
    }

    @Test
    void testInvalidModel() {
        // Mock dữ liệu từ ExcelService
        List<String> validModels = Arrays.asList("Camry", "Civic", "Mustang");

        when(excelService.getAllModels()).thenReturn(validModels);

        // Kiểm tra model không hợp lệ
        assertFalse(validator.isValid("Accord", context));
        assertFalse(validator.isValid("Model S", context));
        assertFalse(validator.isValid("Corolla", context));
    }

    @Test
    void testNullModel() {
        // Mock dữ liệu từ ExcelService
        List<String> validModels = Arrays.asList("Camry", "Civic", "Mustang");

        when(excelService.getAllModels()).thenReturn(validModels);

        // Kiểm tra khi giá trị là null
        assertFalse(validator.isValid(null, context));
    }

    @Test
    void testEmptyModel() {
        // Mock dữ liệu từ ExcelService
        List<String> validModels = Arrays.asList("Camry", "Civic", "Mustang");

        when(excelService.getAllModels()).thenReturn(validModels);

        // Kiểm tra khi giá trị là chuỗi rỗng
        assertFalse(validator.isValid("", context));
    }
}
