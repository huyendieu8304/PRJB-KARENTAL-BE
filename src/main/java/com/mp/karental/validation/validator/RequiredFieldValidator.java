package com.mp.karental.validation.validator;

import com.mp.karental.validation.RequiredField;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RequiredFieldValidator implements ConstraintValidator<RequiredField, Object> {

    @Override
    public void initialize(RequiredField constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(Object o, ConstraintValidatorContext constraintValidatorContext) {
        if (o instanceof String s) {
            return !s.isEmpty() && !s.isBlank();
        }
        return o != null;
    }
}
