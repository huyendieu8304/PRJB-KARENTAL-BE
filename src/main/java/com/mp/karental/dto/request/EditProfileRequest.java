package com.mp.karental.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EditProfileRequest {

    @NotBlank(message = "Full name is required")
    String fullName;

    @Past(message = "Date of birth must be in the past")
    LocalDate dob;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Invalid phone number format")
    String phoneNumber;

    @NotBlank(message = "National ID is required")
    @Size(min = 9, max = 12, message = "National ID must be between 9 and 12 digits")
    String nationalId;

    MultipartFile drivingLicense;

    @NotBlank(message = "Address is required")
    String address;

    @NotBlank(message = "City/Province is required")
    String cityProvince;

    @NotBlank(message = "District is required")
    String district;

    @NotBlank(message = "Ward is required")
    String ward;

    @NotBlank(message = "House number and street are required")
    String houseNumberStreet;
}
