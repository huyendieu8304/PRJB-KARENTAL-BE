package com.mp.karental.validation.validator;

import com.mp.karental.validation.ValidStatusEdit;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validator class for the {@link ValidStatusEdit} annotation.
 * Ensures that the status value can only be "available" or "stopped".
 * @author QuangPM20
 *
 * @version 1.0
 */
public class StatusEditValidator implements ConstraintValidator<ValidStatusEdit, String> {

    /**
     * Validates the given status value.
     *
     * @param value   The status string to validate.
     * @param context The validation context.
     * @return {@code true} if the status is either "available" or "stopped", otherwise {@code false}.
     */
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return value != null && (value.equalsIgnoreCase("available") || value.equalsIgnoreCase("stopped"));
    }
}
