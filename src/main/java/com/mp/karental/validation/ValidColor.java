package com.mp.karental.validation;

import com.mp.karental.validation.validator.ColorValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom validation annotation to ensure that a given color is valid.
 * This annotation is linked to {@link ColorValidator}, which contains the validation logic.
 * <p>
 * It can be applied to fields that require color validation.
 *
 * QuangPM20
 * @version 1.0
 */
@Constraint(validatedBy = ColorValidator.class) // Specifies the validator class
@Target({ElementType.FIELD}) // Can be applied to fields only
@Retention(RetentionPolicy.RUNTIME) // Retained at runtime for reflection-based validation
public @interface ValidColor {

    /**
     * Default error message when the provided color is invalid.
     *
     * @return The error message string.
     */
    String message() default "{Your color were not predefined}";

    /**
     * Defines validation groups for applying different validation constraints.
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
