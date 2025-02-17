package com.mp.karental.exception;

import lombok.Getter;
import lombok.Setter;

/**
 * Custom exception class for handling application-specific errors.
 * <p>
 * This exception extends {@code RuntimeException} and is used to encapsulate
 * error codes and messages defined in the {@code ErrorCode} enum.
 * It provides a unified way to manage application errors and their associated messages.
 * </p>
 * @author DieuTTH4
 *
 * @version 1.0
 */
@Getter
@Setter
public class AppException extends RuntimeException {

    private ErrorCode errorCode;

    /**
     * Initializes the exception with a given {@code ErrorCode} and sets the exception message
     * using the error code's message.
     * @param errorCode The specific error code associated with the exception.
     */
    public AppException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
