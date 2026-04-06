package com.merge.final_project.org;

public enum ReviewStatus {
    ILLEGAL,   // 불법단체 정확 일치
    SIMILAR,   // 유사 단체 존재
    CLEAN,     // 이상 없음
    APPROVED,  // 관리자 승인
    REJECTED   // 관리자 반려
}
