package com.mp.karental.validation.validator;

import com.mp.karental.service.AllowedValuesService;
import com.mp.karental.validation.ValidColor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.List;

@RequiredArgsConstructor
public class ColorValidator implements ConstraintValidator<ValidColor, String> {


    private final AllowedValuesService allowedValuesService;

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        List<String> allowedColors = allowedValuesService.getAllowedValuesOfColor();
        //System.out.println("Allowed Colors: " + allowedColors);  // Debugging

        boolean isValid = allowedColors.stream()
                .anyMatch(allowedColor -> allowedColor.equalsIgnoreCase(value));

        return isValid;
    }
}
