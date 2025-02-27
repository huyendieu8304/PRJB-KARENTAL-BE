package com.mp.karental.validation;

import com.mp.karental.validation.validator.RequireFieldValidator;
import com.mp.karental.validation.validator.UniqueEmailValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = RequireFieldValidator.class)
public @interface RequiredField {

    String message() default "REQUIRED_FIELD";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
    String fieldName();

}
