package com.baridmedia.assuranceapi.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.WebRequest;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ConflictException.class)
    @ResponseBody
    public ResponseEntity<Object> handleConflict(ConflictException ex, WebRequest request) {
        return buildError(HttpStatus.CONFLICT, ex.getMessage(), request.getDescription(false));
    }

    @ExceptionHandler(BusinessException.class)
    @ResponseBody
    public ResponseEntity<Object> handleBusiness(BusinessException ex, WebRequest request) {
        return buildError(HttpStatus.valueOf(422), ex.getMessage(), request.getDescription(false));
    }


    private ResponseEntity<Object> buildError(HttpStatus status, String message, String path) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", Instant.now());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        body.put("path", path);
        return new ResponseEntity<>(body, status);
    }
}

