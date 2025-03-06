package com.mp.karental.validation.validator;

import com.mp.karental.constant.EColors;
import com.mp.karental.validation.ValidColor;
import lombok.RequiredArgsConstructor;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Validator for checking if a given color is valid.
 * The valid colors are retrieved from the {@link EColors} enum.
 * QuangPM20
 * @version 1.0
 */
@RequiredArgsConstructor
public class ColorValidator implements ConstraintValidator<ValidColor, String> {

    /**
     * Retrieves a list of all valid color names from the {@link EColors} enum.
     *
     * @return A list of valid color names as strings.
     */
    public List<String> getAllowedValuesOfColor() {
        return Arrays.stream(EColors.values())
                .map(Enum::name)
                .collect(Collectors.toList());
    }

    /**
     * Checks if the provided color exists in the list of valid colors.
     *
     * @param value   The color name to validate.
     * @param context The validation context.
     * @return true if the color is valid, false otherwise.
     */
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        List<String> allowedColors = getAllowedValuesOfColor();

        // Validate color by ignoring case sensitivity
        return allowedColors.stream()
                .anyMatch(allowedColor -> allowedColor.equalsIgnoreCase(value));
    }
}
