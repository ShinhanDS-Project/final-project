package com.merge.final_project.admin.service;

import com.merge.final_project.admin.Admin;
import com.merge.final_project.admin.AdminRepository;
import com.merge.final_project.admin.adminlog.ActionType;
import com.merge.final_project.admin.adminlog.AdminLogService;
import com.merge.final_project.admin.adminlog.TargetType;
import com.merge.final_project.admin.dto.AdminUserResponseDTO; // [가빈] 추가
import com.merge.final_project.global.exceptions.BusinessException;
import com.merge.final_project.global.exceptions.ErrorCode;
import com.merge.final_project.user.users.User;
import com.merge.final_project.user.users.UserRepository;
import com.merge.final_project.user.users.UserStatus;
import org.springframework.data.domain.Page; // [가빈] 추가
import org.springframework.data.domain.Pageable; // [가빈] 추가
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class AdminUserServiceImpl implements AdminUserService {

    private final UserRepository userRepository;
    private final AdminRepository adminRepository;
    private final AdminLogService adminLogService;

    @Transactional
    @Override
    public void activateUser(Long userNo) {
        User user = userRepository.findById(userNo)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (user.getStatus() == UserStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.USER_ALREADY_ACTIVE);
        }

        user.activate();

        Admin admin = getAdmin();
        adminLogService.log(ActionType.APPROVE, TargetType.USERS, userNo,
                user.getName() + " 회원 활성화", admin);
    }

    @Transactional
    @Override
    public void deactivateUser(Long userNo) {
        User user = userRepository.findById(userNo)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (user.getStatus() == UserStatus.INACTIVE) {
            throw new BusinessException(ErrorCode.USER_ALREADY_INACTIVE);
        }

        user.deactivate();

        Admin admin = getAdmin();
        adminLogService.log(ActionType.REJECT, TargetType.USERS, userNo,
                user.getName() + " 회원 비활성화", admin);
    }

    // [가빈] 회원 목록 조회
    @Override
    @Transactional(readOnly = true)
    public Page<AdminUserResponseDTO> getUsers(UserStatus status, String keyword, Pageable pageable) {
        return userRepository.findUsersWithFilter(status, keyword, pageable)
                .map(AdminUserResponseDTO::from);
    }

    private Admin getAdmin() {
        String adminId = Objects.requireNonNull(
                SecurityContextHolder.getContext().getAuthentication()).getName();
        return adminRepository.findByAdminId(adminId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ADMIN_NOT_FOUND));
    }
}
