package com.mp.karental.validation;

import com.mp.karental.validation.validator.UniqueLicensePlateValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom validation annotation to ensure that a license plate is unique.
 * This annotation is applied to fields that represent license plates.
 * It uses {@link UniqueLicensePlateValidator} to perform the validation.
 *
 * QuangPM20
 * @version 1.0
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = UniqueLicensePlateValidator.class)
public @interface UniqueLicensePlate {

    /**
     * The default error message when validation fails.
     *
     * @return The error message.
     */
    String message() default "{The license plate must be unique}";

    /**
     * Defines validation groups for more granular validation control.
     *
     * @return The groups.
     */
    Class<?>[] groups() default {};

    /**
     * Additional metadata for validation payload.
     *
     * @return The payload classes.
     */
    Class<? extends Payload>[] payload() default {};
}
