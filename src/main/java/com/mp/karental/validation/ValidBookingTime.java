package com.mp.karental.validation;

import com.mp.karental.validation.validator.BookingTimeValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Custom validation annotation to check if the booking time is valid.
 * This annotation ensures that the pick-up and drop-off times follow
 * the business rules defined in {@link BookingTimeValidator}.
 */
@Documented
@Constraint(validatedBy = BookingTimeValidator.class) // Specifies the validator class that will handle validation logic
@Target({ElementType.TYPE}) // Applicable to class-level (BookingRequest)
@Retention(RetentionPolicy.RUNTIME) // The annotation will be available at runtime for reflection processing
public @interface ValidBookingTime {

    /**
     * Default validation message when the constraint is violated.
     */
    String message() default "Invalid booking time";

    /**
     * Allows specifying validation groups. This feature is useful
     * for applying different validation rules in different contexts.
     */
    Class<?>[] groups() default {};

    /**
     * Can be used by clients to associate additional metadata with
     * the constraint (e.g., severity levels, error codes).
     */
    Class<? extends Payload>[] payload() default {};
}
