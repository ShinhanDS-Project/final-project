package com.merge.final_project.user.users.dto.support;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChangePasswordRequestDTO {
    // 필요한거 - 이메일, 이름 -> 이메일 인증하게끔 유도
    @NotBlank(message = "이메일을 입력해주세요")
    @Email
    private String email;

    @NotBlank //blank 안에 null 체크가 포함되어있으므로 NotNull 또 쓸 필요 없음
    private String name;


}
