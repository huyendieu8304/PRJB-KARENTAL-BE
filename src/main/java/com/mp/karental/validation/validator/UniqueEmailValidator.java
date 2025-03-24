package com.mp.karental.validation.validator;

import com.mp.karental.repository.AccountRepository;
import com.mp.karental.validation.UniqueEmail;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;


/**
 * Validator class that checks if an email address is unique within the system.
 * <p>
 * This class implements the {@link ConstraintValidator} interface for the {@link UniqueEmail} annotation,
 * leveraging the {@link AccountRepository} to verify whether the given email already exists in the database.
 * </p>
 *
 * <p>
 * Example usage:
 * <pre>
 * &#64;UniqueEmail(message = "Email is already in use")
 * private String email;
 * </pre>
 * </p>
 *
 * @author DieuTTH4
 *
 * @version 1.0
 */
@Component
@RequiredArgsConstructor
public class UniqueEmailValidator implements ConstraintValidator<UniqueEmail, String> {

    private final AccountRepository accountRepository;


    /**
     * Validates that the provided email is unique.
     *
     * @param s the email address to validate
     * @param constraintValidatorContext context in which the constraint is evaluated
     * @return {@code true} if the email is not present in the repository (i.e., unique), {@code false} otherwise
     *
     * @author DieuTTH4
     *
     * @version 1.0
     */
    @Override
    public boolean isValid(String s, ConstraintValidatorContext constraintValidatorContext) {
        return accountRepository.findByEmail(s).isEmpty();
    }
}
