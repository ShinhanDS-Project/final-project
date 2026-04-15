package com.merge.final_project.user.users.dto.support;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import java.time.LocalDate;
@Getter
@Builder
public class MyInfoResponseDTO {


    private String email;
    // 이메일(수정불가)

    private String name;
    // 이름(수정불가)


    private String phone;
    // 핸드폰 번호(수정불가)

    private String nameHash; // 닉네임으로 사용

    // 닉네임 수정

    private String profilePath;
    // 프로필 사진 수정

    //이채원:[누락] 생일(수정불가)
    private LocalDate birth;
}
