package com.mp.karental.validation;

import com.mp.karental.validation.validator.BrandValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom validation annotation to ensure that the brand is valid.
 * This annotation checks whether the provided brand exists in the predefined list.
 * <p>
 * It uses {@link BrandValidator} to perform the validation logic.
 *
 * QuangPM20
 * @version 1.0
 */
@Constraint(validatedBy = BrandValidator.class) // Links this annotation to the custom validator
@Target({ElementType.FIELD}) // Can be applied to fields only
@Retention(RetentionPolicy.RUNTIME) // Retained at runtime for reflection-based validation
public @interface ValidBrand {

    /**
     * The default error message if the brand is not predefined.
     *
     * @return The error message string.
     */
    String message() default "{Your brand was not predefined}";

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
