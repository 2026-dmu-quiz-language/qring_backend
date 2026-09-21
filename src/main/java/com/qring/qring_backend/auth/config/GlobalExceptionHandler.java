package com.qring.qring_backend.auth.config;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.qring.qring_backend.service.content.ContentLockedException;

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

    /** DB 제약 조건 위반(유니크/FK 등) → 409. SQL문·제약조건 이름은 응답에 노출하지 않는다. */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleDataIntegrity(DataIntegrityViolationException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
            "success", false,
            "code", "DATA_CONFLICT",
            "message", "이미 존재하는 데이터이거나 제약 조건을 위반했습니다."
        ));
    }

    /** 그 외 미처리 예외 → 500. */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleUnknown(Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "code", "INTERNAL_ERROR",
                "message", e.getMessage() != null ? e.getMessage() : "Unknown error"));
    }
    
    @ExceptionHandler(ContentLockedException.class)
    public ResponseEntity<?> handleContentLocked(ContentLockedException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
    }
}
