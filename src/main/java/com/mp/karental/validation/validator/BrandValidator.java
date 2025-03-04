package com.mp.karental.validation.validator;

import com.mp.karental.service.ExcelService;
import com.mp.karental.validation.ValidBrand;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * Validator for checking if a given brand is valid.
 * The valid brands are retrieved from the ExcelService.
 * QuangPM20
 * @version 1.0
 */
@RequiredArgsConstructor
public class BrandValidator implements ConstraintValidator<ValidBrand, String> {
    private final ExcelService excelService;

    /**
     * Checks whether the provided brand exists in the list of valid brands.
     *
     * @param value   The brand name to validate.
     * @param context The validation context.
     * @return true if the brand exists in the list of valid brands, false otherwise.
     */
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        List<String> allBrands = excelService.getAllBrands();
        return allBrands.contains(value);
    }
}
