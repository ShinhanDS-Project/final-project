package com.merge.final_project.user.signUp;

import com.merge.final_project.user.signUp.dto.UserSignUpRequestDTO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/signup")
public class UserSignUpController {
    @Autowired
    UserSignUpService userSignUpService;

    @PostMapping("/users")
    public ResponseEntity<Void> register(@Valid @RequestBody UserSignUpRequestDTO dto) {
        userSignUpService.register(dto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
