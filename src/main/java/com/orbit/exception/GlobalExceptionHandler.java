package com.orbit.exception;

import com.orbit.dto.response.ApiResponse;
import com.orbit.util.AppUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AppException.class)
    public ResponseEntity<ApiResponse<Void>> handleAppExceptions(AppException e) {
        log.error(e.getMessage(), e);
        ApiResponse.ApiResponseBuilder<Void> builder = ApiResponse.builder();
        return ResponseEntity.status(e.getStatus()).body(builder.errorMessage(e.getMessage()).build());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleOtherExceptions(Exception e) {
        log.error(e.getMessage(), e);
        ApiResponse.ApiResponseBuilder<Void> builder = ApiResponse.builder();
        builder.errorMessage(AppUtils.Status.UNEXPECTED_ERROR.getMessage(e.getMessage()));
        return ResponseEntity.internalServerError().body(builder.build());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        log.error(e.getMessage(), e);
        ApiResponse.ApiResponseBuilder<Void> builder = ApiResponse.builder();
        String message = String.format("Invalid value '%s' for parameter '%s'", e.getValue(), e.getName());
        return ResponseEntity.badRequest().body(builder.errorMessage(message).build());
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNoHandlerFound(NoHandlerFoundException e) {
        log.error(e.getMessage(), e);
        ApiResponse.ApiResponseBuilder<Void> builder = ApiResponse.builder();
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(builder.errorMessage("Resource not found").build());
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNoResourceFound(NoResourceFoundException e) {
        log.error(e.getMessage(), e);
        ApiResponse.ApiResponseBuilder<Void> builder = ApiResponse.builder();
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(builder.errorMessage("Resource not found").build());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException e) {
        log.error(e.getMessage(), e);
        ApiResponse.ApiResponseBuilder<Void> builder = ApiResponse.builder();
        Map<String, String> errors = getErrorAttributes(e.getBindingResult());
        builder.errorMessage(AppUtils.Status.VALIDATION_FAILED.getMessage()).errors(errors);
        return ResponseEntity.badRequest().body(builder.build());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException e) {
        log.error(e.getMessage(), e);
        ApiResponse.ApiResponseBuilder<Void> builder = ApiResponse.builder();
        builder.errorMessage(AppUtils.Status.INVALID_REQUEST_BODY.getMessage());
        return ResponseEntity.badRequest().body(builder.build());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDeniedException(AccessDeniedException e) {
        log.error(e.getMessage(), e);
        ApiResponse.ApiResponseBuilder<Void> builder = ApiResponse.builder();
        builder.errorMessage(AppUtils.Status.ACCESS_DENIED.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(builder.build());
    }

    private static Map<String, String> getErrorAttributes(BindingResult bindingResult) {
        Map<String, String> errorAttributes = new HashMap<>();
        bindingResult.getAllErrors().forEach(error -> {
            String fieldName = error.getObjectName();
            String errorMessage = error.getDefaultMessage();
            if (error instanceof FieldError fieldError) {
                fieldName = fieldError.getField();
            }
            errorAttributes.put(fieldName, errorMessage);
        });
        return errorAttributes;
    }
}
