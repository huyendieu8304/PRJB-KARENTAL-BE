package com.mp.karental.validation.validator;

import com.mp.karental.dto.request.AddCarRequest;
import com.mp.karental.service.ExcelService;
import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BrandAndModelValidatorTest {

    @Mock
    private ExcelService excelService;

    private BrandAndModelValidator validator;

    @Mock
    private ConstraintValidatorContext context;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        validator = new BrandAndModelValidator(excelService);
    }

    @Test
    void testValidBrandAndModel() {
        // Mock dữ liệu từ ExcelService
        Map<String, Set<String>> brandModelMap = new HashMap<>();
        Set<String> toyotaModels = new HashSet<>();
        toyotaModels.add("Camry");
        brandModelMap.put("Toyota", toyotaModels);

        when(excelService.getBrandModelMap()).thenReturn(brandModelMap);

        // Tạo request hợp lệ
        AddCarRequest request = new AddCarRequest();
        request.setBrand("Toyota");
        request.setModel("Camry");

        assertTrue(validator.isValid(request, context));
    }

    @Test
    void testInvalidBrandAndModel() {
        // Mock dữ liệu từ ExcelService
        Map<String, Set<String>> brandModelMap = new HashMap<>();
        Set<String> toyotaModels = new HashSet<>();
        toyotaModels.add("Camry");
        brandModelMap.put("Toyota", toyotaModels);

        when(excelService.getBrandModelMap()).thenReturn(brandModelMap);

        // Tạo request không hợp lệ
        AddCarRequest request = new AddCarRequest();
        request.setBrand("Toyota");
        request.setModel("Civic"); // Civic không phải là model hợp lệ của Toyota

        assertFalse(validator.isValid(request, context));
    }

    @Test
    void testNullRequest() {
        assertTrue(validator.isValid(null, context));
    }

    @Test
    void testNullBrandOrModel() {
        AddCarRequest requestWithNullBrand = new AddCarRequest();
        requestWithNullBrand.setBrand(null);
        requestWithNullBrand.setModel("Camry");

        AddCarRequest requestWithNullModel = new AddCarRequest();
        requestWithNullModel.setBrand("Toyota");
        requestWithNullModel.setModel(null);

        assertTrue(validator.isValid(requestWithNullBrand, context));
        assertTrue(validator.isValid(requestWithNullModel, context));
    }
}
