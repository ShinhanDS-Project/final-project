package com.merge.final_project.user.signUp;

import com.merge.final_project.global.jwt.JwtTokenProvider;
import com.merge.final_project.user.signUp.dto.UserSignUpRequestDTO;
import com.merge.final_project.user.signUp.dto.UserSignUpResponseDTO;
import com.merge.final_project.user.users.LoginType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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

    @Operation(summary = "로컬 회원가입", description = "이메일·비밀번호로 일반 사용자 계정을 생성합니다. multipart/form-data: dto 파트(JSON)와 profileImage(선택)를 함께 전송합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "회원가입 성공"),
            @ApiResponse(responseCode = "400", description = "요청 값 유효성 오류"),
            @ApiResponse(responseCode = "409", description = "이미 등록된 이메일")
    })
    @PostMapping(value="/local", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> register(@Valid @RequestPart("dto") UserSignUpRequestDTO dto, @RequestPart(value="profileImage",required = false) MultipartFile profileImage) throws IOException {
        dto.setLoginType(LoginType.LOCAL);
        userSignUpService.register(dto,profileImage);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
    @Operation(summary = "닉네임 중복 확인", description = "닉네임 중복 여부를 확인합니다. true=이미 존재, false=사용 가능.")
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "확인 성공") })
    @GetMapping("/nickname")
    public boolean existNickName(String nameHash){
        //중복여부 체크 api
        return userSignUpService.findNickName(nameHash);
    }

    @Operation(summary = "소셜(Google) 회원가입", description = "Google 로그인 후 발급된 TEMP 토큰을 Authorization 헤더에 담아 소셜 계정을 생성합니다. multipart/form-data: dto 파트(JSON)와 profileImage(선택)를 함께 전송합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "회원가입 성공"),
            @ApiResponse(responseCode = "400", description = "요청 값 유효성 오류"),
            @ApiResponse(responseCode = "401", description = "유효하지 않은 TEMP 토큰")
    })
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
