package com.mp.karental.validation.validator;

import com.mp.karental.repository.UserProfileRepository;
import com.mp.karental.validation.UniquePhoneNumber;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UniquePhoneNumberValidator implements ConstraintValidator<UniquePhoneNumber, String> {

    private final UserProfileRepository userProfileRepository;

    @Override
    public boolean isValid(String s, ConstraintValidatorContext constraintValidatorContext) {
        //find whether the phonenumber exist in the db or not
        return userProfileRepository.findByPhoneNumber(s).isEmpty();
    }
}
