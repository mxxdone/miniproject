package com.mxxdone.miniproject.config;

import com.mxxdone.miniproject.exception.DuplicateException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

// 전역 예외 처리
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, String>> handleAccessDeniedException(AccessDeniedException e) {
        log.warn("Access denied: ", e);
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN) // 403 Forbidden 상태 코드 반환
                .body(Map.of("message", "접근 권한이 없습니다."));
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Map<String, String>> handleNoSuchElementException(NoSuchElementException e) {
        log.warn("Resource not found ", e);
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND) // 404 리소스 없음
                .body(Map.of("message", e.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handlerIllegalArgumentException(IllegalArgumentException e) {
        log.warn("Invalid argument: ", e);
        // 프론트엔드에 맞는 JSON 형식으로 에러 메세지 포장-전달
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleAllExceptions(Exception e) {
        log.error("Unhandled exception caught: ", e);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요,"));
    }

    // 서비스에서 중복 에러 처리(409)
    @ExceptionHandler(DuplicateException.class)
    public ResponseEntity<Map<String, String>> handleDuplicateException(DuplicateException e) {
        log.warn("Duplicate data encounter: ", e);
        return ResponseEntity
                .status(HttpStatus.CONFLICT) // 409 상태 코드
                .body(Map.of("message", e.getMessage())); // "이미 사용 중인 ~입니다" 메시지 전달
    }

    // DB 데이터 충돌 (중복id, 이메일 등) (409)
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>> handleDataIntegrityViolationException(DataIntegrityViolationException e) {
        log.warn("Data integrity violation: ", e);
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(Map.of("message", "이미 존재하는 데이터입니다."));
    }

    // DTO 유효성 검사 실패 (400)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException e) {
        log.warn("Validation failed: ", e);
        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of(
                        "message", "입력값이 올바르지 않습니다.",
                        "details", errors // 상세 필드 에러는 details 안에
                ));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> handleIllegalStateException(IllegalStateException e) {
        log.error("Service unavailable due to infrastructure failure: ", e);
        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE) // 503에러
                .body(Map.of("message", e.getMessage())); // AuthService에서 보낸 메세지 전달
    }
}