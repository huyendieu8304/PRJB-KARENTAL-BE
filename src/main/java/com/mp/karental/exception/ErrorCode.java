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

    INVALID_LICENSE(2007, "Invalid license plate format! Expected format: (11-99)(A-Z)-(000-999).(00-99).", HttpStatus.BAD_REQUEST),
    NOT_UNIQUE_LICENSE(2008, "License plate already existed. Please try another license plate", HttpStatus.BAD_REQUEST),
    INVALID_COLOR(2009, "Your color is not alive. Please try another color", HttpStatus.BAD_REQUEST),
    INVALID_ADDITIONAL_FUNTION(2010, "Your additional functions were not predefined. Please try another color", HttpStatus.BAD_REQUEST),
    //range 3xxx
    UPLOAD_OBJECT_TO_S3_FAIL(3001, "There was error occured during uploading files. Please try again.", HttpStatus.INTERNAL_SERVER_ERROR),
    ROLE_NOT_FOUND_IN_DB(3002, "The entity role requested is not found in the db", HttpStatus.NOT_FOUND),

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
