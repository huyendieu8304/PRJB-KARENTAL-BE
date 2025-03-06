package com.mp.karental.validation.validator;

import com.mp.karental.validation.ValidProductionYear;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;

/**
 * Validator for checking if the provided production year is within the allowed range.
 * The valid range is from 1990 to 2030.
 * QuangPM20
 * @version 1.0
 */
@RequiredArgsConstructor
public class ProductionYearValidator implements ConstraintValidator<ValidProductionYear, Integer> {
    // Minimum and maximum allowed production years
    private static final int MIN_YEAR = 1990;
    private static final int MAX_YEAR = 2030;

    /**
     * Validates whether the provided production year is within the allowed range.
     *
     * @param value   The production year to validate.
     * @param context The validation context.
     * @return true if the production year is within the valid range, false otherwise.
     */
    @Override
    public boolean isValid(Integer value, ConstraintValidatorContext context) {
        return value >= MIN_YEAR && value <= MAX_YEAR;
    }
}
