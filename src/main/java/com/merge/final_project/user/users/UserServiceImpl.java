package com.merge.final_project.user.users;

import com.merge.final_project.global.exceptions.BusinessException;
import com.merge.final_project.global.jwt.JwtTokenProvider;
import com.merge.final_project.global.utils.MaskingUtils;
import com.merge.final_project.user.users.dto.support.EmailResponseDTO;
import com.merge.final_project.user.users.dto.login.UserLoginRequestDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository; // 또는 UserMapper (MyBatis 사용 시)
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    @Transactional
    public String login(UserLoginRequestDTO dto) {
        //이메일로 사용자 조회
        User user = userRepository.findByEmailAndLoginType(dto.getEmail(),LoginType.LOCAL)
                .orElseThrow(() -> new RuntimeException("가입되지 않은 이메일입니다."));

        //2. 계정상태를 확인하기
        if (!user.getStatus().equals(UserStatus.ACTIVE)) {
            throw new RuntimeException("로그인할 수 없는 계정입니다. 문의바랍니다");
        }
        //3. (부가기능) login_count>=5이면 로그인 불가(비밀번호 재설정 인데, 일단 막아두기 --> 부가기능)
        if (user.getLoginCount() >= 5) {
            throw new RuntimeException("해당 계정의 로그인 횟수가 초과되었습니다. 문의바랍니다.");
        }
        //4.비밀번호 확인하기
        if (!passwordEncoder.matches(dto.getPassword(), user.getPasswordHash())) {
            user.setsLoginCount(user.getLoginCount()+1);
            throw new RuntimeException("비밀번호가 일치하지 않습니다.");
        }
        //5. 정상 로그인을 위해 jwt 토큰 활용
        user.setsLoginCount(0);
        return jwtTokenProvider.createGeneralAccessToken(user.getName(), user.getEmail(), "ROLE_USER", user.getUserNo());
    }

    @Override
    public EmailResponseDTO findEmail(String phone, String name) {
        //1. 핸드폰과 번호로 현재 존재하지 않는다면 존재하지 않다고 띄우기
        User user = userRepository.findByPhoneAndName(phone, name)
                .orElseThrow(() -> new RuntimeException("가입정보가 없습니다. 가입해주세요"));
        //2. 존재한다면 이메일과 login Type 반환

        return new EmailResponseDTO(MaskingUtils.maskEmail(user.getEmail()),user.getLoginType());
    }
}

