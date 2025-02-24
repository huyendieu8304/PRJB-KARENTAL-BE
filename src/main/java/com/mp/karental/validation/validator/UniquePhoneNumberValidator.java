package com.mp.karental.validation.validator;

import com.mp.karental.repository.UserProfileRepository;
import com.mp.karental.validation.UniquePhoneNumber;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;

/**
 * Validator class that checks if a phone number is unique within the system.
 * <p>
 * This class implements the {@link ConstraintValidator} interface for the {@link UniquePhoneNumber}
 * annotation, using the {@link UserProfileRepository} to verify whether the given phone number already
 * exists in the database.
 * </p>
 *
 * <p>
 * Example usage:
 * <pre>
 * &#64;UniquePhoneNumber(message = "Phone number is already in use")
 * private String phoneNumber;
 * </pre>
 * </p>
 *
 * @author DieuTTH4
 *
 * @version 1.0
 */
@RequiredArgsConstructor
public class UniquePhoneNumberValidator implements ConstraintValidator<UniquePhoneNumber, String> {

    private final UserProfileRepository userProfileRepository;

    /**
     * Validates that the provided phone number is unique.
     *
     * @param s the phone number to validate
     * @param constraintValidatorContext context in which the constraint is evaluated
     * @return {@code true} if the phone number is not present in the repository (i.e., unique), {@code false} otherwise
     *
     * @author DieuTTH4
     *
     * @version 1.0
     */
    @Override
    public boolean isValid(String s, ConstraintValidatorContext constraintValidatorContext) {
        //find whether the phonenumber exist in the db or not
        return userProfileRepository.findByPhoneNumber(s).isEmpty();
    }
}
