package com.merge.final_project.user.users.dto.login;

import com.merge.final_project.global.exceptions.NoRepeatedDigits;
import com.merge.final_project.user.users.LoginType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.usertype.UserType;


@Getter
@Setter
public class UserLoginRequestDTO {
    @NotBlank
    @Email
    private String email;

    @NotBlank(message = "비밀번호는 필수입니다.")
    @Size(min = 8, max = 20, message = "비밀번호는 8자 이상 20자 이하여야 해.")
    @Pattern(
            regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]).*$",
            message = "비밀번호는 영문, 숫자, 특수문자를 각각 1개 이상 포함해야 해."
    )
    @NoRepeatedDigits(message = "같은 숫자를 3번 이상 연속으로 사용할 수 없어.")
    private String password;
    private LoginType userType;


}
