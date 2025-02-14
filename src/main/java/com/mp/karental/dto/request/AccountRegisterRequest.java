package com.mp.karental.dto.request;

import com.mp.karental.validation.UniqueEmail;
import com.mp.karental.validation.UniquePhoneNumber;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class AccountRegisterRequest {

    @NotBlank(message = "INVALID_NAME")
    String fullName;

    @Email (message = "INVALID_EMAIL")
    @UniqueEmail(message = "NOT_UNIQUE_EMAIL")
    String email;

    @Pattern(regexp = "^0[\\d]{10}$", message = "INVALID_PHONE_NUMBER")
    @UniquePhoneNumber(message = "NOT_UNIQUE_PHONE_NUMBER")
    String phoneNumber;

    @NotBlank
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).{9,}$", message = "INVALID_PASSWORD")
    String password;

    @NotNull(message = "Must choose one role, customer or car owner, to register account.")
    boolean isCustomer;
}
