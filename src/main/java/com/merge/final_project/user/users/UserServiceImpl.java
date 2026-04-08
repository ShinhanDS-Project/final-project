package com.merge.final_project.user.users;

import com.merge.final_project.global.jwt.JwtTokenProvider;
import com.merge.final_project.user.signUp.dto.UserSignUpRequestDTO;
import com.merge.final_project.user.users.dto.login.UserLoginRequestDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
@RequiredArgsConstructor
@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository; // 또는 UserMapper (MyBatis 사용 시)
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    @Override
    public String login(UserLoginRequestDTO dto) {
        //이메일로 사용자 조회
        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new RuntimeException("가입되지 않은 이메일입니다."));

        //2. 계정상태를 확인하기
        if(!user.getStatus().equals(UserStatus.ACTIVE)){
             throw new RuntimeException("로그인할 수 없는 계정입니다. 문의바랍니다");
        }

        //3.비밀번호 확인하기
        if(!passwordEncoder.matches(dto.getPassword(),user.getPasswordHash())){
            throw new RuntimeException("비밀번호가 일치하지 않습니다.");
        }
        //4. 정상 로그인을 위해 jwt 토큰 활용
        return jwtTokenProvider.createGeneralAccessToken(user.getName(),user.getEmail(),"ROLE_USER");
    }
}
