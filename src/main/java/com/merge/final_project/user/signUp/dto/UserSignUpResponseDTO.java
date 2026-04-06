package com.merge.final_project.user.signUp.dto;

import com.merge.final_project.user.users.LoginType;
import com.merge.final_project.user.users.UserStatus;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserSignUpResponseDTO {
    private Integer userNo;
    private String email;
    private UserStatus status;
    private LoginType loginType;
}
