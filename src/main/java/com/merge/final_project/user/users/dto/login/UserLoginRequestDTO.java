package com.merge.final_project.user.users.dto.login;

import lombok.Getter;
import org.hibernate.usertype.UserType;

@Getter
public class UserLoginRequestDTO {
    private String email;
    private String password;
    private UserType userType;
}
