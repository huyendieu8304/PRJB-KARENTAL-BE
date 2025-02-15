package com.mp.karental.validation;

import com.mp.karental.validation.validator.UniquePhoneNumberValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom annotation used to validate that a phone number has not already been used for an existing account.
 * <p>
 * This annotation leverages the {@link UniquePhoneNumberValidator} to implement the validation logic.
 * It ensures that the annotated phone number value is unique in the system.
 * </p>
 *
 * <p>Usage example:</p>
 * <pre>
 *     &#64;UniquePhoneNumber(message = "Phone number is already in use")
 *     private String phoneNumber;
 * </pre>
 *
 * <p>
 * The default validation message is: "{The phone number must be unique}".
 * </p>
 *
 * @see UniquePhoneNumberValidator
 *
 * @author DieuTTH4
 *
 * @version 1.0
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = UniquePhoneNumberValidator.class)
public @interface UniquePhoneNumber {

    /**
     * The default error message when validation fails.
     *
     * @return the error message
     */
    String message() default "{The phone number must be unique}";

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
