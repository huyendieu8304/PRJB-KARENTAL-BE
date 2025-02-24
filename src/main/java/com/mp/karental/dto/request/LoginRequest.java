package com.mp.karental.dto.request;

import com.mp.karental.validation.UniqueEmail;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
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
public class LoginRequest {
    @NotBlank(message = "REQUIRED_FIELD")
    @Email(message = "INVALID_EMAIL")
    @UniqueEmail(message = "NOT_UNIQUE_EMAIL")
    String email;

    @NotBlank(message = "REQUIRED_FIELD")
    //The password must have at least 1 character, 1 digit and 7 characters
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).{9,}$", message = "INVALID_PASSWORD")
    String password;
}
