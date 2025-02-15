package com.mp.karental.dto.request;

import com.mp.karental.validation.UniqueEmail;
import com.mp.karental.validation.UniquePhoneNumber;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import lombok.experimental.FieldDefaults;

/**
 * Represents the request payload for registering a new user account.
 * <p>
 * This class encapsulates the necessary data required to create a new account,
 * including personal information and authentication credentials.
 * </p>
 *
 * @author DieuTTH4
 *
 * @version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class AccountRegisterRequest {

    @NotBlank(message = "REQUIRED_FIELD")
    //The name can only contain alphabet characters (accept unicode), spaces and hyphens -
    @Pattern(regexp = "^[\\p{L}\\s-]+$", message = "INVALID_NAME")
    String fullName;

    @NotBlank(message = "REQUIRED_FIELD")
    @Email (message = "INVALID_EMAIL")
    @UniqueEmail(message = "NOT_UNIQUE_EMAIL")
    String email;

    @NotBlank(message = "REQUIRED_FIELD")
    //The phone number must start with 0 and having 10 digits in total
    @Pattern(regexp = "^0[\\d]{9}$", message = "INVALID_PHONE_NUMBER")
    @UniquePhoneNumber(message = "NOT_UNIQUE_PHONE_NUMBER")
    String phoneNumber;

    @NotBlank(message = "REQUIRED_FIELD")
    //The password must have at least 1 character, 1 digit and 7 characters
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).{9,}$", message = "INVALID_PASSWORD")
    String password;

    @NotNull(message = "REQUIRED_FIELD")
    String isCustomer;
}
