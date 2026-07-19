package com.launchwindow.exception;

import com.launchwindow.dto.ApiErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ApiErrorResponse> handleUserAlreadyExists(UserAlreadyExistsException exception) {
        return createResponse(HttpStatus.CONFLICT, exception.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .orElse("Validation failed");

        return createResponse(HttpStatus.BAD_REQUEST, message);
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidCredentials(InvalidCredentialsException exception) {
        return createResponse(HttpStatus.UNAUTHORIZED, exception.getMessage());
    }

    private ResponseEntity<ApiErrorResponse> createResponse(HttpStatus status, String message) {
        ApiErrorResponse error = new ApiErrorResponse(status.value(), status.getReasonPhrase(), message);

        return ResponseEntity
                .status(status)
                .body(error);
    }
}
