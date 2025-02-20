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

    INVALID_EMAIL(2002, "Please enter a valid email address", HttpStatus.BAD_REQUEST),
    NOT_UNIQUE_EMAIL(2003, "Email already existed. Please try another email.", HttpStatus.BAD_REQUEST),

    INVALID_PHONE_NUMBER(2004, "Invalid phone number.", HttpStatus.BAD_REQUEST),
    NOT_UNIQUE_PHONE_NUMBER(2005, "The phone number already existed. Please try another phone number", HttpStatus.BAD_REQUEST),

    INVALID_PASSWORD(2006, "Password must contain at least one number, one numeral, and seven characters.", HttpStatus.BAD_REQUEST),

    //range 3xxx
    UPLOAD_OBJECT_TO_S3_FAIL(3001, "There was error occured during uploading files. Please try again.", HttpStatus.INTERNAL_SERVER_ERROR),
    ROLE_NOT_FOUND_IN_DB(3002, "The entity role requested is not found in the db", HttpStatus.NOT_FOUND),
//    ACCOUNT_NOT_FOUND_IN_DB(3003, "The email has not been registered.", HttpStatus.NOT_FOUND),

    //range 4xxx
    UNCATEGORIZED_EXCEPTION(4000, "There was error happen during run time", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_ERROR_KEY(4001, "The error key could be misspelled", HttpStatus.BAD_REQUEST),
    INVALID_LOGIN_INFORMATION(4002, "Either email address or password is incorrect. Please try again", HttpStatus.UNAUTHORIZED),
    //TODO: xem lại cái message và cái http status ở chỗ này cũng như cái tên của nó luôn
    UNAUTHENTICATED(4003, "Unauthenticated access. The access token is invalid", HttpStatus.UNAUTHORIZED), //401
    UNAUTHORIZED(4004, "User doesn't have permission to access the endpoint.", HttpStatus.FORBIDDEN), //403
    ACCESS_TOKEN_EXPIRED(4005, "The access token is expired. Please try again", HttpStatus.UNAUTHORIZED),
//    WRONG_PASSWORD(4003, "The password is incorrect.", HttpStatus.UNAUTHORIZED),
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
