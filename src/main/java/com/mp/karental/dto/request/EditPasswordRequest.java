package com.mp.karental.dto.request;

import com.mp.karental.validation.RequiredField;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import lombok.experimental.FieldDefaults;

/**
 * Represents the request to edit password
 *
 * @author AnhHP9
 *
 * @version 1.0
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EditPasswordRequest {
    @RequiredField(fieldName = "Current password")
    String currentPassword;

    @RequiredField(fieldName = "New password")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).{9,}$", message = "INVALID_PASSWORD")
    String newPassword;

}
