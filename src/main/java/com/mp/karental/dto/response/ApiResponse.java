package com.mp.karental.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

/**
 * This class defines the form of all API response
 * @param <T> The type of object contain inside the response
 *
 * @author DieuTTH4
 *
 * @version 1.0
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class ApiResponse<T>{
    /**
     * The code signifies the status of the request
     * By default, it would be 1000, means that the request is successfully resolved
     */
    @Builder.Default
    int code = 1000;

    /**
     * Short message inform client about the result of processing the request
     * By default, it would be "Success"
     */
    @Builder.Default
    String message = "Success";

    /**
     * An object which is data return for the client.
     */
    T data;
}
