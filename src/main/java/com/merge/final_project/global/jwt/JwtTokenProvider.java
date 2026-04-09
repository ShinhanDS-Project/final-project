package com.merge.final_project.global.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {
    // 관리자용 , 소셜가입용 임시 토큰용, 로컬 로그인용 총 3개로 구성되어있습니다.
    private final JwtProperties jwtProperties;
    private SecretKey secretKey;


    @PostConstruct
    public void init(){
        this.secretKey = Keys.hmacShaKeyFor(
                jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8)
        );
    }

    //1.관리자용 로그인 토큰
    public String createAdminAccessToken(String adminId, String role){

        Date now = new Date();
        Date expiry = new Date(now.getTime() + jwtProperties.getAccessTokenExpiration());

        return Jwts.builder()
                .subject(adminId)
                .claim("role", role)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(secretKey)
                .compact();
    }

    //// 2. 소셜 가입용 임시 토큰 생성 (일반사용자 -> 구글 로그인 이메일과 이름을 담음)
    public String createTempToken(String name,String email){
        Date now = new Date();
        //10분 제한
        Date expiry = new Date(now.getTime() +600000);

        return Jwts.builder()
                .subject(email)
                .claim("name",name)
                .claim("email",email)
                .claim("type","TEMP") //임시토큰으로 구분하기 위한 타입
                .issuedAt(now)
                .expiration(expiry)
                .signWith(secretKey)
                .compact();

    }
    //3. 소셜 접근 로그인 토큰 생성(일반사용자 -구글용)
    public String createSocialAccessToken(String name,String email){
        Date now = new Date();
        //general과 동일한 로그인 토큰 시간 정책 적용
        Date expiry = new Date(now.getTime() + jwtProperties.getAccessTokenExpiration());

        return Jwts.builder()
                .subject(email)
                .claim("name",name)
                .claim("email",email)
                .claim("role","ROLE_USER") //social 로그인은 user로 강제지정
                .claim("type","ACCESS") //임시토큰으로 구분하기 위한 타입
                .issuedAt(now)
                .expiration(expiry)
                .signWith(secretKey)
                .compact();

    }

    //4. 로그인 토큰 생성(일반사용자, 수혜자, 기업용)
    public String createGeneralAccessToken(String name,String email,String role){
        Date now = new Date();
        //
        Date expiry = new Date(now.getTime() + jwtProperties.getAccessTokenExpiration());


        return Jwts.builder()
                .subject(email)
                .claim("name",name)
                .claim("email",email)
                .claim("role",role)
                .claim("type","ACCESS") //임시토큰으로 구분하기 위한 타입
                .issuedAt(now)
                .expiration(expiry) //시간 제한-> 부가기능
                .signWith(secretKey)
                .compact();

    }

    //1.유효한 토큰인지 검증.
    public boolean validateToken(String token){
        try {
            parseClaims(token);
            return true;

        } catch (Exception e) {
            return false;
        }
    }

    //2. 토큰에서 이름 추출하는 메서드 (리액트 요청시 사용)
    public String getAdminId(String token) {return parseClaims(token).getSubject();}
    public String getAdminRole(String token) {return parseClaims(token).get("role", String.class);}
     public String getNameFromToken(String token){
        return parseClaims(token).get("name", String.class);
     }
     public String getEmailFromToken(String token){
        return parseClaims(token).get("email", String.class);
     }
     public Long getReceiverNo(String token) {
        return parseClaims(token).get("receiverNo", Long.class);
     }

     //3. 토큰 종류 구분하기
    public String getTokenType(String token){return parseClaims(token).get("type", String.class);}

    //4. 토큰이 정상인지 검증하고 그 안에 json 데이터인 페이로드를 추출.
    public Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
