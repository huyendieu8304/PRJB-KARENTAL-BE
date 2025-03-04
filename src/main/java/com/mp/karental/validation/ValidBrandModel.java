package com.mp.karental.validation;

import com.mp.karental.validation.validator.BrandAndModelValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom validation annotation to ensure that a given brand and model combination is valid.
 * This annotation is linked to {@link BrandAndModelValidator}, which contains the validation logic.
 * <p>
 * It can be applied to constructors, fields, or types (classes).
 *
 * QuangPM20
 * @version 1.0
 */
@Constraint(validatedBy = BrandAndModelValidator.class) // Specifies the validator class
@Target({ElementType.CONSTRUCTOR, ElementType.FIELD, ElementType.TYPE}) // Applicable to constructors, fields, and types
@Retention(RetentionPolicy.RUNTIME) // Retained at runtime for reflection-based validation
public @interface ValidBrandModel {

    /**
     * Default error message when the brand-model combination is invalid.
     *
     * @return The error message string.
     */
    String message() default "Invalid brand-model combination.";

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
