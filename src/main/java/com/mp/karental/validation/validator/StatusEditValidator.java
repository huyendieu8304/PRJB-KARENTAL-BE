package com.mp.karental.validation.validator;

import com.mp.karental.constant.ECarStatus;
import com.mp.karental.validation.ValidStatusEdit;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Set;

/**
 * Validator class for the {@link ValidStatusEdit} annotation.
 * Ensures that the status value can only be "not verified" or "stopped".
 * @author QuangPM20
 *
 * @version 1.0
 */
public class StatusEditValidator implements ConstraintValidator<ValidStatusEdit, String> {
    // Define allowed values in uppercase
    private static final Set<String> ALLOWED_STATUSES = Set.of("NOT_VERIFIED", "STOPPED");

    /**
     * Validates the given status value.
     *
     * @param value   The status string to validate.
     * @param context The validation context.
     * @return {@code true} if the status is either "not verified" or "stopped", otherwise {@code false}.
     */
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // Ensure value is not null and check case-insensitive match
        return value != null && ALLOWED_STATUSES.contains(value.trim());
    }
}
