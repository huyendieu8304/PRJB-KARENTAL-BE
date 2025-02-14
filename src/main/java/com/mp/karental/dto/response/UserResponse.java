package com.mp.karental.dto.response;

import com.mp.karental.entity.Role;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class UserResponse {
    String fullName;
    String email;
    String phoneNumber;
    Role role;

}
