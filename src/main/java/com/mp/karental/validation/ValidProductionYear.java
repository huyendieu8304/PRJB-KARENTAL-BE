package com.mp.karental.validation;

import com.mp.karental.validation.validator.ProductionYearValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = ProductionYearValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidProductionYear {
    String message() default "{Your production year were not predefined}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
