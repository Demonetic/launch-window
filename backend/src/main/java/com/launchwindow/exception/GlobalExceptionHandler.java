package com.launchwindow.exception;

import com.launchwindow.dto.ApiErrorCode;
import com.launchwindow.dto.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ApiErrorResponse> handleUserAlreadyExists(UserAlreadyExistsException exception, HttpServletRequest request) {
        return createResponse(HttpStatus.CONFLICT, ApiErrorCode.USER_ALREADY_EXISTS, exception.getMessage(), request, Map.of());
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidCredentials(InvalidCredentialsException exception, HttpServletRequest request) {
        return createResponse(HttpStatus.UNAUTHORIZED, ApiErrorCode.INVALID_CREDENTIALS, exception.getMessage(), request, Map.of());
    }

    @ExceptionHandler(InvalidPaginationException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidPagination(InvalidPaginationException exception, HttpServletRequest request) {
        return createResponse(HttpStatus.BAD_REQUEST, ApiErrorCode.INVALID_PAGINATION, exception.getMessage(), request, Map.of());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException exception, HttpServletRequest request) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();

        exception.getBindingResult()
                .getFieldErrors()
                .forEach(error -> fieldErrors.putIfAbsent(error.getField(), error.getDefaultMessage() == null
                                ? "Invalid value"
                                : error.getDefaultMessage()
                ));

        return createResponse(HttpStatus.BAD_REQUEST, ApiErrorCode.VALIDATION_FAILED,"Request validation failed", request, fieldErrors);
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class, MissingServletRequestParameterException.class})
    public ResponseEntity<ApiErrorResponse> handleMalformedRequest(Exception exception, HttpServletRequest request) {
        return createResponse(HttpStatus.BAD_REQUEST, ApiErrorCode.MALFORMED_REQUEST, "Request could not be read", request, Map.of());
    }

    @ExceptionHandler(InvalidCalendarInvitationException.class)
    public ResponseEntity<ApiErrorResponse>
    handleInvalidCalendarInvitation(InvalidCalendarInvitationException exception, HttpServletRequest request) {
        return createResponse(HttpStatus.BAD_REQUEST, ApiErrorCode.INVALID_CALENDAR_INVITATION, exception.getMessage(), request, Map.of());
    }

    private ResponseEntity<ApiErrorResponse> createResponse(HttpStatus status, ApiErrorCode code, String message,
                                                            HttpServletRequest request, Map<String, String> fieldErrors) {
        ApiErrorResponse error = new ApiErrorResponse(
                Instant.now(),
                status.value(),
                code,
                message,
                request.getRequestURI(),
                fieldErrors
        );

        return ResponseEntity
                .status(status)
                .body(error);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleResourceNotFound(ResourceNotFoundException exception, HttpServletRequest request) {
        return createResponse(HttpStatus.NOT_FOUND, ApiErrorCode.RESOURCE_NOT_FOUND, exception.getMessage(), request, Map.of());
    }
}