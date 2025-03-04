package com.mp.karental.validation.validator;

import com.mp.karental.repository.CarRepository;
import com.mp.karental.validation.UniqueLicensePlate;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;

/**
 * Validator for ensuring that a license plate is unique in the system.
 * It checks the database to verify if the given license plate already exists.
 * QuangPM20
 * @version 1.0
 */
@RequiredArgsConstructor
public class UniqueLicensePlateValidator implements ConstraintValidator<UniqueLicensePlate, String> {
    private final CarRepository carRepository;

    /**
     * Validates whether the provided license plate is unique.
     *
     * @param s The license plate to validate.
     * @param constraintValidatorContext The validation context.
     * @return true if the license plate does not exist in the database, false otherwise.
     */
    @Override
    public boolean isValid(String s, ConstraintValidatorContext constraintValidatorContext) {
        return carRepository.findByLicensePlate(s).isEmpty();
    }
}
