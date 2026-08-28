package com.echolife.session.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<Map<String,Object>> validation(MethodArgumentNotValidException ex) {
        return response(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR");
    }

    @ExceptionHandler(AccessDeniedException.class)
    ResponseEntity<Map<String,Object>> denied(AccessDeniedException ex) {
        return response(HttpStatus.FORBIDDEN, ex.getMessage() == null ? "ACCESS_DENIED" : ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<Map<String,Object>> bad(IllegalArgumentException ex) {
        return response(HttpStatus.BAD_REQUEST, ex.getMessage() == null ? "REQUEST_REJECTED" : ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<Map<String,Object>> internal(Exception ex) {
        return response(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR");
    }

    private ResponseEntity<Map<String,Object>> response(HttpStatus status, String code) {
        return ResponseEntity.status(status).body(Map.of("errorCode", code, "timestamp", Instant.now().toString()));
    }
}
