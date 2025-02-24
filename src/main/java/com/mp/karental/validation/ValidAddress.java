package com.mp.karental.validation;

import com.mp.karental.validation.validator.AddressValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = AddressValidator.class)  // Link to the custom validator
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidAddress {

    String message() default "Invalid address format. Expected format: ward, district, city, house number, street";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}

