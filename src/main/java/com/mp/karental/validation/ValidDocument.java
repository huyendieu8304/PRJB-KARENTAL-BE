package com.mp.karental.validation;
import com.mp.karental.validation.validator.FileDocumentValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = FileDocumentValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidDocument {
    String message() default "Invalid file type. Accepted formats are .doc, .docx, .pdf, .jpeg, .jpg, .png";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
