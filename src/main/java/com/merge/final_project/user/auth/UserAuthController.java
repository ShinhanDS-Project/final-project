package com.merge.final_project.user.auth;

import com.merge.final_project.global.jwt.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class UserAuthController {
    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @GetMapping("/social-info")
    public ResponseEntity<?> socialInfo(@RequestHeader("Authorization") String bearerToken ) {
        String token=bearerToken.replace("Bearer ","");

        //토큰이 유효한 토큰인지 확인
        if(!jwtTokenProvider.validateToken(token)){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        //토큰에서 구글이 준 정보를 꺼내는것
        Map<String,String> responser=new HashMap<>();
        responser.put("email", jwtTokenProvider.getEmailFromToken(token));
        responser.put("name", jwtTokenProvider.getNameFromToken(token));

        return ResponseEntity.ok(responser);
    }
}
