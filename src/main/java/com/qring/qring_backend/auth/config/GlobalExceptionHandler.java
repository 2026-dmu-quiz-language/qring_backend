package com.qring.qring_backend.auth.config;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

/** 컨트롤러 공통 예외 처리: 도메인 오류·검증 실패·미처리 예외를 통일된 JSON 포맷으로 변환. */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** 도메인 비즈니스 오류 (코드 문자열을 message로 사용) → 400. */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
            "success", false,
            "code", e.getMessage(),
            "message", e.getMessage()
        ));
    }

    /** Bean Validation 실패 → 첫 필드 에러 메시지로 400. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
            .findFirst()
            .map(f -> f.getField() + ": " + f.getDefaultMessage())
            .orElse("VALIDATION_ERROR");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
            "success", false,
            "code", "VALIDATION_ERROR",
            "message", msg
        ));
    }

    /** 그 외 미처리 예외 → 500. */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleUnknown(Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
            "success", false,
            "code", "INTERNAL_ERROR",
            "message", e.getMessage() != null ? e.getMessage() : "Unknown error"
        ));
    }
}
