package com.merge.final_project.user.verify.dto;

import com.merge.final_project.user.users.LoginType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserVerifyRequestDTO {
    @NotBlank(message="이메일은 필수입니다.")
    @Email(message="올바른 이메일 형식이 아닙니다.")
    private String email;

    @NotNull
    private LoginType loginType;

}
