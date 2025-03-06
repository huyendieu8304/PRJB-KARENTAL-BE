package com.mp.karental.validation;

import com.mp.karental.validation.validator.AddressValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom validation annotation to validate the address format.
 * This annotation ensures that an address follows the expected structure:
 * ward, district, city, house number, street.
 * <p>
 * It uses {@link AddressValidator} for the validation logic.
 *
 * QuangPM20
 * @version 1.0
 */
@Constraint(validatedBy = AddressValidator.class)  // Links to the custom validator
@Target({ ElementType.FIELD, ElementType.PARAMETER }) // Can be used on fields and method parameters
@Retention(RetentionPolicy.RUNTIME) // Retained at runtime for reflection-based validation
public @interface ValidAddress {

    /**
     * The default error message if the address format is invalid.
     *
     * @return The error message string.
     */
    String message() default "Invalid address format. Expected format: ward, district, city, house number, street";

    /**
     * Defines validation groups for more granular control over validation.
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
