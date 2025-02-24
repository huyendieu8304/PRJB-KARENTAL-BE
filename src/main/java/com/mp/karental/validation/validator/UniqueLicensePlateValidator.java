package com.mp.karental.validation.validator;

import com.mp.karental.repository.CarRepository;
import com.mp.karental.validation.UniqueLicensePlate;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UniqueLicensePlateValidator implements ConstraintValidator<UniqueLicensePlate, String> {
    private final CarRepository carRepository;
    @Override
    public boolean isValid(String s, ConstraintValidatorContext constraintValidatorContext) {
        return carRepository.findByLicensePlate(s).isEmpty();
    }
}
