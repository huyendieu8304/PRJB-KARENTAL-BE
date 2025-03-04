package com.mp.karental.validation.validator;

import com.mp.karental.validation.ValidNumberOfSeats;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.List;

/**
 * Validator for checking if the provided number of seats is valid.
 * Only specific seat numbers are allowed (4, 5, or 7).
 * QuangPM20
 * @version 1.0
 */
@RequiredArgsConstructor
public class NumberOfSeatValidator implements ConstraintValidator<ValidNumberOfSeats, Integer> {
    // List of allowed seat numbers
    private static final List<Integer> ALLOWED_SEATS = Arrays.asList(4, 5, 7);

    /**
     * Validates whether the provided number of seats is in the list of allowed values.
     *
     * @param value   The number of seats to validate.
     * @param context The validation context.
     * @return true if the number of seats is valid, false otherwise.
     */
    @Override
    public boolean isValid(Integer value, ConstraintValidatorContext context) {
        return ALLOWED_SEATS.contains(value);
    }
}
