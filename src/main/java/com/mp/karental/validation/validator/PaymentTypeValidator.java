package com.mp.karental.validation.validator;

import com.mp.karental.constant.EPaymentType;
import com.mp.karental.validation.ValidPaymentType;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.EnumSet;

/**
 * Validator for the {@link ValidPaymentType} annotation.
 * Ensures that the provided payment type is a valid enum value.
 */
public class PaymentTypeValidator implements ConstraintValidator<ValidPaymentType, EPaymentType> {

    @Override
    public boolean isValid(EPaymentType paymentType, ConstraintValidatorContext context) {
        if (paymentType == null) {
            return false; // Prevent null payment type
        }

        // Ensure the payment type exists within the enum
        return EnumSet.allOf(EPaymentType.class).contains(paymentType);
    }
}
