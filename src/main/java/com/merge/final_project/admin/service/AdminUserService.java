package com.merge.final_project.admin.service;

import com.merge.final_project.admin.dto.AdminUserResponseDTO;
import com.merge.final_project.user.users.User;
import com.merge.final_project.user.users.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

// [가빈] 관리자 회원 활성화/비활성화 서비스
// 팀원(이채원) UserService와 분리하여 관리자 전용 기능만 여기서 처리
public interface AdminUserService {
    void activateUser(Long userNo);
    void deactivateUser(Long userNo);

    // [가빈] 회원 목록 조회 (상태 필터 + 키워드 검색 + 페이징)
    Page<AdminUserResponseDTO> getUsers(UserStatus status, String keyword, Pageable pageable);
    User getUserById (Long userNo);
}
