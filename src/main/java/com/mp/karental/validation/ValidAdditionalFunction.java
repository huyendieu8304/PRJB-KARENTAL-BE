package com.mp.karental.validation;

import com.mp.karental.validation.validator.AdditionalFunctionValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;

@Constraint(validatedBy = AdditionalFunctionValidator.class)
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidAdditionalFunction {
    String message() default "Invalid additional function(s)";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

