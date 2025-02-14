package com.mp.karental.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class ApiResponse<T>{
    @Builder.Default
    private int code = 1000;
    @Builder.Default
    private String message = "success";
    private T data;
}
