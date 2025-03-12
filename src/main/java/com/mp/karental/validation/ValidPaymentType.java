package com.mp.karental.validation;

import com.mp.karental.validation.validator.PaymentTypeValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

/**
 * Custom annotation to validate the payment type.
 * Ensures that the provided payment type is valid.
 */
@Documented
@Constraint(validatedBy = PaymentTypeValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidPaymentType {

    /**
     * Default validation message.
     */
    String message() default "Invalid payment type";

    /**
     * Validation groups.
     */
    Class<?>[] groups() default {};

    /**
     * Payload for additional metadata.
     */
    Class<? extends Payload>[] payload() default {};
}
