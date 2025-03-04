package com.mp.karental.validation.validator;

import com.mp.karental.service.ExcelService;
import com.mp.karental.validation.ValidModel;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * Validator for checking if a given car model is valid.
 * The valid models are retrieved from the {@link ExcelService}.
 * QuangPM20
 * @version 1.0
 */
@RequiredArgsConstructor
public class ModelValidator implements ConstraintValidator<ValidModel, String> {
    private final ExcelService excelService;

    /**
     * Validates whether the provided car model exists in the list of valid models.
     *
     * @param value   The car model name to validate.
     * @param context The validation context.
     * @return true if the model exists in the list of valid models, false otherwise.
     */
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        List<String> allModels = excelService.getAllModels();
        return allModels.contains(value);
    }
}
