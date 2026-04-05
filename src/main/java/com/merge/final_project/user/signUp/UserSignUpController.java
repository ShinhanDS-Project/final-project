package com.merge.final_project.user.signUp;

import com.merge.final_project.user.signUp.dto.UserSignUpRequestDTO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api/signup")
public class UserSignUpController {
    @Autowired
    UserSignUpService userSignUpService;
    @PostMapping("/users")
    public void register(@Valid @RequestBody UserSignUpRequestDTO dto){
        return ResponseEntity.status(HttpStatus.CREATED).body(userSignUpService.register(dto));
    }
}
