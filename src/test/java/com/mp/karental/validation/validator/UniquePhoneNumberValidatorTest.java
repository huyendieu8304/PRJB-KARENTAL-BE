package com.mp.karental.validation.validator;

import com.mp.karental.entity.UserProfile;
import com.mp.karental.repository.UserProfileRepository;
import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
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
 *     Check whether the validator could check the unique of phone number or not
 * </p>
 *
 * @author DieuTTH4
 *
 * @version 1.0
 */
@ExtendWith(MockitoExtension.class)
class UniquePhoneNumberValidatorTest {

    @Mock
    private UserProfileRepository userProfileRepository;
    @Mock
    private ConstraintValidatorContext context;
    @InjectMocks
    private UniquePhoneNumberValidator validator;


    @ParameterizedTest(name = "[{index} phoneNumber={0} -> expected valid? {1}]") //set name for tc
    @CsvSource({
            "0123456789, true", //phone number not exist
            "0123456788, false" //phone number existed
    })
    void isValid(String phoneNumber, boolean expected) {
        //Mock up
        if (expected) {
            when(userProfileRepository.findByPhoneNumber(phoneNumber)).thenReturn(Optional.empty());
        } else {
            when(userProfileRepository.findByPhoneNumber(phoneNumber)).thenReturn(Optional.of(new UserProfile()));
        }
        //Call the method
        boolean result = validator.isValid(phoneNumber, context);
        //Assert
        assertEquals(expected, result);
    }
}