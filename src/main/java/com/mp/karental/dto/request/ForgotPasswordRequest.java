package com.mp.karental.dto.request;

import com.mp.karental.validation.RequiredField;
import com.mp.karental.validation.UniqueEmail;
import jakarta.validation.constraints.Email;
import lombok.*;
import lombok.experimental.FieldDefaults;

/**
 * Represents the request where user enter email to change password when he/she forgot it.
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
public class ForgotPasswordRequest {
    @RequiredField(fieldName = "Email")
    @Email(message = "INVALID_EMAIL")
    String email;
}
