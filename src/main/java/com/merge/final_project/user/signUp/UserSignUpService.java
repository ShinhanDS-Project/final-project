package com.merge.final_project.user.signUp;

import com.merge.final_project.user.signUp.dto.UserSignUpRequestDTO;

public interface UserSignUpService {
    void register(UserSignUpRequestDTO requestDto) throws IllegalAccessException;
    public void registerLocal(UserSignUpRequestDTO dto);
    public void registerGoogle(UserSignUpRequestDTO dto);
}
