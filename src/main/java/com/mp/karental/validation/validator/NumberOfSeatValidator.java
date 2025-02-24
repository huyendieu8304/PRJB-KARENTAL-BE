package com.mp.karental.validation.validator;

import com.mp.karental.validation.ValidNumberOfSeats;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.List;

@RequiredArgsConstructor
public class NumberOfSeatValidator implements ConstraintValidator<ValidNumberOfSeats, Integer> {
    private static final List<Integer> ALLOWED_SEATS = Arrays.asList(4, 5, 7);

    @Override
    public boolean isValid(Integer value, ConstraintValidatorContext context) {
        return ALLOWED_SEATS.contains(value);
    }
}
