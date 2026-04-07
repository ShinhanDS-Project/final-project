package com.merge.final_project.user.auth.oauth;

import com.merge.final_project.global.jwt.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class UserAuthController {
    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    //용도 : 소셜 회원 가입 직전 or 직후에 토큰 안에 들어있는 구글 사용자 정보를 프론트가
    //다시 꺼내기 위한 api
    @GetMapping("/social-info")
    public ResponseEntity<?> socialInfo(@RequestHeader("Authorization") String bearerToken ) {
        //1. 프론트가 헤더로 토큰을 꺼내고, bearer 부분 때고 jwt만 꺼냄
        //bearer( 토큰 가진 사람이 접근 권한이 있다고 보고 서버가 받아들이는 인증 토큰)
        String token=bearerToken.replace("Bearer ","");

        //토큰이 유효한 토큰인지 확인- 토큰 만료/ 서명/이상한 형식이면 401
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
