package com.mp.karental.validation;

import com.mp.karental.validation.validator.FileCarImageValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom validation annotation for validating car image file formats.
 * <p>
 * This annotation ensures that uploaded images for cars have valid file types.
 * It is validated by {@link FileCarImageValidator}, which contains the actual validation logic.
 * <p>
 * Accepted file formats: .jpg, .jpeg, .png, .gif
 *
 * QuangPM20
 * @version 1.0
 */
@Constraint(validatedBy = FileCarImageValidator.class) // Specifies the validator class
@Target({ElementType.FIELD}) // Can be applied to fields only
@Retention(RetentionPolicy.RUNTIME) // Retained at runtime for reflection-based validation
public @interface ValidImageCar {

    /**
     * Default error message when the uploaded file type is invalid.
     *
     * @return The error message string.
     */
    String message() default "Invalid file type. Accepted formats are .jpg, .jpeg, .png, .gif";

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
