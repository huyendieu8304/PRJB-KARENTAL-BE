package com.mp.karental.validation;

import com.mp.karental.validation.validator.UniqueEmailValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom annotation used to validate that an email address has not already been used for an existing account.
 * <p>
 * This annotation leverages the {@link UniqueEmailValidator} to perform the validation logic.
 * It can be applied to fields and ensures that the annotated email value is unique in the system.
 * </p>
 *
 * <p>Usage example:</p>
 * <pre>
 *     &#64;UniqueEmail(message = "Email is already in use")
 *     private String email;
 * </pre>
 *
 * <p>
 * The default message is: "{The email must be unique}".
 * </p>
 *
 * @see UniqueEmailValidator
 *
 * @author DieuTTH4
 *
 * @version 1.0
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = UniqueEmailValidator.class)
public @interface UniqueEmail {

    /**
     * The default error message when validation fails.
     *
     * @return the error message
     */
    String message() default "{The email must be unique}";

    /**
     * Allows the specification of validation groups, to which this constraint belongs.
     *
     * @return an array of groups the constraint belongs to
     */
    Class<?>[] groups() default {};

    /**
     * Can be used by clients of the Bean Validation API to assign custom payload objects to a constraint.
     *
     * @return an array of payload classes associated with the constraint
     */
    Class<? extends Payload>[] payload() default {};

}
