package com.merge.final_project.admin.dto;

import com.merge.final_project.user.users.LoginType;
import com.merge.final_project.user.users.User;
import com.merge.final_project.user.users.UserStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class AdminUserDetailResponseDTO {

    private Long userNo;
    private String name;
    private String email;
    private String phone;
    private LocalDate birth;
    private String profilePath;
    private UserStatus status;
    private LoginType loginType;
    private Integer loginCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static AdminUserDetailResponseDTO from(User user) {
        return AdminUserDetailResponseDTO.builder()
                .userNo(user.getUserNo())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .birth(user.getBirth())
                .profilePath(user.getProfilePath())
                .status(user.getStatus())
                .loginType(user.getLoginType())
                .loginCount(user.getLoginCount())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
