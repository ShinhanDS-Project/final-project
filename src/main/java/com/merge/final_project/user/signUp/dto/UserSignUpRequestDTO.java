package com.merge.final_project.user.signUp.dto;

import com.merge.final_project.user.users.LoginType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.*;

import java.time.LocalDate;
@AllArgsConstructor
@Getter
@Setter
@Builder
@NoArgsConstructor
public class UserSignUpRequestDTO {


   @Email(message = "이메일 형식이 올바르지 않습니다.")
   private String email;

   private String password;


   private String name;

   @Pattern(regexp = "^\\d{2,3}-\\d{3,4}-\\d{4}$", message = "핸드폰 번호의 형식과 맞지 않습니다. xxx-xxxx-xxxx 또는 xx-xxx-xxxx 형태여야 합니다.")
   private String phone;

   @NotNull(message = "생년월일은 필수입니다.")
   private LocalDate birth;

   private LoginType loginType;


   private String profilePath;




   //중복검사용 메서드 추가

}
