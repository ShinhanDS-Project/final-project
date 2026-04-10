package com.merge.final_project.global.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    // 사용자
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_001", "존재하지 않는 사용자입니다."),

    AUTHENTICATION_FAILED(HttpStatus.UNAUTHORIZED, "ADMIN_001","아이디 또는 비밀번호가 올바르지 않습니다."),
    // 보안을 위해 외부로는 안 남기지만 내부 로깅 용으로 남길 코드들.
    ADMIN_NOT_FOUND(HttpStatus.NOT_FOUND, "ADMIN_002", "존재하지 않는 관리자입니다."),
    INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, "ADMIN_003", "비밀번호가 일치하지 않습니다."),
    // 결제
    PAYMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "PAYMENT_001", "존재하지 않는 결제 내역입니다."),
    PAYMENT_AMOUNT_MISMATCH(HttpStatus.BAD_REQUEST, "PAYMENT_002", "결제 금액이 일치하지 않습니다."),
    PAYMENT_ALREADY_COMPLETED(HttpStatus.CONFLICT, "PAYMENT_003", "이미 완료된 결제입니다."),
    PAYMENT_INVALID_STATUS(HttpStatus.BAD_REQUEST, "PAYMENT_004", "유효하지 않은 결제 상태입니다."),
    PAYMENT_CONFIRM_FAILED(HttpStatus.BAD_GATEWAY, "PAYMENT_005", "결제 승인에 실패했습니다."),
    PAYMENT_READY_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "PAYMENT_006", "결제 준비 중 오류가 발생했습니다."),
    PAYMENT_METHOD_MISMATCH(HttpStatus.BAD_REQUEST,"PAYMENT_007","유효하지않은 결제 방식입니다."),
    // 기부단체 가입 관련
    DUPLICATE_BUSINESS_REGISTRATION(HttpStatus.CONFLICT, "FOUNDATION_001", "이미 신청된 사업자등록번호입니다."),
    FOUNDATION_NOT_FOUND(HttpStatus.NOT_FOUND, "FOUNDATION_002", "존재하지 않는 기부단체입니다."),
    CANNOT_APPROVE_ILLEGAL_FOUNDATION(HttpStatus.CONFLICT, "FOUNDATION_003", "불성실기부단체에 포함되는 단체는 가입이 안됩니다."),
    FOUNDATION_LOGIN_FAILED(HttpStatus.UNAUTHORIZED, "FOUNDATION_004", "이메일 또는 비밀번호가 올바르지 않습니다."),
    FOUNDATION_NOT_ACTIVATED(HttpStatus.FORBIDDEN, "FOUNDATION_005", "승인되지 않은 단체입니다. 관리자 승인 후 로그인 가능합니다."),
    // 파일 관련
    FILE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "FILE_001", "파일 업로드에 실패했습니다."),
    FILE_UPLOAD_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "FILE_002", "파일 업로드 중 오류가 발생했습니다."),
    INVALID_FILE_TYPE(HttpStatus.BAD_REQUEST, "FILE_003", "지원하지 않는 파일 형식입니다."),
    // 공통 입력값 검증
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "COMMON_001", "입력값이 올바르지 않습니다."),
    // 메일
    MAIL_SEND_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "MAIL_001", "메일 발송에 실패했습니다."),
    // 캠페인
    CAMPAIGN_NOT_FOUND(HttpStatus.NOT_FOUND, "CAMPAIGN_001", "존재하지 않는 캠페인입니다."),
    CAMPAIGN_NOT_ACTIVE(HttpStatus.BAD_REQUEST, "CAMPAIGN_002", "현재 모금기간이 아닙니다."),
    CAMPAIGN_WALLET_NOT_FOUND(HttpStatus.NOT_FOUND, "CAMPAIGN_003", "캠페인 지갑 정보를 찾을 수 없습니다."),
    // 기부단체
    FOUNDATION_INACTIVE(HttpStatus.FORBIDDEN, "FOUNDATION_006", "현재 후원 가능한 기부단체가 아닙니다."),
    // 기부
    DONATION_CREATE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "DONATION_001", "기부 내역 생성에 실패했습니다."),
    // 알림
    NOTIFICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "NOTIFICATION_001", "존재하지 않는 알림입니다."),
    NOTIFICATION_ACCESS_DENIED(HttpStatus.FORBIDDEN, "NOTIFICATION_002", "해당 알림에 접근 권한이 없습니다.");



    private final HttpStatus status;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}
