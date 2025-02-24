package com.mp.karental.validation.validator;

import com.mp.karental.service.ExcelService;
import com.mp.karental.validation.ValidBrand;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;

import java.util.List;


@RequiredArgsConstructor
public class BrandValidator implements ConstraintValidator<ValidBrand, String> {
    private final ExcelService excelService;

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        List<String> allBrands = excelService.getAllBrands();
        return allBrands.contains(value);
    }
}
