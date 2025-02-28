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

    private static final Set<String> NORMALIZED_FUNCTIONS = ALLOWED_FUNCTIONS.stream()
            .map(name -> name.replaceAll("\\s+", "").toLowerCase()) // Bỏ khoảng trắng và chuyển về chữ thường
            .collect(Collectors.toSet());

    private boolean isAllowedFunction(String input) {
        String normalizedInput = input.replaceAll("\\s+", "").toLowerCase();
        return NORMALIZED_FUNCTIONS.contains(normalizedInput);
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }

        List<String> functions = Arrays.asList(value.split(","));

        return functions.stream()
                .map(String::trim)
                .allMatch(this::isAllowedFunction);
    }
}
