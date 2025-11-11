package com.survey.exception;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import jakarta.servlet.http.HttpServletRequest;

@ControllerAdvice
public class GlobalExceptionHandler {
	private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	@ExceptionHandler(ResourceNotFoundException.class)
	public ResponseEntity<APIError> handleNotFound(ResourceNotFoundException ex, HttpServletRequest req) {
		  logger.error("Resource not found: {}", ex.getMessage());
		APIError err = new APIError();
		err.setStatus(HttpStatus.NOT_FOUND.value());
		err.setError(HttpStatus.NOT_FOUND.getReasonPhrase());
		err.setMessage(ex.getMessage());
		err.setPath(req.getRequestURI());
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(err);

	}


	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<APIError> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
		APIError err = new APIError();
		err.setStatus(HttpStatus.BAD_REQUEST.value());
		err.setError(HttpStatus.BAD_REQUEST.getReasonPhrase());
		err.setMessage("Validation failed");
		err.setPath(req.getRequestURI());
		List<String> details = ex.getBindingResult().getFieldErrors().stream()
				.map(fe -> fe.getField() + ": " + fe.getDefaultMessage()).toList();
		err.setValidationErrors(details);
		return ResponseEntity.badRequest().body(err);
	}


	@ExceptionHandler(IllegalStateException.class)
	public ResponseEntity<APIError> handleState(IllegalStateException ex, HttpServletRequest req) {
		APIError err = new APIError();
		err.setStatus(HttpStatus.CONFLICT.value());
		err.setError(HttpStatus.CONFLICT.getReasonPhrase());
		err.setMessage(ex.getMessage());
		err.setPath(req.getRequestURI());
		return ResponseEntity.status(HttpStatus.CONFLICT).body(err);
	}

	
	@ExceptionHandler(Exception.class)
	public ResponseEntity<APIError> handleGeneric(Exception ex, HttpServletRequest req) {
	    ex.printStackTrace(); 

		APIError err = new APIError();
		err.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
		err.setError(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase());
		err.setMessage(ex.getMessage());
		err.setPath(req.getRequestURI());
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(err);
	}

//	This is a fallback handler for any unhandled exception — runtime exceptions, programming errors, etc.
//	
//	{
//	    "status": 500,
//	    "error": "Internal Server Error",
//	    "message": "Unexpected error occurred: something went wrong!",
//	    "path": "/api/employees"
//	}


	 @ExceptionHandler(CustomConcurrentUpdateException.class)
	    public ResponseEntity<String> handleConcurrentUpdateException(CustomConcurrentUpdateException ex) {
	        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
	    }
	 

@ExceptionHandler(AuthorizationDeniedException.class)
public ResponseEntity<String> handleAccessDenied(AuthorizationDeniedException ex) {
    return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access Denied");
}

@ExceptionHandler(UserApiException.class)
public ResponseEntity<?> handleUserApiException(UserApiException ex) {
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
        .body(Map.of("error", ex.getMessage()));
}
}


