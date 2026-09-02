package com.fooddelivery.payment.exception;

import com.fooddelivery.payment.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(PaymentNotFoundException.class)
    public ResponseEntity<ErrorResponse> handlePaymentNotFound(PaymentNotFoundException ex) {
        ErrorResponse response = ErrorResponse.builder()
                .status(HttpStatus.NOT_FOUND.value())
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(DuplicatePaymentException.class)
    public ResponseEntity<ErrorResponse> handleDuplicatePayment(DuplicatePaymentException ex) {
        ErrorResponse response = ErrorResponse.builder()
                .status(HttpStatus.CONFLICT.value())
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(PaymentProcessingException.class)
    public ResponseEntity<ErrorResponse> handlePaymentProcessing(PaymentProcessingException ex) {
        ErrorResponse response = ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(UnauthorizedPaymentAccessException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorizedAccess(UnauthorizedPaymentAccessException ex) {
        ErrorResponse response = ErrorResponse.builder()
                .status(HttpStatus.FORBIDDEN.value())
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.toList());

        ErrorResponse response = ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .message("Validation failed")
                .timestamp(LocalDateTime.now())
                .errors(errors)
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        ErrorResponse response = ErrorResponse.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .message(ex.getMessage() != null ? ex.getMessage() : "An unexpected payment error occurred")
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
