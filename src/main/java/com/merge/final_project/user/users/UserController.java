package com.merge.final_project.user.users;

import com.merge.final_project.user.users.dto.support.EmailRequestDTO;
import com.merge.final_project.user.users.dto.support.EmailResponseDTO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users/support")
public class UserController {
    //부가기능 또는 마이페이지 전용 컨트롤러
    @Autowired
    private UserService userService;

    @PostMapping("/email")
    public ResponseEntity<EmailResponseDTO> email(@Valid @RequestBody EmailRequestDTO request) {
        EmailResponseDTO response=userService.findEmail(request.getPhone(),request.getName());
        return ResponseEntity.ok(response);
    }
}
