package com.merge.final_project.user.users.dto.support;

import com.merge.final_project.user.users.LoginType;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class EmailResponseDTO{
    @NotBlank(message="아이디는 필수입니다.")
    private String email;

    private LoginType loginType;
}
