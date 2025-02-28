package com.mp.karental.validation.validator;

import com.mp.karental.constant.EAdditionalFunctions;
import com.mp.karental.validation.ValidAdditionalFunction;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Validator class for additional functions.
 * Ensures that input values match predefined additional functions.
 * @author QuangPM20
 *
 * @version 1.0
 */
@RequiredArgsConstructor
public class AdditionalFunctionValidator implements ConstraintValidator<ValidAdditionalFunction, String> {

    /**
     * Set of allowed additional function names retrieved from the EAdditionalFunctions enum.
     */
    private static final Set<String> ALLOWED_FUNCTIONS = Arrays.stream(EAdditionalFunctions.values())
            .map(EAdditionalFunctions::getName)
            .collect(Collectors.toSet());

    /**
     * Set of normalized function names (whitespace removed, converted to lowercase)
     * to allow case-insensitive and whitespace-agnostic validation.
     */
    private static final Set<String> NORMALIZED_FUNCTIONS = ALLOWED_FUNCTIONS.stream()
            .map(name -> name.replaceAll("\\s+", "").toLowerCase()) // Remove spaces and convert to lowercase
            .collect(Collectors.toSet());

    /**
     * Checks whether the given input string is a valid additional function.
     * Normalizes the input (removes spaces and converts to lowercase) before validation.
     *
     * @param input The input string representing an additional function.
     * @return true if the function is allowed, false otherwise.
     */
    private boolean isAllowedFunction(String input) {
        String normalizedInput = input.replaceAll("\\s+", "").toLowerCase();
        return NORMALIZED_FUNCTIONS.contains(normalizedInput);
    }

    /**
     * Validates the input string against the allowed additional functions.
     * The input can be a comma-separated list of function names.
     *
     * @param value   The input value to validate.
     * @param context The validation context.
     * @return true if all provided functions are valid, false otherwise.
     */
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isEmpty()) {
            return true; // Allow empty values (assumed to be optional)
        }

        List<String> functions = Arrays.asList(value.split(",")); // Split input into a list of functions

        return functions.stream()
                .map(String::trim) // Remove leading/trailing spaces
                .allMatch(this::isAllowedFunction); // Validate each function
    }
}
