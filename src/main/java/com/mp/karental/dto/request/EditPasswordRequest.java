package com.mp.karental.dto.request;

import com.mp.karental.validation.RequiredField;
import jakarta.validation.constraints.Pattern;

public class EditPasswordRequest {
    @RequiredField(fieldName = "Current password")
    String currentPassword;

    @RequiredField(fieldName = "New password")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).{9,}$", message = "INVALID_PASSWORD")
    String newPassword;

    @RequiredField(fieldName = "Confirm password")
    String confirmPassword;

}
