package com.merge.final_project.user.users.dto.support;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;

@Getter
public class EmailRequestDTO {

    @NotBlank(message = "이름은 필수입니다.")
    String name;

    @NotBlank(message = "전화번호는 필수입니다.")
    String phone;
}
