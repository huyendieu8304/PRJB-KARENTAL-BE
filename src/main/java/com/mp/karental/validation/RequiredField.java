package com.mp.karental.validation;

import com.mp.karental.validation.validator.RequiredFieldValidator;
import com.mp.karental.validation.validator.UniqueEmailValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom annotation used to validate that a field has data.
 * <p>
 * This annotation leverages the {@link RequiredFieldValidator} to perform the validation logic.
 * It can be applied to fields and ensures that the annotated field has data.
 * </p>
 *
 * <p>
 * Example usage:
 * <pre>
 * &#64;RequiredField(fieldName = "Email)
 * private String email;
 * </pre>
 * </p>
 *
 * @see RequiredFieldValidator
 *
 * @author DieuTTH4
 *
 * @version 1.0
 */
@Target({ElementType.PARAMETER, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = RequiredFieldValidator.class)
public @interface RequiredField {

    String message() default "REQUIRED_FIELD";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
    String fieldName() default  "This field";

}
