package com.mp.karental.exception;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

/**
 * Enumeration representing custom error codes for the application.
 * <p>
 * This centralized error handling mechanism allows for consistent error reporting across the application.
 * </p>
 *
 * @author DieuTTH4
 *
 * @version 1.0
 */
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum ErrorCode {
    //range 2xxx
    REQUIRED_FIELD(2000, "This field is required.", HttpStatus.BAD_REQUEST),
    INVALID_NAME(2001, "The full name can only contain alphabet characters.", HttpStatus.BAD_REQUEST),

    INVALID_EMAIL(2002, "Invalid email.", HttpStatus.BAD_REQUEST),
    NOT_UNIQUE_EMAIL(2003, "Email already existed. Please try another email.", HttpStatus.BAD_REQUEST),

    INVALID_PHONE_NUMBER(2004, "Invalid phone number.", HttpStatus.BAD_REQUEST),
    NOT_UNIQUE_PHONE_NUMBER(2005, "The phone number already existed. Please try another phone number", HttpStatus.BAD_REQUEST),

    INVALID_PASSWORD(2006, "Password must contain at least one number, one numeral, and seven characters.", HttpStatus.BAD_REQUEST),

    //range 3xxx
    UPLOAD_OBJECT_TO_S3_FAIL(3001, "There was error occured during uploading files. Please try again.", HttpStatus.INTERNAL_SERVER_ERROR),

    //range 4xxx
    UNCATEGORIZED_EXCEPTION(4000, "There was error happen during run time", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_ERROR_KEY(4001, "The error key could be misspelled", HttpStatus.BAD_REQUEST),
    ;

    /**
     * A unique numeric identifier for the error.
     */
    int code;
    /**
     * A human-readable message describing the error.
     */
    String message;
    /**
     * The corresponding HTTP status to be returned when the error occurs.
     */
    HttpStatusCode httpStatusCode;

}
