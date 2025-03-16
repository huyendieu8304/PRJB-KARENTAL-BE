package com.mp.karental.validation.validator;

import com.mp.karental.dto.request.booking.CreateBookingRequest;
import com.mp.karental.service.ExcelService;
import com.mp.karental.validation.ValidAddressComponent;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;

/**
 * Validator for individual address components (City, District, Ward).
 */
@RequiredArgsConstructor
public class AddressComponentValidator implements ConstraintValidator<ValidAddressComponent, CreateBookingRequest> {

    private final ExcelService excelService;

    @Override
    public boolean isValid(CreateBookingRequest CreateBookingRequest, ConstraintValidatorContext context) {
        if (CreateBookingRequest == null) {
            return true; // Ignore validation if object is null
        }
        if(!CreateBookingRequest.isDriver()){
            return true;
        }

        String city = CreateBookingRequest.getDriverCityProvince();
        String district = CreateBookingRequest.getDriverDistrict();
        String ward = CreateBookingRequest.getDriverWard();

        boolean isValid = true;

        // Validate city
        if (!StringUtils.hasText(city) || !excelService.getAllCities().contains(city)) {
            isValid = false;
        }

        // Validate district belongs to city
        if (!StringUtils.hasText(district) || !excelService.getDistrictsByCity(city).contains(district)) {
            isValid = false;
        }

        // Validate ward belongs to district
        if (!StringUtils.hasText(ward) || !excelService.getWardsByDistrict(district).contains(ward)) {
            isValid = false;
        }

        return isValid;
    }
}
