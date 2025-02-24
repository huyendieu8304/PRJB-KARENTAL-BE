package com.mp.karental.validation;
import com.mp.karental.validation.validator.FileCarImageValidator;
import com.mp.karental.validation.validator.FileDocumentValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = FileCarImageValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidImageCar {
    String message() default "Invalid file type. Accepted formats are .jpg, .jpeg, .png, .gif";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
