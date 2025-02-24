package com.mp.karental.validation.validator;

import com.mp.karental.exception.ErrorCode;
import com.mp.karental.validation.ValidBrandModel;
import com.mp.karental.dto.request.AddCarRequest;
import com.mp.karental.service.ExcelService;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.Set;

@RequiredArgsConstructor
public class BrandAndModelValidator implements ConstraintValidator<ValidBrandModel, AddCarRequest> {

    private final ExcelService excelService;

    @Override
    public boolean isValid(AddCarRequest value, ConstraintValidatorContext context) {
        if (value == null) {
            return true; // Allow null if needed, or return false if not allowed
        }

        // Get brand and model from the request
        String brand = value.getBrand();
        String model = value.getModel();

        if (brand == null || model == null) {
            return true;  // Skip validation if any of the fields are null
        }

        // Get the map of valid brand-model combinations from the ExcelService
        Map<String, Set<String>> brandModelMap = excelService.getBrandModelMap();

        // Validate the combination of brand and model
        Set<String> validModels = brandModelMap.get(brand);


        return validModels.contains(model);  // Valid combination
    }
}
