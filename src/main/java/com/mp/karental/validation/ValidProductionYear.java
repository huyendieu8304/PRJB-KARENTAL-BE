package com.mp.karental.validation;

import com.mp.karental.validation.validator.ProductionYearValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom validation annotation to ensure the production year of a car falls within a valid range.
 * <p>
 * This annotation is validated by {@link ProductionYearValidator}, which checks whether
 * the provided production year is within the predefined range (e.g., 1990 - 2030).
 * <p>
 * It is typically used in DTOs or entity classes to enforce constraints on car production years.
 *
 * QuangPM20
 * @version 1.0
 */
@Constraint(validatedBy = ProductionYearValidator.class) // Specifies the validator class
@Target({ElementType.FIELD}) // Can be applied only to fields
@Retention(RetentionPolicy.RUNTIME) // Retained at runtime for validation
public @interface ValidProductionYear {

    /**
     * Default error message when the production year is outside the predefined range.
     *
     * @return The error message string.
     */
    String message() default "{Your production year were not predefined}";

    /**
     * Defines validation groups for grouping different constraints.
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
