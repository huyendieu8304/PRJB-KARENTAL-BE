package com.mp.karental.validation;

import com.mp.karental.validation.validator.NumberOfSeatValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom validation annotation for ensuring that the number of seats in a car is within a predefined list.
 * <p>
 * This annotation is validated by the {@link NumberOfSeatValidator} class, which checks whether
 * the provided number of seats is one of the allowed values (e.g., 4, 5, 7).
 * <p>
 * It is used in DTOs or entity classes to enforce validation constraints on seat numbers.
 *
 * QuangPM20
 * @version 1.0
 */
@Constraint(validatedBy = NumberOfSeatValidator.class) // Specifies the validator responsible for validation logic
@Target({ElementType.FIELD}) // Can be applied to fields only
@Retention(RetentionPolicy.RUNTIME) // Available at runtime for validation
public @interface ValidNumberOfSeats {

    /**
     * Default error message when the provided number of seats is not in the predefined list.
     *
     * @return The error message string.
     */
    String message() default "{Your number of seat were not predefined}";

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
