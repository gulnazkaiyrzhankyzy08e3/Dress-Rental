
package com.example.rental.handler;

import com.example.rental.domain.DuplicateRental;
import com.example.rental.domain.MissingRental;

import jakarta.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.UUID;

@RestControllerAdvice
public class ApiErrors {

    private static final Logger log =
            LoggerFactory.getLogger(ApiErrors.class);

    private String correlation(HttpServletRequest request) {
        String id = request.getHeader("X-Correlation-Id");

        if (id == null || id.isBlank()) {
            return UUID.randomUUID().toString();
        }

        return id;
    }

    private ResponseEntity<ApiError> error(
            HttpStatus status,
            String code,
            String message,
            HttpServletRequest request) {

        return ResponseEntity.status(status).body(
                new ApiError(code, message, correlation(request)));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> validation(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        var field = ex.getBindingResult().getFieldError();

        String message = field == null
                ? "invalid request"
                : field.getField() + " " + field.getDefaultMessage();

        return error(HttpStatus.BAD_REQUEST,
                "VALIDATION", message, request);
    }

    @ExceptionHandler(MissingRental.class)
    public ResponseEntity<ApiError> missing(
            MissingRental ex,
            HttpServletRequest request) {

        return error(HttpStatus.NOT_FOUND,
                "NOT_FOUND", ex.getMessage(), request);
    }

    @ExceptionHandler(DuplicateRental.class)
    public ResponseEntity<ApiError> duplicate(
            DuplicateRental ex,
            HttpServletRequest request) {

        return error(HttpStatus.CONFLICT,
                "DUPLICATE", ex.getMessage(), request);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> invalidId(
            MethodArgumentTypeMismatchException ex,
            HttpServletRequest request) {

        return error(HttpStatus.BAD_REQUEST,
                "VALIDATION", "id must be a UUID", request);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> invalidBody(
            HttpMessageNotReadableException ex,
            HttpServletRequest request) {

        return error(HttpStatus.BAD_REQUEST,
                "VALIDATION", "body must be JSON", request);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiError> unsupported(
            HttpMediaTypeNotSupportedException ex,
            HttpServletRequest request) {

        return error(HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                "UNSUPPORTED", "unsupported media type", request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> internal(
            Exception ex,
            HttpServletRequest request) {

        String id = correlation(request);
        log.error("Unexpected error correlationId={}", id, ex);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiError(
                        "INTERNAL", "unexpected error", id));
    }
}
