package com.mp.karental.dto.request.auth;

import com.mp.karental.validation.RequiredField;
import com.mp.karental.validation.UniqueEmail;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import lombok.experimental.FieldDefaults;

/**
 * Represents the request payload for login to account.
 * <p>
 * This class encapsulates the necessary data required to login into an account,
 * including email and password
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
@Schema(name = "request.auth.LoginRequest", description = "DTO contain necessary information to login into the system")
public class LoginRequest {
    @RequiredField(fieldName = "Email")
    @Email(message = "INVALID_EMAIL")
    @Schema( format = "email", example = "bich@example.com")
    String email;

    @RequiredField(fieldName = "Password")
    //The password must have at least 1 character, 1 digit and 7 characters
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).{9,}$", message = "INVALID_PASSWORD")
    @Schema(example = "a12345678")
    String password;
}
