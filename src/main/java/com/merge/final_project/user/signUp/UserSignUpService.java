package com.merge.final_project.user.signUp;

import com.merge.final_project.user.signUp.dto.UserSignUpRequestDTO;
import com.merge.final_project.user.signUp.dto.UserSignUpResponseDTO;
import org.springframework.web.multipart.MultipartFile;

public interface UserSignUpService {
    void register(UserSignUpRequestDTO requestDto, MultipartFile file);


}
