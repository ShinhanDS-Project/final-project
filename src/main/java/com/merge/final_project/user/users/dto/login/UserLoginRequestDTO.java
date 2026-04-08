package com.merge.final_project.user.users.dto.login;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.usertype.UserType;

@Getter
@Setter
public class UserLoginRequestDTO {
    @NotBlank
    @Email
    private String email;

    private String password;
    private UserType userType;
}
