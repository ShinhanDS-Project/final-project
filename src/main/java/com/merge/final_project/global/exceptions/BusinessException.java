package com.merge.final_project.global.exceptions;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage()); // 부모 메시지 설정
        this.errorCode = errorCode;    // 에러코드 저장
    }
}