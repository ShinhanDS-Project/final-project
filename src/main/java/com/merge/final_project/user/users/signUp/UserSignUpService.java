package com.merge.final_project.user.users.signUp;

import com.merge.final_project.user.users.User;
import com.merge.final_project.user.users.UserStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import static com.merge.final_project.user.users.signUp.UserHashGenerater.generateUserHash;

public interface UserSignUpService {
    void register(UserSignUpRequestDTO requestDto) throws IllegalAccessException;
    public void registerLocal(UserSignUpRequestDTO dto);
    public void registerGoogle(UserSignUpRequestDTO dto);
}
