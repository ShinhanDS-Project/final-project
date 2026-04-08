package com.merge.final_project.user.users.dto.support;

import com.merge.final_project.user.users.LoginType;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class EmailResponseDTO{
    @NotBlank(message="이름은 필수입니다.")
    String email;

    LoginType loginType;
}
