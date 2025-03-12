package com.mp.karental.validation;

import com.mp.karental.validation.validator.AddressComponentValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

/**
 * Annotation for validating address components (City, District, Ward).
 */
@Documented
@Constraint(validatedBy = AddressComponentValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidAddressComponent {
    String message() default "Invalid address component";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
