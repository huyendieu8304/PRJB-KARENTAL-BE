package com.mp.karental.validation;

import com.mp.karental.validation.validator.ModelValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom validation annotation for verifying if a car model is predefined.
 * <p>
 * This annotation ensures that the provided car model exists in the predefined list
 * by using the {@link ModelValidator} class for validation.
 * <p>
 * It is typically applied to fields in DTOs or entity classes to validate input data.
 *
 * QuangPM20
 * @version 1.0
 */
@Constraint(validatedBy = ModelValidator.class) // Specifies the validator class responsible for validation logic
@Target({ElementType.FIELD}) // Applicable only to fields
@Retention(RetentionPolicy.RUNTIME) // Available at runtime for reflection-based validation
public @interface ValidModel {

    /**
     * Default error message when the provided model is not in the predefined list.
     *
     * @return The error message string.
     */
    String message() default "{Your model were not predefined}";

    /**
     * Defines validation groups for grouping different validation constraints.
     *
     * @return The groups for validation.
     */
    Class<?>[] groups() default {};

    /**
     * Additional metadata for validation payload.
     *
     * @return The payload classes.
     */
    Class<? extends Payload>[] payload() default {};
}
