package com.mp.karental.validation.validator;

import com.mp.karental.repository.AccountRepository;
import com.mp.karental.validation.UniqueEmail;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UniqueEmailValidator implements ConstraintValidator<UniqueEmail, String> {

    private final AccountRepository accountRepository;

    @Override
    public boolean isValid(String s, ConstraintValidatorContext constraintValidatorContext) {
        return accountRepository.findByEmail(s).isEmpty();
    }
}
