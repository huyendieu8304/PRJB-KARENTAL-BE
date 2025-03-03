package com.mp.karental.validation.validator;

import com.mp.karental.repository.AccountRepository;
import com.mp.karental.validation.RequiredField;
import com.mp.karental.validation.UniqueEmail;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;

/**
 * Validator class that checks if a field has data or not.
 * <p>
 * This class implements the {@link ConstraintValidator} interface for the {@link RequiredField} annotation
 * </p>
 *
 * <p>
 * Example usage:
 * <pre>
 * &#64;RequiredField(fieldName = "Email)
 * private String email;
 * </pre>
 * </p>
 *
 * @author DieuTTH4
 *
 * @version 1.0
 */
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
