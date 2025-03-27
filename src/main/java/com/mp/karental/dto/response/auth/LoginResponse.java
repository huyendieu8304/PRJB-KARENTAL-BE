package com.mp.karental.dto.response.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.FieldDefaults;

/**
 * Represents the response payload for authentication requests.
 * <p>
 * This class encapsulates access and refresh token, along with the role of the user that make authentication request.
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
@Schema(name = "response.auth.LoginResponse", description = "Data return after login success fully")
public class LoginResponse {
    @Schema(example = "CUSTOMER")
    String userRole;
    @Schema(example = "Nguyễn Thị Bích")
    String fullName;

}
