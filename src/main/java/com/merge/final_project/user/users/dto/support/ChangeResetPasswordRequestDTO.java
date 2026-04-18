package com.merge.final_project.user.users.dto.support;

import com.merge.final_project.global.exceptions.NoRepeatedDigits;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;


@Getter
public class ChangeResetPasswordRequestDTO {

    @Email
    private String email;

    @NotBlank(message = "비밀번호를 입력해주세요.")
    @Size(min = 8, max = 20, message = "비밀번호는 8자 이상 20자 이하여야 합니다.")
    @Pattern(
            regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]).*$",
            message = "비밀번호는 영문, 숫자, 특수문자를 각각 1개 이상 포함해야 합니다."
    )
    @NoRepeatedDigits(message = "같은 숫자를 3번 이상 연속으로 사용할 수 없습니다.")
    private String newPassword;

    @NotBlank(message = "비밀번호를 한번 더 입력해주세요")
    @Size(min = 8, max = 20, message = "비밀번호는 8자 이상 20자 이하여야 합니다.")
    @Pattern(
            regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]).*$",
            message = "비밀번호는 영문, 숫자, 특수문자를 각각 1개 이상 포함해야 합니다."
    )
    @NoRepeatedDigits(message = "같은 숫자를 3번 이상 연속으로 사용할 수 없습니다.")
    private String newPassword2;
    private boolean isChecked; // 실제로 코드를 제대로 입력했는지 확인
}
