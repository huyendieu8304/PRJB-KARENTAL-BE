package com.mp.karental.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Standard API response format")
public class ApiResponse<T>{
    /**
     * The code signifies the status of the request
     * By default, it would be 1000, means that the request is successfully resolved
     */
    @Builder.Default
    @Schema(description = "Response code", example = "1000")
    int code = 1000;

    /**
     * Short message inform client about the result of processing the request
     * By default, it would be "Success"
     */
    @Builder.Default
    @Schema(description = "Response message", example = "Successful")
    String message = "Success";

    /**
     * An object which is data return for the client.
     */
    @Schema(description = "Data object")
    T data;
}
