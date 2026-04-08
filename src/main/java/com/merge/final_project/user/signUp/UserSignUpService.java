package com.merge.final_project.user.signUp;

import com.merge.final_project.user.signUp.dto.UserSignUpRequestDTO;
import com.merge.final_project.user.signUp.dto.UserSignUpResponseDTO;

public interface UserSignUpService {
    void register(UserSignUpRequestDTO requestDto);


}
