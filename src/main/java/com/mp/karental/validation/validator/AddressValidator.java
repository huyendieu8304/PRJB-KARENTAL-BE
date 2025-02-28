package com.mp.karental.validation.validator;

import com.mp.karental.validation.ValidAddress;
import com.mp.karental.service.ExcelService;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Validator class for validating address format and ensuring the address components
 * (city, district, ward) exist in the predefined dataset from ExcelService.
 *
 * @version 1.0
 */
@RequiredArgsConstructor
public class AddressValidator implements ConstraintValidator<ValidAddress, String> {

    private final ExcelService excelService;

    // Define the regex pattern for the address format
    private static final String ADDRESS_REGEX = "^\\s*([\\p{L}\\s]+),\\s*([\\p{L}\\s]+),\\s*([\\p{L}\\s]+),\\s*(\\d+),\\s*([\\p{L}\\s]+)\\s*$";

    /**
     * Validates the given address string.
     * Ensures it follows the correct format and matches existing city, district, and ward data.
     *
     * @param value   The address string to validate.
     * @param context The validation context.
     * @return true if the address is valid, false otherwise.
     */
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

    /**
     * Checks if the given city is valid based on data from ExcelService.
     *
     * @param city The city to validate.
     * @return true if the city exists in the dataset, false otherwise.
     */
    private boolean isValidCity(String city) {
        List<String> validCities = excelService.getAllCities();  // Fetch the valid cities from the ExcelService
        return validCities.contains(city);
    }

    /**
     * Checks if the given district is valid based on data from ExcelService.
     *
     * @param district The district to validate.
     * @return true if the district exists in the dataset, false otherwise.
     */
    private boolean isValidDistrict(String district) {
        List<String> validDistricts = excelService.getAllDistricts();  // Fetch the valid districts from the ExcelService
        return validDistricts.contains(district);
    }

    /**
     * Checks if the given ward is valid based on data from ExcelService.
     *
     * @param ward The ward to validate.
     * @return true if the ward exists in the dataset, false otherwise.
     */
    private boolean isValidWard(String ward) {
        List<String> validWards = excelService.getAllWards();  // Fetch the valid wards from the ExcelService
        return validWards.contains(ward);
    }

    /**
     * Validates if the given district belongs to the specified city.
     *
     * @param district The district to validate.
     * @param city     The city to check against.
     * @return true if the district belongs to the city, false otherwise.
     */
    private boolean isValidDistrictForCity(String district, String city) {
        List<String> validDistricts = excelService.getDistrictsByCity(city).stream().toList();  // Fetch the valid districts for the given city
        return validDistricts.contains(district);
    }

    /**
     * Validates if the given ward belongs to the specified district.
     *
     * @param ward     The ward to validate.
     * @param district The district to check against.
     * @return true if the ward belongs to the district, false otherwise.
     */
    private boolean isValidWardForDistrict(String ward, String district) {
        List<String> validWards = excelService.getWardsByDistrict(district).stream().toList();  // Fetch the valid wards for the given district
        return validWards.contains(ward);
    }
}
