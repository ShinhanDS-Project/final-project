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
    @NotBlank(message = "이메일을 입력해주세요.")
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    private String email;

    @NotBlank(message = "비밀번호를 입력해주세요.")
    private String password;
    
    private LoginType userType;
}
