package com.smu.energydatatradingapp.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpServletRequest;

/**
 * This ApiExceptionHandler class is the global exception handler for all API
 * controller.
 */
@ControllerAdvice
public class ApiExceptionHandler {

    /**
     * Exception handler for DataNotFoundException. Response status is "Not
     * Found 404" error.
     * @param ex DataNotFoundException object
     * @param request HttpServletRequest object
     * @return ResponseEntity Object
     */
    @ExceptionHandler(value = {DataNotFoundException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<Object> handleDataNotFoundException (DataNotFoundException ex, HttpServletRequest request) {
        ExceptionResponse exceptionResponse = new ExceptionResponse(
                HttpStatus.NOT_FOUND.value(),
                ex.getMessage(),
                request.getServletPath()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exceptionResponse);
    }

    /**
     * Exception handler for IllegalArgumentException. Response status is "Bad
     * Request 400" error.
     * @param ex IllegalArgumentException object
     * @param request HttpServletRequest object
     * @return ResponseEntity Object
     */
    @ExceptionHandler(value = {IllegalArgumentException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<Object> handleIllegalArgumentException (IllegalArgumentException ex, HttpServletRequest request) {
        String message = "Invalid parameter value passed. ";

        ExceptionResponse exceptionResponse = new ExceptionResponse(
                HttpStatus.BAD_REQUEST.value(),
                message + ex.getMessage(),
                request.getServletPath()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exceptionResponse);
    }
}
