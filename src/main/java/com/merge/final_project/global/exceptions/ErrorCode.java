package com.merge.final_project.global.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    AUTHENTICATION_FAILED(HttpStatus.UNAUTHORIZED, "ADMIN_001","아이디 또는 비밀번호가 올바르지 않습니다."),
    // 보안을 위해 외부로는 안 남기지만 내부 로깅 용으로 남길 코드들.
    ADMIN_NOT_FOUND(HttpStatus.NOT_FOUND, "ADMIN_002", "존재하지 않는 관리자입니다."),
    INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, "ADMIN_003", "비밀번호가 일치하지 않습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}
