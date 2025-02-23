package com.mp.karental.validation.validator;

import com.mp.karental.constant.EAdditionalFunctions;
import com.mp.karental.service.AllowedValuesService;
import com.mp.karental.validation.ValidAdditionalFunction;
import com.mp.karental.validation.ValidColor;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class AdditionalFunctionValidator implements ConstraintValidator<ValidAdditionalFunction, String> {
    private static final Set<String> ALLOWED_FUNCTIONS = Arrays.stream(EAdditionalFunctions.values())
            .map(EAdditionalFunctions::getName)
            .collect(Collectors.toSet());

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // If the value is null, it's considered valid
        if (value == null) {
            return true;
        }

        // Split the comma-separated string into a list of functions
        List<String> functions = Arrays.asList(value.split(", "));

        // Validate that each function is in the allowed functions set
        return functions.stream()
                .map(String::trim)  // Trim spaces around the functions
                .allMatch(ALLOWED_FUNCTIONS::contains);
    }
}
