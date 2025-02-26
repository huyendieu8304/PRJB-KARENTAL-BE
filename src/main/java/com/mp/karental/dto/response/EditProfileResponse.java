package com.mp.karental.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

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
    String address;
    String cityProvince;
    String district;
    String ward;
    String houseNumberStreet;
}
