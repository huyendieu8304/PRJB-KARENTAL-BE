package com.mp.karental.dto.request.user;

import com.mp.karental.validation.RequiredField;
import com.mp.karental.validation.UniqueEmail;
import com.mp.karental.validation.UniquePhoneNumber;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
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
 * @version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
@Schema(description = "DTO send along create account request")
public class AccountRegisterRequest {

    @RequiredField(fieldName = "Full name")
    //The name can only contain alphabet characters (accept unicode), spaces and hyphens -
    @Pattern(regexp = "^[\\p{L}\\s-]+$", message = "INVALID_NAME")
    @Schema(example = "Nguyễn Thị Bích")
    String fullName;

    @RequiredField(fieldName = "Email")
    @Email(message = "INVALID_EMAIL")
    @UniqueEmail(message = "NOT_UNIQUE_EMAIL")
    @Schema(example = "bich@example.com")
    String email;

    @RequiredField(fieldName = "Phone number")
    //The phone number must start with 0 and having 10 digits in total
    @Pattern(regexp = "^0[\\d]{9}$", message = "INVALID_PHONE_NUMBER")
    @UniquePhoneNumber(message = "NOT_UNIQUE_PHONE_NUMBER")
    @Schema(example = "0123456789")
    String phoneNumber;

    @RequiredField(fieldName = "Password")
    //The password must have at least 1 character, 1 digit and 7 characters
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).{9,}$", message = "INVALID_PASSWORD")
    @Schema(example = "a12345678")
    String password;

    @RequiredField(fieldName = "The role of the account")
    @Schema(example = "true")
    String isCustomer;
}
