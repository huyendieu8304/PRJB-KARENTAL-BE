package com.mp.karental.validation.validator;

import com.mp.karental.validation.ValidProductionYear;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ProductionYearValidator implements ConstraintValidator<ValidProductionYear,Integer> {
    private static final int MIN_YEAR = 1990;
    private static final int MAX_YEAR = 2030;

    @Override
    public boolean isValid(Integer value, ConstraintValidatorContext context) {

        return value >= MIN_YEAR && value <= MAX_YEAR;
    }
}
