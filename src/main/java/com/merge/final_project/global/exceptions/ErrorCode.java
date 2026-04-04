package com.merge.final_project.global.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    ADMIN_NOT_FOUND(HttpStatus.NOT_FOUND, "ADMIN_001", "존재하지 않는 관리자입니다."),
    INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, "ADMIN_002", "비밀번호가 일치하지 않습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}
