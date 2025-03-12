package com.mp.karental.validation.validator;

import com.mp.karental.dto.request.BookingRequest;
import com.mp.karental.service.ExcelService;
import jakarta.validation.ConstraintValidatorContext;
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

@ExtendWith(MockitoExtension.class)
class AddressComponentValidatorTest {
    private AddressComponentValidator addressComponentValidator;
    private ConstraintValidatorContext context;

    @Mock
    private ExcelService excelService;

    @BeforeEach
    void setUp() {
        // Sử dụng List thay vì Set
        lenient().when(excelService.getAllCities()).thenReturn(List.of("Thành phố Hà Nội", "Thành phố Hồ Chí Minh"));
        lenient().when(excelService.getAllDistricts()).thenReturn(List.of("Quận Ba Đình", "Quận 1"));
        lenient().when(excelService.getAllWards()).thenReturn(List.of(
                "Phường Phúc Xá", "Phường Trúc Bạch", "Phường Vĩnh Phúc",
                "Phường Cống Vị", "Phường Liễu Giai", "Phường Bến Nghé", "Phường Cầu Kho"
        ));

        // Mapping districts to cities
        lenient().when(excelService.getDistrictsByCity("Thành phố Hà Nội"))
                .thenReturn( Set.of("Quận Ba Đình"));
        lenient().when(excelService.getDistrictsByCity("Thành phố Hồ Chí Minh"))
                .thenReturn(Set.of("Quận 1"));

        // Mapping wards to districts
        lenient().when(excelService.getWardsByDistrict("Quận Ba Đình"))
                .thenReturn( Set.of("Phường Phúc Xá", "Phường Trúc Bạch", "Phường Vĩnh Phúc",
                        "Phường Cống Vị", "Phường Liễu Giai"));
        lenient().when(excelService.getWardsByDistrict("Quận 1"))
                .thenReturn( Set.of("Phường Bến Nghé", "Phường Cầu Kho"));

        addressComponentValidator = new AddressComponentValidator(excelService);
        context = mock(ConstraintValidatorContext.class);
    }

    @Test
    void testValidAddressComponent() {
        BookingRequest request = new BookingRequest();
        request.setDriverCityProvince("Thành phố Hà Nội");
        request.setDriverDistrict("Quận Ba Đình");
        request.setDriverWard("Phường Phúc Xá");

        assertTrue(addressComponentValidator.isValid(request, context));
    }


    @Test
    void testInvalidAddressComponent_MissingCity() {
        BookingRequest request = new BookingRequest();
        request.setDriverCityProvince("");  // City bị thiếu
        request.setDriverDistrict("Quận Ba Đình");
        request.setDriverWard("Phường Phúc Xá");

        assertFalse(addressComponentValidator.isValid(request, context));
    }


    @Test
    void testInvalidAddressComponent_InvalidDistrict() {
        BookingRequest request = new BookingRequest();
        request.setDriverCityProvince("Thành phố Hà Nội");
        request.setDriverDistrict("Invalid District");  // Quận không hợp lệ
        request.setDriverWard("Phường Phúc Xá");

        assertFalse(addressComponentValidator.isValid(request, context));
    }


    @Test
    void testInvalidAddressComponent_InvalidWard() {
        BookingRequest request = new BookingRequest();
        request.setDriverCityProvince("Thành phố Hà Nội");
        request.setDriverDistrict("Quận Ba Đình");
        request.setDriverWard("Invalid Ward");  // Phường không hợp lệ

        assertFalse(addressComponentValidator.isValid(request, context));
    }

    @Test
    void testInvalidAddressComponent_NullRequest() {
        assertTrue(addressComponentValidator.isValid(null, context)); // Nếu null thì không kiểm tra
    }

    @Test
    void testInvalidAddressComponent_EmptyFields() {
        BookingRequest request = new BookingRequest();
        request.setDriverCityProvince("");  // Tất cả rỗng
        request.setDriverDistrict("");
        request.setDriverWard("");

        assertFalse(addressComponentValidator.isValid(request, context));
    }
    @Test
    void testInvalidAddressComponent_DistrictNotInCity() {
        BookingRequest request = new BookingRequest();
        request.setDriverCityProvince("Thành phố Hà Nội");
        request.setDriverDistrict("Quận 1"); // Không thuộc Hà Nội
        request.setDriverWard("Phường Phúc Xá");

        assertFalse(addressComponentValidator.isValid(request, context));
    }
}
