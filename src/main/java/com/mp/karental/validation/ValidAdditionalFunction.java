package com.mp.karental.validation;

import com.mp.karental.validation.validator.AdditionalFunctionValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom validation annotation to ensure that the provided additional functions are valid.
 * This annotation can be applied to fields or method parameters.
 * It utilizes {@link AdditionalFunctionValidator} for the validation logic.
 *
 * QuangPM20
 * @version 1.0
 */
@Constraint(validatedBy = AdditionalFunctionValidator.class)
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidAdditionalFunction {

    /**
     * The default error message when validation fails.
     *
     * @return The error message.
     */
    String message() default "Invalid additional function(s)";

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
