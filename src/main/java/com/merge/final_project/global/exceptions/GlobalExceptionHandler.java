package com.merge.final_project.global.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * @Valid 검증 실패를 400 응답으로 변환한다.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getAllErrors()
                .stream()
                .findFirst()
                .map(error -> error.getDefaultMessage())
                .orElse("요청 값이 올바르지 않습니다.");
        return ResponseEntity.badRequest()
                .body(new ErrorResponse("VALID_001", message));
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException e) {
        ErrorCode errorCode = e.getErrorCode();
        return ResponseEntity.status(errorCode.getStatus())
                .body(new ErrorResponse(errorCode.getCode(), errorCode.getMessage()));
    }

    /**
     * 서비스에서 의도적으로 던진 HTTP 상태 예외를 그대로 유지해 반환한다.
     * (예: 400 invalid status, 404 transaction not found)
     */
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleResponseStatusException(ResponseStatusException e) {
        int statusCode = e.getStatusCode().value();
        String reason = e.getReason() == null || e.getReason().isBlank()
                ? "요청 처리 중 오류가 발생했습니다."
                : e.getReason();
        return ResponseEntity.status(e.getStatusCode())
                .body(new ErrorResponse("COMMON_" + statusCode, reason));
    }

    /**
     * 처리되지 않은 런타임 예외의 최종 fallback.
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("COMMON_500", e.getMessage()));
    }
}
