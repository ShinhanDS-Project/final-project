package com.merge.final_project.user.signUp.dto;

import com.merge.final_project.global.exceptions.NoRepeatedDigits;
import com.merge.final_project.user.users.LoginType;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;
@AllArgsConstructor
@Getter
@Setter
@Builder
@NoArgsConstructor
public class UserSignUpRequestDTO {

   @NotBlank(message = "이메일은 필수입니다.")
   @Email(message = "이메일 형식이 올바르지 않습니다.")
   private String email;
   @NotBlank(message = "비밀번호는 필수입니다.")
   @Size(min = 8, max = 20, message = "비밀번호는 8자 이상 20자 이하여야 해.")
   @Pattern(
           regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]).*$",
           message = "비밀번호는 영문, 숫자, 특수문자를 각각 1개 이상 포함해야 해."
   )
   @NoRepeatedDigits(message = "같은 숫자를 3번 이상 연속으로 사용할 수 없어.")
   private String password;

   @NotBlank(message = "이름은 필수입니다.")
   private String name;

   @NotBlank(message = "휴대폰 번호는 필수입니다.")
   @Pattern(regexp = "^\\d{2,3}-\\d{3,4}-\\d{4}$", message = "핸드폰 번호의 형식과 맞지 않습니다. xxx-xxxx-xxxx 또는 xx-xxx-xxxx 형태여야 합니다.")
   private String phone;

   @NotNull(message = "생년월일은 필수입니다.")
   private LocalDate birth;


   private LoginType loginType;


   private String profilePath;




   //중복검사용 메서드 추가

}
