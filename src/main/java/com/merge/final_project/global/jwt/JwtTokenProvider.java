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

    private final JwtProperties jwtProperties;
    private SecretKey secretKey;

    @PostConstruct
    public void init(){
        this.secretKey = Keys.hmacShaKeyFor(
                jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8)
        );
    }

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

    //토큰이 정상인지 검증하고 그 안에 json 데이터인 페이로드를 추출.
    public Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String getAdminId(String token) {
        return parseClaims(token).getSubject();
    }

    public String getAdminRole(String token) {
        return parseClaims(token).get("role", String.class);
    }


    //유효한 토큰인지 검증.
    public boolean validateToken(String token){
        try {
            parseClaims(token);
            return true;

        } catch (Exception e) {
            return false;
        }
    }

    //리프레시 토큰 생성


    //// 1. 소셜 가입용 임시 토큰 생성 (이메일과 이름을 담음)
    public String createToken(String name,String email){
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


    //2. 토큰에서 이름 추출하는 메서드 (리액트 요청시 사용)
     public String getNameFromToken(String token){
        return parseClaims(token).get("name", String.class);
     }
     public String getEmailFromToken(String token){
        return parseClaims(token).get("email", String.class);
     }
     //3. 토큰 종류 구분하기
    public String getTokenType(String token){return parseClaims(token).get("type", String.class);}
}
