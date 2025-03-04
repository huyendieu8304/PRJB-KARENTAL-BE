package com.mp.karental.validation;

import com.mp.karental.validation.validator.FileDocumentValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom validation annotation to ensure that an uploaded document has a valid file type.
 * <p>
 * This annotation is linked to {@link FileDocumentValidator}, which contains the validation logic.
 * It can be applied to fields representing uploaded files.
 *
 * Supported file formats: .doc, .docx, .pdf, .jpeg, .jpg, .png
 *
 * QuangPM20
 * @version 1.0
 */
@Constraint(validatedBy = FileDocumentValidator.class) // Specifies the validator class
@Target({ElementType.FIELD}) // Can be applied to fields only
@Retention(RetentionPolicy.RUNTIME) // Retained at runtime for reflection-based validation
public @interface ValidDocument {

    /**
     * Default error message when the provided file type is invalid.
     *
     * @return The error message string.
     */
    String message() default "Invalid file type. Accepted formats are .doc, .docx, .pdf, .jpeg, .jpg, .png";

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
