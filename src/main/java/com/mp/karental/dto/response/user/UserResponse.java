package com.mp.karental.dto.response.user;

import com.mp.karental.entity.Role;
import lombok.*;
import lombok.experimental.FieldDefaults;

/**
 * Represents the response payload for user details.
 * <p>
 * This class encapsulates user information that is returned to the client,
 * including personal details and assigned role.
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
public class UserResponse {
    String fullName;
    String email;
    String phoneNumber;
    String role;

}
