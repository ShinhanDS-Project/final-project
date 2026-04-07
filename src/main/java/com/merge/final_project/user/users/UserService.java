package com.merge.final_project.user.users;

import com.merge.final_project.user.signUp.dto.UserSignUpRequestDTO;
import com.merge.final_project.user.users.dto.login.UserLoginRequestDTO;

public interface UserService {
    public String login(UserLoginRequestDTO dto);

}
