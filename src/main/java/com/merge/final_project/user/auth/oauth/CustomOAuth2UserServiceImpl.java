package com.merge.final_project.user.auth.oauth;

import com.merge.final_project.user.signUp.UserSignUpRepository;
import com.merge.final_project.user.users.LoginType;
import com.merge.final_project.user.users.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserServiceImpl extends DefaultOAuth2UserService implements CustomOAuth2UserService {
    //DefaultOAuth2UserService를 상속받기
    //loadUser 메서드를 오버라이드 해 email,name을 추출하기


    private final UserSignUpRepository userSignUpRepository;
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
       return super.loadUser(userRequest);
    }

    //로깅 기능 추가?
}
