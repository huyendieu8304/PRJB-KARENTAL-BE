package com.mp.karental.validation.validator;

import com.mp.karental.entity.Account;
import com.mp.karental.repository.AccountRepository;
import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * This is a class used to test custom validator
 * <p>
 *     Check whether the validator could check the unique of email or not
 * </p>
 *
 * @author DieuTTH4
 *
 * @version 1.0
 */
@ExtendWith(MockitoExtension.class)
class UniqueEmailValidatorTest {

    @Mock
    private AccountRepository accountRepository;
    @Mock
    private ConstraintValidatorContext context;
    @InjectMocks
    private UniqueEmailValidator validator;

    @ParameterizedTest(name = "[{index} email={0} -> expected valid? {1}]") //set name for tc
    @CsvSource({
            "unique@example.com, true", //email not exist
            "exist@example.com, false" //email existed
    })
    void testIsValid(String email, boolean expectedValid) {
        //Set up repo's behaviour depend on input
        if (expectedValid) {
            when(accountRepository.findByEmail(email)).thenReturn(Optional.empty());
        } else {
            when(accountRepository.findByEmail(email)).thenReturn(Optional.of(new Account()));
        }

        //Call the method
        boolean result = validator.isValid(email, context);
        //Assert
        assertEquals(expectedValid, result);
    }
}