package com.mp.karental.validation.validator;

import com.mp.karental.service.ExcelService;
import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
/**
 * test address validator
 *
 * QuangPM20
 * version 1.0
 */

@ExtendWith(MockitoExtension.class)
class AddressValidatorTest {
    private AddressValidator addressValidator;
    private ConstraintValidatorContext context;
    @Mock
    private ExcelService excelService;

    @BeforeEach
    void setUp() {
        // Mock valid lists for multiple cities and districts
        lenient().when(excelService.getAllCities()).thenReturn(List.of("Thành phố Hà Nội", "Thành phố Hồ Chí Minh"));
        lenient().when(excelService.getAllDistricts()).thenReturn(List.of("Quận Ba Đình", "Quận 1"));
        lenient().when(excelService.getAllWards()).thenReturn(List.of(
                "Phường Phúc Xá", "Phường Trúc Bạch", "Phường Vĩnh Phúc",
                "Phường Cống Vị", "Phường Liễu Giai", "Phường Bến Nghé", "Phường Cầu Kho"
        ));

        // Mapping districts to cities
        lenient().when(excelService.getDistrictsByCity("Thành phố Hà Nội"))
                .thenReturn(Set.of("Quận Ba Đình"));
        lenient().when(excelService.getDistrictsByCity("Thành phố Hồ Chí Minh"))
                .thenReturn(Set.of("Quận 1"));

        // Mapping wards to districts
        lenient().when(excelService.getWardsByDistrict("Quận Ba Đình"))
                .thenReturn(Set.of("Phường Phúc Xá", "Phường Trúc Bạch", "Phường Vĩnh Phúc",
                        "Phường Cống Vị", "Phường Liễu Giai"));
        lenient().when(excelService.getWardsByDistrict("Quận 1"))
                .thenReturn(Set.of("Phường Bến Nghé", "Phường Cầu Kho"));

        addressValidator = new AddressValidator(excelService);
        context = mock(ConstraintValidatorContext.class);
    }

    @Test
    void testBlank() {
        assertFalse(addressValidator.isValid("", context));
    }
    @Test
    void testNull() {
        assertFalse(addressValidator.isValid(null, context));
    }
    @Test
    void InvalidFormat() {
        assertFalse(addressValidator.isValid("Thành phố Hà Nội-Quận Ba Đình-Phường Phúc Xá-001-Việt Nam", context));
    }

    @Test
    void testValidAddress_CityDistrictWard_HaNoi() {
        assertTrue(addressValidator.isValid("Thành phố Hà Nội, Quận Ba Đình, Phường Phúc Xá, 001, Việt Nam", context));
    }

    @Test
    void testInvalidAddress_ExtraSpace() {
        assertTrue(addressValidator.isValid("Thành phố Hà Nội , Quận Ba Đình, Phường Phúc Xá, 001, Việt Nam", context));
    }

    @Test
    void testInvalidAddress_MissingWard() {
        assertFalse(addressValidator.isValid("Thành phố Hà Nội, Quận Ba Đình", context));
    }

    @Test
    void testInvalidAddress_MissingDistrict() {
        assertFalse(addressValidator.isValid("Thành phố Hà Nội, , Phường Phúc Xá, 001, Việt Nam", context));
    }

    @Test
    void testInvalidAddress_MissingCity() {
        assertFalse(addressValidator.isValid(",Quận Ba Đình, Phường Phúc Xá, 001, Việt Nam", context));
    }

    @Test
    void testInvalidAddress_InvalidCity() {
        assertFalse(addressValidator.isValid("abc,Quận Ba Đình, Phường Phúc Xá, 001, Việt Nam", context));
    }

    @Test
    void testInvalidAddress_InvalidDistrict() {
        assertFalse(addressValidator.isValid("Thành phố Hà Nội,abc, Phường Phúc Xá, 001, Việt Nam", context));
    }

    @Test
    void testInvalidAddress_InvalidWard() {
        assertFalse(addressValidator.isValid("Thành phố Hà Nội,Quận Ba Đình, abc, 001, Việt Nam", context));
    }

    @Test
    void testInvalidAddress_MissingHouseNumber() {
        assertFalse(addressValidator.isValid("Thành phố Hà Nội , Quận Ba Đình, Phường Phúc Xá", context));
    }

    @Test
    void testInValidAddress_CityDistrictWard_HoChiMinh() {
        assertFalse(addressValidator.isValid("Thành phố Hồ Chí Minh, Quận 1, Phường Bến Nghé, 002, Việt Nam", context));
    }

    @Test
    void testInvalidAddress_MissingCity_HoChiMinh() {
        assertFalse(addressValidator.isValid(", Quận 1, Phường Bến Nghé, 002, Việt Nam", context));
    }

    @Test
    void testInvalidAddress_InvalidDistrict_HoChiMinh() {
        assertFalse(addressValidator.isValid("Thành phố Hồ Chí Minh, Invalid District, Phường Bến Nghé, 002, Việt Nam", context));
    }

    @Test
    void testInvalidAddress_InvalidWard_HoChiMinh() {
        assertFalse(addressValidator.isValid("Thành phố Hồ Chí Minh, Quận 1, Invalid Ward, 002, Việt Nam", context));
    }

    @Test
    void testValidDistrictForCity_HaNoi() {
        assertTrue(addressValidator.isValid("Thành phố Hà Nội, Quận Ba Đình, Phường Trúc Bạch, 001, Việt Nam", context));
    }

    @Test
    void testInvalidDistrictForCity_HoChiMinh() {
        assertFalse(addressValidator.isValid("Thành phố Hồ Chí Minh, Quận 1, Phường Cầu Kho, 002, Việt Nam", context));
    }

    @Test
    void testInvalidDistrictForCity_InvalidCity() {
        assertFalse(addressValidator.isValid("Invalid City, Quận 1, Phường Bến Nghé, 002, Việt Nam", context));
    }

    @Test
    void testValidWardForDistrict_HaNoi() {
        assertTrue(addressValidator.isValid("Thành phố Hà Nội, Quận Ba Đình, Phường Phúc Xá, 001, Việt Nam", context));
    }

    @Test
    void testInvalidWardForDistrict_HoChiMinh() {
        assertFalse(addressValidator.isValid("Thành phố Hồ Chí Minh, Quận 1, Phường Cầu Kho, 002, Việt Nam", context));
    }

    @Test
    void testInvalidWardForDistrict_InvalidDistrict() {
        assertFalse(addressValidator.isValid("Thành phố Hà Nội, Quận Invalid District, Phường Phúc Xá, 001, Việt Nam", context));
    }

}
