package com.example.books;

import java.util.Map;
import java.util.TreeMap;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.method.ParameterErrors;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class BookExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> invalidBody(MethodArgumentNotValidException exception) {
        Map<String, String> errors = new TreeMap<>();
        exception.getBindingResult().getFieldErrors().forEach(error ->
                errors.putIfAbsent(error.getField(), error.getDefaultMessage()));
        return ResponseEntity.badRequest().body(new ApiError("Invalid book request", errors));
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ApiError> invalidParameters(HandlerMethodValidationException exception) {
        Map<String, String> errors = new TreeMap<>();
        exception.getParameterValidationResults().forEach(result -> {
            if (result instanceof ParameterErrors parameterErrors) {
                parameterErrors.getFieldErrors().forEach(error ->
                        errors.putIfAbsent(error.getField(), error.getDefaultMessage()));
            } else {
                result.getResolvableErrors().forEach(error ->
                        errors.putIfAbsent("id", error.getDefaultMessage()));
            }
        });
        return ResponseEntity.badRequest().body(new ApiError("Invalid book request", errors));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> nonNumericId(MethodArgumentTypeMismatchException exception) {
        return ResponseEntity.badRequest().body(
                new ApiError("Book ID must be a number", Map.of("id", "Book ID must be a number")));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> unreadableBody(HttpMessageNotReadableException exception) {
        return ResponseEntity.badRequest().body(new ApiError("Invalid JSON request body", Map.of()));
    }

    @ExceptionHandler(BookNotFoundException.class)
    public ResponseEntity<ApiError> missingBook(BookNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiError(exception.getMessage(), Map.of()));
    }

    public record ApiError(String message, Map<String, String> errors) {
    }
}
