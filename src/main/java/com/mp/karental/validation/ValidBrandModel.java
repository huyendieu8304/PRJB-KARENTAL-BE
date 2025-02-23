package com.mp.karental.validation;

import com.mp.karental.validation.validator.BrandAndModelValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = BrandAndModelValidator.class)
@Target({ElementType.CONSTRUCTOR, ElementType.FIELD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidBrandModel {

    // Default message for invalid brand-model combination
    String message() default "Invalid brand-model combination.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
