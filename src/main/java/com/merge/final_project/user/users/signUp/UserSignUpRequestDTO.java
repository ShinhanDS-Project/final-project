package com.merge.final_project.user.users.signUp;

import com.merge.final_project.user.users.LoginType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class UserSignUpRequestDTO {

   @NotBlank(message = "이름은 필수입니다.")
   @Email(message = "이메일 형식이 올바르지 않습니.")
   private String email;

   private String password;

   @NotBlank(message = "이름은 필수입니다.")
   private String name;

   private String phone;

   @NotNull(message = "생년월일은 필수입니다.")
   private LocalDate birth;

   private LoginType loginType;

   @NotBlank(message = "사진을 지정해주세요.")
   private String profilePath;

   //중복검사용 메서드 추가

}
