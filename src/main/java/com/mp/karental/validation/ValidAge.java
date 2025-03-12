package com.mp.karental.validation;

import com.mp.karental.validation.validator.ValidAgeValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

/**
 * Custom annotation to validate the minimum age requirement.
 * Ensures that the user's date of birth is at least the specified minimum age.
 */
@Documented
@Constraint(validatedBy = ValidAgeValidator.class) // Links to the validator class
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidAge {
    String message() default "INVALID_DATE_OF_BIRTH"; // Default error message
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
    int min(); // Minimum age required
}