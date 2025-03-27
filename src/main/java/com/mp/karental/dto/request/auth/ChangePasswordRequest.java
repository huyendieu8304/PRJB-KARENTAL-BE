package com.mp.karental.dto.request.auth;

import com.mp.karental.validation.RequiredField;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import lombok.experimental.FieldDefaults;

/**
 * Represents the request payload for change password.
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
@Schema(name = "request.auth.ChangePasswordRequest", description = "DTO contain necessary information to change password")
public class ChangePasswordRequest {

    @RequiredField(fieldName = "Forgot password token")
    @Schema(format = "UUID", example = "1c169cf0-81f3-4e4b-ba0b-9d1659b9b0ca")
    String forgotPasswordToken;

    @RequiredField(fieldName = "Password")
    //The password must have at least 1 character, 1 digit and 7 characters
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).{9,}$", message = "INVALID_PASSWORD")
    @Schema(example = "a12345678")
    String newPassword;
}
