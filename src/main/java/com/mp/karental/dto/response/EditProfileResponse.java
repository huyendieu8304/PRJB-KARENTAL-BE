package com.mp.karental.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

/**
 * DTO representing the response for editing a user profile.
 * Contains user profile details that can be updated.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EditProfileResponse {

    String fullName;
    LocalDate dob;
    String phoneNumber;
    String nationalId;
    String drivingLicenseUrl;
    String cityProvince;
    String district;
    String ward;
    String houseNumberStreet;
    String email;
}
