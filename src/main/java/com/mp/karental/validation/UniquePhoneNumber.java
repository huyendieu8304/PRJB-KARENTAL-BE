package com.mp.karental.validation;

import com.mp.karental.validation.validator.UniquePhoneNumberValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is used to validate whether the phone number has been used for a account
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = UniquePhoneNumberValidator.class)
public @interface UniquePhoneNumber {
    String message() default "{The phone number must be unique}";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
