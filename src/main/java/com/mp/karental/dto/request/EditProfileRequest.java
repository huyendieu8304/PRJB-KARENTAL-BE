package com.mp.karental.dto.request;

import com.mp.karental.validation.*;
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

    @NotBlank(message = "REQUIRED_FIELD")
    @Pattern(regexp = "^[\\p{L}\\s-]+$", message = "INVALID_NAME")
    String fullName;

    @Past(message = "DATE_OF_BIRTH_MUST_BE_IN_THE_PAST")
    @NotNull(message = "REQUIRED_FIELD")
    LocalDate dob;

    @NotBlank(message = "REQUIRED_FIELD")
    @Pattern(regexp = "^0[\\d]{9}$", message = "INVALID_PHONE_NUMBER")
    @UniquePhoneNumber(message = "NOT_UNIQUE_PHONE_NUMBER")
    String phoneNumber;

    @NotBlank(message = "REQUIRED_FIELD")
    String nationalId;

    @NotBlank(message = "REQUIRED_FIELD")
    String cityProvince;

    @NotBlank(message = "REQUIRED_FIELD")
    String district;

    @NotBlank(message = "REQUIRED_FIELD")
    String ward;

    @NotBlank(message = "REQUIRED_FIELD")
    String houseNumberStreet;

    @ValidDocument(message = "INVALID_IMAGE_FILE")
    @NotNull(message = "REQUIRED_FIELD")
    MultipartFile drivingLicense;

}
