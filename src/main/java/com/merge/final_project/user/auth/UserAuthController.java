package com.merge.final_project.user.auth;

import com.merge.final_project.global.jwt.JwtTokenProvider;
import com.merge.final_project.user.signUp.dto.UserSignUpRequestDTO;
import com.merge.final_project.user.users.UserService;
import com.merge.final_project.user.users.dto.login.UserLoginRequestDTO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class UserAuthController {
    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private UserService userService;


    //1.용도 : 소셜 회원 가입 직전 or 직후에 토큰 안에 들어있는 구글 사용자 정보를 프론트가 다시 꺼내기 위한 api
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

    //2. 용도 : 로그인 컨트롤러
    @PostMapping("/login/user")
    public ResponseEntity<?> userLogin(@Valid @RequestBody UserLoginRequestDTO dto){
        //Service단에서 해당 테이블에 아이디 비밀번호, 현재 계정상태(UserStatus)까지 확인하
        //jwt 토큰이 포함된 응답 dto로 반환
        String access= userService.login(dto);

        //2. jwt 방식에서는 json 응답에 토큰을 실어 보냄
        //프론트에서는 이 응답후에 직접 페이지에 이동한다
        Map<String,String> responser=new HashMap<>();
        responser.put("accessToken",access);
        responser.put("message","로그인이 성공하였습니다");

        return ResponseEntity.ok(responser);
    }

}
