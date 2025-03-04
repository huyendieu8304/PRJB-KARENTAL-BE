package com.mp.karental.validation;

import com.mp.karental.validation.validator.StatusEditValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom annotation for validating status values.
 * Ensures that the status can only be "available" or "stopped".
 * @author QuangPM20
 *
 * @version 1.0
 */
@Constraint(validatedBy = StatusEditValidator.class) // Specifies the validator class that will handle the validation logic
@Target({ElementType.FIELD}) // This annotation can only be applied to fields
@Retention(RetentionPolicy.RUNTIME) // The annotation will be retained at runtime and available for reflection
public @interface ValidStatusEdit {

    /**
     * The default error message when validation fails.
     *
     * @return The validation error message.
     */
    String message() default "{Status can edit only available/stopped}";

    /**
     * Defines validation groups. This is used for grouping different validation constraints.
     *
     * @return An array of validation groups.
     */
    Class<?>[] groups() default {};

    /**
     * Defines additional metadata for validation, typically used for integration with frameworks like Spring.
     *
     * @return An array of payload classes.
     */
    Class<? extends Payload>[] payload() default {};
}
