package com.mp.karental.exception;

import com.mp.karental.dto.response.ApiResponse;
import jakarta.validation.ConstraintViolation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * This class handle for exception all over the app
 * <p>
 * This provides a unified way to handle exception through out the application.
 * </p>
 *
 * @author DieuTTH4
 *
 * @version 1.0
 */
@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    private final String[] VALIDATORS_ATTRIBUTES = {
            "fieldName"
    };

    /**
     * Handle RuntimeException
     * @param e - the exception
     * @return ResponseEntity in form of defined ApiResponse, containing code and message of the exception
     *
     * @author DieuTTH4
     *
     * @version 1.0
     *
     * TEMPORARY disable for development
     */
    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiResponse> exceptionHandler(Exception e) {
        log.error(e.getMessage(), e);
        log.error(e.getStackTrace().toString());
        ApiResponse apiResponse = new ApiResponse();

        apiResponse.setCode(ErrorCode.UNCATEGORIZED_EXCEPTION.getCode());
        apiResponse.setMessage(ErrorCode.UNCATEGORIZED_EXCEPTION.getMessage());
        return ResponseEntity.badRequest().body(apiResponse);
    }


    /**
     * Handle APP EXCEPTION, custom exception
     * @param e the exception
     * @return ResponseEntity in form of defined ApiResponse, containing code and message of the exception
     *
     * @author DieuTTH4
     *
     * @version 1.0
     */
    @ExceptionHandler(AppException.class)
    ResponseEntity<ApiResponse> appExceptionHandler(AppException e) {
        log.info("Exception is catch by appExceptionHandler, exception: {}", e.getMessage());
        ErrorCode errorCode = e.getErrorCode();
        ApiResponse apiResponse = new ApiResponse();

        apiResponse.setCode(errorCode.getCode());
        apiResponse.setMessage(errorCode.getMessage());

        return ResponseEntity
                .status(errorCode.getHttpStatusCode())
                .body(apiResponse);
    }

    /**
     * Handle exception from validation
     * @param e the exception
     * @return ResponseEntity in form of defined ApiResponse, containing code and message of the exception
     *
     * @author DieuTTH4
     *
     * @version 1.0
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiResponse> methodArgumentNotValidException(MethodArgumentNotValidException e) {
        //Get the key of error code in the validation's message attribute
        ObjectError objectError = e.getBindingResult().getAllErrors().get(0);
        String enumKey = objectError.getDefaultMessage();

        //In case the key in the validation message is misspelled
        ErrorCode errorCode = ErrorCode.INVALID_ERROR_KEY;

        //Save attributes from the annotation
        Map validationAttributes  = null;

        try {
            errorCode = ErrorCode.valueOf(enumKey);
            //Get the first error in list of exceptions
            var constrainViolation = e.getBindingResult()
                    .getAllErrors().get(0)
                    .unwrap(ConstraintViolation.class); //Make it turn into ConstraintViolation for further action

            //Get out all the attributes in the annotation
            validationAttributes = constrainViolation.getConstraintDescriptor().getAttributes();
        } catch (IllegalArgumentException ex) {
            //Enum key is not valid
        }

        //Create the body for the response
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setCode(errorCode.getCode());
        apiResponse.setMessage(Objects.nonNull(validationAttributes)
                ? mapAttributeMessage(errorCode.getMessage(), validationAttributes)
                : errorCode.getMessage());

        return ResponseEntity
                .status(errorCode.getHttpStatusCode())
                .body(apiResponse);
    }
    private String mapAttributeMessage(String message, Map<String, Object> validationAttributes) {
        //there are attributes in the validator annotation
        if (validationAttributes != null) {
            for (String attribute : VALIDATORS_ATTRIBUTES) {
                log.info("attribute: " + attribute);
                //the attributes exist in the validation attribute
                if (validationAttributes.containsKey(attribute)) {
                    log.info("exist attributes in the validation attributes: " + validationAttributes.get(attribute));
                    String value = validationAttributes.get(attribute).toString();
                    //replace the value of the attribute to the error message
                    message = message.replace("{" + attribute + "}", value);
                }
            }
        }
        return message;
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiResponse<?>> handleMaxSizeException(MaxUploadSizeExceededException e) {
        ApiResponse apiResponse = new ApiResponse();

        apiResponse.setCode(ErrorCode.MAXIMUM_FILE_UPLOAD_EXCEED.getCode());
        apiResponse.setMessage(ErrorCode.MAXIMUM_FILE_UPLOAD_EXCEED.getMessage());

        return ResponseEntity
                .status(ErrorCode.MAXIMUM_FILE_UPLOAD_EXCEED.getHttpStatusCode())
                .body(apiResponse);
    }

}
