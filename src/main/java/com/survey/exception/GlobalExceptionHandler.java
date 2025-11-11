package com.survey.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // 🔹 Resource Not Found
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<APIError> handleNotFound(ResourceNotFoundException ex, HttpServletRequest req) {
        logger.error("Resource not found: {}", ex.getMessage());
        APIError err = new APIError(HttpStatus.NOT_FOUND.value(),
                HttpStatus.NOT_FOUND.getReasonPhrase(), ex.getMessage(), req.getRequestURI());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(err);
    }

    // 🔹 Validation Errors (DTO validation)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<APIError> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        List<String> details = ex.getBindingResult().getFieldErrors()
                .stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .toList();
        APIError err = new APIError(HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(), "Validation failed", req.getRequestURI());
        err.setValidationErrors(details);
        return ResponseEntity.badRequest().body(err);
    }

    // 🔹 Illegal state (for logical violations)
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<APIError> handleIllegalState(IllegalStateException ex, HttpServletRequest req) {
        logger.error("Illegal state: {}", ex.getMessage());
        APIError err = new APIError(HttpStatus.CONFLICT.value(),
                HttpStatus.CONFLICT.getReasonPhrase(), ex.getMessage(), req.getRequestURI());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(err);
    }

    // 🔹 Concurrent update
    @ExceptionHandler(CustomConcurrentUpdateException.class)
    public ResponseEntity<APIError> handleConcurrentUpdate(CustomConcurrentUpdateException ex, HttpServletRequest req) {
        logger.warn("Concurrent update: {}", ex.getMessage());
        APIError err = new APIError(HttpStatus.CONFLICT.value(),
                HttpStatus.CONFLICT.getReasonPhrase(), ex.getMessage(), req.getRequestURI());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(err);
    }

    // 🔹 Unauthorized / Forbidden
    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<APIError> handleAccessDenied(AuthorizationDeniedException ex, HttpServletRequest req) {
    	APIError err = new APIError(HttpStatus.FORBIDDEN.value(),
                HttpStatus.FORBIDDEN.getReasonPhrase(), "Access Denied", req.getRequestURI());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(err);
    }

    @ExceptionHandler(UserApiException.class)
    public ResponseEntity<APIError> handleUserApi(UserApiException ex, HttpServletRequest req) {
    	APIError err = new APIError(HttpStatus.UNAUTHORIZED.value(),
                HttpStatus.UNAUTHORIZED.getReasonPhrase(), ex.getMessage(), req.getRequestURI());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(err);
    }

    // 🔹 Generic fallback for all other exceptions
    @ExceptionHandler(Exception.class)
    public ResponseEntity<APIError> handleGeneric(Exception ex, HttpServletRequest req) {
        logger.error("Unexpected error: ", ex);
        APIError err = new APIError(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(), ex.getMessage(), req.getRequestURI());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(err);
    }
}
