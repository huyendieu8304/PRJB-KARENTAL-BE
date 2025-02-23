package com.mp.karental.validation;

import com.mp.karental.validation.validator.UniqueEmailValidator;
import com.mp.karental.validation.validator.UniqueLicensePlateValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = UniqueLicensePlateValidator.class)
public @interface UniqueLicensePlate {
    String message() default "{The license plate must be unique}";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
