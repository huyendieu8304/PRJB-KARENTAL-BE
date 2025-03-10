package com.mp.karental.dto.response;

import com.mp.karental.entity.Role;
import io.swagger.v3.oas.annotations.media.Schema;
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
    @Schema(example = "Trần Văn Long")
    String fullName;
    @Schema(example = "long@gmail.com")
    String email;
    @Schema(example = "0918823499")
    String phoneNumber;
    @Schema(example = "CUSTOMER")
    String role;

}
