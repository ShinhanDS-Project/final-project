package com.merge.final_project.admin.service;

// [가빈] 관리자 회원 활성화/비활성화 서비스
// 팀원(이채원) UserService와 분리하여 관리자 전용 기능만 여기서 처리
public interface AdminUserService {
    void activateUser(Long userNo);
    void deactivateUser(Long userNo);
}
