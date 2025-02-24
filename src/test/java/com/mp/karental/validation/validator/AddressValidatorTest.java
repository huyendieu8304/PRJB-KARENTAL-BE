package com.mp.karental.validation.validator;

import com.mp.karental.service.ExcelService;
import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AddressValidatorTest {
    private AddressValidator addressValidator;
    private ConstraintValidatorContext context;
    private ExcelService excelService;

    @BeforeEach
    void setUp() {
        excelService = mock(ExcelService.class);

        // Mock danh sách hợp lệ
        when(excelService.getAllCities()).thenReturn(List.of("Thành phố Hà Nội"));
        when(excelService.getAllDistricts()).thenReturn(List.of("Quận Ba Đình"));
        when(excelService.getAllWards()).thenReturn(List.of(
                "Phường Phúc Xá", "Phường Trúc Bạch", "Phường Vĩnh Phúc",
                "Phường Cống Vị", "Phường Liễu Giai"
        ));

        // Mapping Quận -> Thành phố
        when(excelService.getDistrictsByCity("Thành phố Hà Nội"))
                .thenReturn(Set.of("Quận Ba Đình"));

        // Mapping Phường -> Quận
        when(excelService.getWardsByDistrict("Quận Ba Đình"))
                .thenReturn(Set.of("Phường Phúc Xá", "Phường Trúc Bạch", "Phường Vĩnh Phúc",
                        "Phường Cống Vị", "Phường Liễu Giai"));

        addressValidator = new AddressValidator(excelService);
        context = mock(ConstraintValidatorContext.class);
    }

    @Test
    void testValidAddress() {
        assertTrue(addressValidator.isValid("Thành phố Hà Nội, Quận Ba Đình, Phường Phúc Xá, 001, Việt Nam", context));
    }

    @Test
    void testInvalidAddress_ExtraSpace() {
        assertTrue(addressValidator.isValid("Thành phố Hà Nội , Quận Ba Đình, Phường Phúc Xá, 001, Việt Nam", context));
    }


}
