package com.edms.customerservice.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationException(MethodArgumentNotValidException ex) {
        final Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getFieldErrors().forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));

        return ResponseEntity.badRequest().body(errors);
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<Map<String, String>> handleEmailAlreadyExistsException(EmailAlreadyExistsException ex) {
        final Map<String, String> errors = new HashMap<>();
        errors.put("message", ex.getLocalizedMessage());

        log.warn(ex.getLocalizedMessage());

        return ResponseEntity.badRequest().body(errors);
    }

    @ExceptionHandler(CustomerNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleCustomerNotFoundException(CustomerNotFoundException ex) {
        Map<String, String> errors = new HashMap<>();
        errors.put("message", ex.getLocalizedMessage());

        log.warn(ex.getLocalizedMessage());

        return ResponseEntity.badRequest().body(errors);
    }
}
