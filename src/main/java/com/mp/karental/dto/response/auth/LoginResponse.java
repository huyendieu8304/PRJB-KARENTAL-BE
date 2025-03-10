package com.mp.karental.dto.response.auth;

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
public class LoginResponse {
    String userRole;
    String fullName;

}
