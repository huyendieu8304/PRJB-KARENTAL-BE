package com.mp.karental.validation.validator;

import com.mp.karental.validation.ValidAddress;
import com.mp.karental.service.ExcelService;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.regex.Pattern;

@RequiredArgsConstructor
public class AddressValidator implements ConstraintValidator<ValidAddress, String> {

    private final ExcelService excelService;

    // Define the regex pattern for the address format
    private static final String ADDRESS_REGEX = "^\\s*([\\p{L}\\s]+),\\s*([\\p{L}\\s]+),\\s*([\\p{L}\\s]+),\\s*(\\d+),\\s*([\\p{L}\\s]+)\\s*$";
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isEmpty()) {
            return false;  // Reject null or empty address
        }

        // Validate the address format using regex
        if (!Pattern.matches(ADDRESS_REGEX, value)) {
            return false;  // If format doesn't match, return false
        }

        // Extract the components of the address
        String[] addressParts = value.split(",");
        if (addressParts.length != 5) {
            return false;  // If the address doesn't have exactly 5 parts, it's invalid
        }

        // Extract city, district, and ward from the address parts
        String city = addressParts[0].trim();
        String district = addressParts[1].trim();
        String ward = addressParts[2].trim();

        // Validate the city, district, and ward using data from ExcelService
        return isValidCity(city) && isValidDistrict(district) && isValidWard(ward)
                && isValidDistrictForCity(district, city)
                && isValidWardForDistrict(ward, district);
    }

    // Validate if the city is valid based on ExcelService
    private boolean isValidCity(String city) {
        List<String> validCities = excelService.getAllCities();  // Fetch the valid cities from the ExcelService
        return validCities.contains(city);
    }

    // Validate if the district is valid based on ExcelService
    private boolean isValidDistrict(String district) {
        List<String> validDistricts = excelService.getAllDistricts();  // Fetch the valid districts from the ExcelService
        return validDistricts.contains(district);
    }

    // Validate if the ward is valid based on ExcelService
    private boolean isValidWard(String ward) {
        List<String> validWards = excelService.getAllWards();  // Fetch the valid wards from the ExcelService
        return validWards.contains(ward);
    }

    // Validate if the district is valid for the given city based on ExcelService
    private boolean isValidDistrictForCity(String district, String city) {
        List<String> validDistricts = excelService.getDistrictsByCity(city).stream().toList();  // Fetch the valid districts for the given city
        return validDistricts.contains(district);
    }

    // Validate if the ward is valid for the given district based on ExcelService
    private boolean isValidWardForDistrict(String ward, String district) {
        List<String> validWards = excelService.getWardsByDistrict(district).stream().toList();  // Fetch the valid wards for the given district
        return validWards.contains(ward);
    }
}
