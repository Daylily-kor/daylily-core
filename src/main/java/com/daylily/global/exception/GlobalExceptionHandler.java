package com.daylily.global.exception;

import com.daylily.global.response.ErrorResponse;
import com.daylily.global.response.code.ErrorCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {
    public record FieldErrorRecord(String field, String reason) {
        public static FieldErrorRecord of(FieldError fieldError) {
            return new FieldErrorRecord(fieldError.getField(), Optional.ofNullable(fieldError.getDefaultMessage()).orElse("Invalid value"));
        }

        public static FieldErrorRecord of(ObjectError objectError) {
            return new FieldErrorRecord(objectError.getObjectName(), Optional.ofNullable(objectError.getDefaultMessage()).orElse("Invalid value"));
        }
    }

    /**
     * Binding 과정에서 발생하는 예외를 처리합니다. 아래는 응답 data 예시입니다.
     * <pre>
     * "data": [
     *     {
     *       "field": "name",
     *       "reason": "must be less than or equal to 20"
     *     },
     *     {
     *       "field": "name",
     *       "reason": "must be greater than or equal to 1"
     *     }
     * ]
     * </pre>
     * @param e MethodArgumentNotValidException
     * @return FieldErrorRecord의 Set을 포함하는 ErrorResponse
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<ErrorResponse<Set<FieldErrorRecord>>> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        // Binding 과정에서 발생한 FieldError와 GlobalError를 수집
        final Set<FieldErrorRecord> errors = e.getBindingResult().getFieldErrors()
                .stream()
                .map(FieldErrorRecord::of)
                .collect(Collectors.toSet());

        errors.addAll(
                e.getBindingResult().getGlobalErrors()
                .stream()
                .map(FieldErrorRecord::of)
                .collect(Collectors.toSet())
        );

        return ErrorResponse.of(ErrorCode.MALFORMED_REQUEST, errors);
    }

    // Binding 과정에서 발생하는 예외 처리
    @ExceptionHandler(BindException.class)
    protected ResponseEntity<ErrorResponse<Void>> handleBindException(BindException e) {
        return ErrorResponse.of(ErrorCode.MALFORMED_REQUEST);
    }

    // enum 타입이 잘못되어 binding 될 수 없을 때 발생하는 예외 처리
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    protected ResponseEntity<ErrorResponse<Void>> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {
        return ErrorResponse.of(ErrorCode.MALFORMED_REQUEST);
    }

    // 지원하지 않는 HTTP 메소드로 요청이 들어올 때 발생하는 예외 처리
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    protected ResponseEntity<ErrorResponse<Void>> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        return ErrorResponse.of(ErrorCode.METHOD_NOT_ALLOWED);
    }

    // 요청을 읽을 수 없을 때 발생하는 예외 처리
    @ExceptionHandler(HttpMessageNotReadableException.class)
    protected ResponseEntity<ErrorResponse<Void>> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        return ErrorResponse.of(ErrorCode.BAD_REQUEST);
    }

    // Handle Daylily BaseException
    @ExceptionHandler(BaseException.class)
    protected ResponseEntity<ErrorResponse<Void>> handleBaseException(BaseException e) {
        return ErrorResponse.of(e.getErrorCode());
    }

    // Handle rest of unhandled exceptions
    @ExceptionHandler(Exception.class)
    protected ResponseEntity<ErrorResponse<Void>> handleException(Exception e) {
        return ErrorResponse.of(ErrorCode.INTERNAL_SERVER_ERROR);
    }
}
