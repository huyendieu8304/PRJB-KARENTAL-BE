package com.mp.karental.validation;

import com.mp.karental.validation.validator.ColorValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = ColorValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidColor {
    String message() default "{Your color were not predefined}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
