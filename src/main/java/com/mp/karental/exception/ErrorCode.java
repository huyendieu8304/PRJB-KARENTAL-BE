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
    //range 1xxx
    SUCCESS(1000, "Success", HttpStatus.OK),

    //range 2xxx
    REQUIRED_FIELD(2000, "{fieldName} is required.", HttpStatus.BAD_REQUEST),
    INVALID_NAME(2001, "The full name can only contain alphabet characters.", HttpStatus.BAD_REQUEST),

    INVALID_EMAIL(2002, "Please enter a valid email address", HttpStatus.BAD_REQUEST),
    NOT_UNIQUE_EMAIL(2003, "Email already existed. Please try another email.", HttpStatus.BAD_REQUEST),

    INVALID_PHONE_NUMBER(2004, "Invalid phone number.", HttpStatus.BAD_REQUEST),
    NOT_UNIQUE_PHONE_NUMBER(2005, "The phone number already existed. Please try another phone number", HttpStatus.BAD_REQUEST),

    INVALID_PASSWORD(2006, "Password must contain at least one number, one numeral, and seven characters.", HttpStatus.BAD_REQUEST),

    INVALID_LICENSE(2007, "Invalid license plate format! Expected format: (11-99)(A-Z)-(000-999).(00-99).", HttpStatus.BAD_REQUEST),
    NOT_UNIQUE_LICENSE(2008, "License plate already existed. Please try another license plate", HttpStatus.BAD_REQUEST),
    INVALID_COLOR(2009, "Your color were not predefined. Please try another color", HttpStatus.BAD_REQUEST),
    INVALID_ADDITIONAL_FUNTION(2010, "Your additional functions were not predefined. Please try another function", HttpStatus.BAD_REQUEST),
    INVALID_PRODUCTION_YEAR(2011, "Your production year was not predefined. Please try another year", HttpStatus.BAD_REQUEST),
    INVALID_NUMBER_OF_SEAT(2012, "Invalid number of seats. Allowed values are 4, 5, or 7.", HttpStatus.BAD_REQUEST),
    INVALID_BRAND(2013, "Your brand were not predefined. Please try another brand", HttpStatus.BAD_REQUEST),
    INVALID_MODEL(2014, "Your model were not predefined. Please try another model", HttpStatus.BAD_REQUEST),
    INVALID_BRAND_MODEL(2015, "Your brand-model were not matched. Please try again", HttpStatus.BAD_REQUEST),
    INVALID_DOCUMENT_FILE(2016,"Invalid file type. Accepted formats are .doc, .docx, .pdf, .jpeg, .jpg, .png", HttpStatus.BAD_REQUEST),
    INVALID_CAR_IMAGE_FILE(2017,"Invalid file type. Accepted formats are .jpg, .jpeg, .png, .gif", HttpStatus.BAD_REQUEST),
    INVALID_VALUE_MIN(2018,"This attribute must be >=0", HttpStatus.BAD_REQUEST),
    INVALID_ADDRESS(2019,"The address is invalid", HttpStatus.BAD_REQUEST),
    INVALID_DATE_OF_BIRTH(2020, "Date of birth must be in the past.", HttpStatus.BAD_REQUEST),
    INVALID_IMAGE_FILE(2021, "Invalid image file type. Accepted formats are .jpg, .jpeg, .png", HttpStatus.BAD_REQUEST),
    INVALID_NATIONAL_ID(2022, "National ID must contain exactly 12 digits.", HttpStatus.BAD_REQUEST),
    NOT_UNIQUE_NATIONAL_ID(2023, "The national id already existed. Please try another national id", HttpStatus.BAD_REQUEST),
    INVALID_STATUS_EDIT(2024,"Status can edit only available/stopped", HttpStatus.BAD_REQUEST),
    INVALID_DATE_RANGE(2025, "Invalid date range. Pick-up date must be before drop-off date.", HttpStatus.BAD_REQUEST),

    //range 3xxx
    UPLOAD_OBJECT_TO_S3_FAIL(3001, "There was error occured during uploading files. Please try again.", HttpStatus.INTERNAL_SERVER_ERROR),
    ROLE_NOT_FOUND_IN_DB(3002, "The entity role requested is not found in the db", HttpStatus.NOT_FOUND),
    ACCOUNT_NOT_FOUND_IN_DB(3003, "The account is not exist in the system", HttpStatus.NOT_FOUND),
    MAXIMUM_FILE_UPLOAD_EXCEED(3004, "Maximum file upload exceeded. Each file should not exceed 5Mb", HttpStatus.BAD_REQUEST),
    PASSWORDS_DO_NOT_MATCH(3005, "New password and Confirm password don’t match.", HttpStatus.BAD_REQUEST),
    INCORRECT_PASSWORD(3006, "Current password is incorrect.", HttpStatus.BAD_REQUEST),
    CAR_NOT_FOUND_IN_DB(3007, "The car is not exist in the system", HttpStatus.NOT_FOUND),
    CAR_NOT_VERIFIED(3008, "This car has not been verified and cannot be viewed.", HttpStatus.FORBIDDEN),

    //range 4xxx
    UNCATEGORIZED_EXCEPTION(4000, "There was error happen during run time", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_ERROR_KEY(4001, "The error key could be misspelled", HttpStatus.BAD_REQUEST),
    INVALID_LOGIN_INFORMATION(4002, "Either email address or password is incorrect. Please try again", HttpStatus.UNAUTHORIZED),
    //TODO: xem lại cái message và cái http status ở chỗ này cũng như cái tên của nó luôn
    UNAUTHENTICATED(4003, "Unauthenticated access. The access token is invalid", HttpStatus.UNAUTHORIZED), //401
    UNAUTHORIZED(4004, "User doesn't have permission to access the endpoint.", HttpStatus.FORBIDDEN), //403
    ACCESS_TOKEN_EXPIRED(4005, "The access token is expired. Please try again", HttpStatus.UNAUTHORIZED),
    ACCOUNT_IS_INACTIVE(4006, "Your account is inactive.", HttpStatus.FORBIDDEN),

    REFRESH_TOKEN_EXPIRED(4008, "The refresh token is expired. Please login again.", HttpStatus.UNAUTHORIZED),
    INVALID_REFRESH_TOKEN(4009, "Invalid refresh token. Please try again", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED_ACCESS(4010, "Can not view detail/edit car of another account", HttpStatus.UNAUTHORIZED),
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
