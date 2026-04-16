package com.merge.final_project.user.auth;

import com.merge.final_project.global.jwt.JwtTokenProvider;
import com.merge.final_project.user.signUp.dto.UserSignUpRequestDTO;
import com.merge.final_project.user.users.UserService;
import com.merge.final_project.user.users.dto.login.UserLoginRequestDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@io.swagger.v3.oas.annotations.tags.Tag(name = "일반 사용자 인증", description = "일반 사용자 로그인·로그아웃·소셜 정보 조회 API")
@RestController
@RequestMapping("/api/auth")
public class UserAuthController {
    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    @Value("${cookie.secure}")
    private boolean secureCookie;
    @Autowired
    private UserService userService;


    @Operation(summary = "소셜 임시 토큰 정보 조회", description = "소셜 회원가입 직전/직후 TEMP 토큰에서 구글 사용자 정보(이름·이메일)를 추출합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공 — 이름·이메일 반환"),
            @ApiResponse(responseCode = "401", description = "유효하지 않은 TEMP 토큰")
    })
    @GetMapping("/social-info")
    public ResponseEntity<?> socialInfo(@RequestHeader("Authorization") String bearerToken ) {
        //1. 프론트가 헤더로 토큰을 꺼내고, bearer 부분 때고 jwt만 꺼냄
        //bearer( 토큰 가진 사람이 접근 권한이 있다고 보고 서버가 받아들이는 인증 토큰)
        if(bearerToken == null ||!bearerToken.startsWith("Bearer ")) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        String token=bearerToken.replace("Bearer ","");
        if (token.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("토큰이 비어 있습니다.");
        }
        //토큰이 유효한 토큰인지 확인- 토큰 만료/ 서명/이상한 형식이면 401
        if(!jwtTokenProvider.validateToken(token)){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        //temp토큰인지 확인하기
        String tokenType = jwtTokenProvider.getTokenType(token);
        if(!"TEMP".equals(tokenType)){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("temp토큰이 아닙니다.");
        }
        //토큰에서 구글이 준 정보를 꺼내는것
        Map<String,String> responser=new HashMap<>();
        responser.put("email", jwtTokenProvider.getEmailFromToken(token));
        responser.put("name", jwtTokenProvider.getNameFromToken(token));

        return ResponseEntity.ok(responser);
    }

    @Operation(summary = "일반 사용자 로그인", description = "이메일·비밀번호로 로그인합니다. JWT 액세스 토큰을 반환합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그인 성공 — JWT 토큰 반환"),
            @ApiResponse(responseCode = "401", description = "이메일 또는 비밀번호 불일치")
    })
    @PostMapping("/login/user/local")
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

    @Operation(summary = "일반 사용자 로그아웃 (로컬)", description = "로컬 로그인 사용자 로그아웃. 프론트엔드에서 localStorage 토큰 제거를 처리합니다.")
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "로그아웃 성공") })
    @PostMapping("/logout/user/local")
    public ResponseEntity<?> logoutLocal(){
        //프론트엔드에서 localStorage.removeItem('accessToken');
        // 프론트엔드에서 loginType을 확인하는 방법을 사용해야한다.

        Map<String,String> responser=new HashMap<>();
        responser.put("message"," 로컬로그아웃 되었습니다.");
        return ResponseEntity.ok(responser);
    }

    @Operation(summary = "소셜 사용자 로그아웃", description = "소셜(Google) 로그인 사용자 로그아웃. 만료된 쿠키를 응답 헤더에 설정하여 브라우저 쿠키를 삭제합니다.")
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "로그아웃 성공") })
    @PostMapping("/logout/user/social")
    public ResponseEntity<?> logoutSocial(HttpServletResponse response){
        //구글 로그인은 "만료시간이 과거 0초인 빈 쿠키를 응답 헤더 (Set-Cookie)에 실어서 브라우저에 쏜다
        //프론트엔드는 기존 토큰 쿠키를 덮어쓰고 삭제한다.
        // 이렇게 분리한 이유는 handler에서 jwt 토큰을 파라미터에 노출하면 보안상 문제가 생기게 되어
        //소셜과 일반사용자가 동일한 방식을 사용할 수 없다. => 이러한 한계를 해결하기 위해 컨트롤러를 분리함 (시간적 한계)
        ResponseCookie deleteCookie= ResponseCookie.from("accessToken","")
                .httpOnly(true)
                .secure(secureCookie)
                .path("/")
                .sameSite("None")
                .maxAge(0)
                .build();
        //HttpServletResponse -> 서버가 브라우저에게 보내는 응답객체
        // HttpServletRequest: 브라우저가 서버에게 보낸 요청


        Map<String,String> body=new HashMap<>();
        body.put("message","소셜 로그아웃되었습니다.");

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, deleteCookie.toString())
                .body(body);

    }
}
