package com.merge.final_project.user.signUp;

import com.merge.final_project.global.jwt.JwtTokenProvider;
import com.merge.final_project.user.signUp.dto.UserSignUpRequestDTO;
import com.merge.final_project.user.signUp.dto.UserSignUpResponseDTO;
import com.merge.final_project.user.users.LoginType;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;

@io.swagger.v3.oas.annotations.tags.Tag(name = "일반 사용자 회원가입", description = "로컬·소셜(Google) 회원가입 API")
@RestController
@RequestMapping("/api/signup")
public class UserSignUpController {
    @Autowired
    UserSignUpService userSignUpService;
    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @PostMapping(value="/local", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> register(@Valid @RequestPart("dto") UserSignUpRequestDTO dto, @RequestPart(value="profileImage",required = false) MultipartFile profileImage) throws IOException {
        dto.setLoginType(LoginType.LOCAL);
        userSignUpService.register(dto,profileImage);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
    @GetMapping("/nickname")
    public boolean existNickName(String nameHash){
        //중복여부 체크 api
        return userSignUpService.findNickName(nameHash);
    }

    @PostMapping(value="/google", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity <Void> registerGoogle(@RequestHeader("Authorization") String bearerToken, @Valid @RequestPart("dto") UserSignUpRequestDTO dto,@RequestPart(value = "profileImage", required = false) MultipartFile profileImage) throws IOException {
        //보안 처리
        if (bearerToken == null || !bearerToken.startsWith("Bearer ")) {
            // bearer로 시작하지 않는 경우 예외처리
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String token=bearerToken.substring(7);
        if(!jwtTokenProvider.validateToken(token)){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        //임시토큰이아니라 기존에 있는 정보가 접근하는 경우
        if (!"TEMP".equals(jwtTokenProvider.getTokenType(token))) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        dto.setEmail(jwtTokenProvider.getEmailFromToken(token));
        dto.setName(jwtTokenProvider.getNameFromToken(token));
        dto.setLoginType(LoginType.GOOGLE);

        userSignUpService.register(dto,profileImage);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
