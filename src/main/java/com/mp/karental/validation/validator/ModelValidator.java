package com.mp.karental.validation.validator;
import com.mp.karental.service.ExcelService;
import com.mp.karental.validation.ValidModel;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;

import java.util.List;


@RequiredArgsConstructor
public class ModelValidator implements ConstraintValidator<ValidModel, String> {
    private final ExcelService excelService;

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        List<String> allModels = excelService.getAllModels();
        return allModels.contains(value);
    }
}
