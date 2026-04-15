package com.merge.final_project.admin.dto;

import com.merge.final_project.user.users.LoginType;
import com.merge.final_project.user.users.User;
import com.merge.final_project.user.users.UserStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class AdminUserResponseDTO {

    private Long userNo;
    private String name;
    private String email;
    private UserStatus status;
    private LoginType loginType;
    private LocalDateTime createdAt;

    public static AdminUserResponseDTO from(User user) {
        return AdminUserResponseDTO.builder()
                .userNo(user.getUserNo())
                .name(user.getName())
                .email(user.getEmail())
                .status(user.getStatus())
                .loginType(user.getLoginType())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
