package com.mp.karental.dto.request;

import com.mp.karental.validation.RequiredField;
import com.mp.karental.validation.UniqueEmail;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

/**
 * Represents the request checking whether the email is not exist in db.
 *
 * @author DieuTHH4
 *
 * @version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class CheckUniqueEmailRequest {
    @RequiredField(fieldName = "Email")
    @Email(message = "INVALID_EMAIL")
    @UniqueEmail(message = "NOT_UNIQUE_EMAIL")
    String email;
}
