package com.mp.karental.validation.validator;
import com.mp.karental.validation.ValidAge;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.time.LocalDate;
import java.time.Period;

/**
 * Validator for @ValidAge annotation.
 * Ensures that the user's age is at least the specified minimum age.
 */
public class ValidAgeValidator implements ConstraintValidator<ValidAge, LocalDate> {
    private int minAge;

    @Override
    public void initialize(ValidAge constraintAnnotation) {
        this.minAge = constraintAnnotation.min(); // Retrieves the minimum age from annotation
    }

    @Override
    public boolean isValid(LocalDate dob, ConstraintValidatorContext context) {
        if (dob == null) {
            return false; // Date of birth is required and cannot be null
        }
        return Period.between(dob, LocalDate.now()).getYears() >= minAge; // Checks if age >= minAge
    }
}