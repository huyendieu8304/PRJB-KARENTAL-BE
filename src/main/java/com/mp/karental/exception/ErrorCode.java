package com.mp.karental.exception;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum ErrorCode {

    INVALID_NAME(2001, "Invalid name.", HttpStatus.BAD_REQUEST),

    INVALID_EMAIL(2002, "Invalid email.", HttpStatus.BAD_REQUEST),
    NOT_UNIQUE_EMAIL(2003, "The email has been used by another user.", HttpStatus.BAD_REQUEST),

    INVALID_PHONE_NUMBER(2004, "Invalid phone number.", HttpStatus.BAD_REQUEST),
    NOT_UNIQUE_PHONE_NUMBER(2005, "The phone number has been used by another user.", HttpStatus.BAD_REQUEST),

    INVALID_PASSWORD(2006, "Please use at least one letter, one number, and seven characters.", HttpStatus.BAD_REQUEST),
    ;

    int code;
    String message;
    HttpStatusCode httpStatusCode;

}
