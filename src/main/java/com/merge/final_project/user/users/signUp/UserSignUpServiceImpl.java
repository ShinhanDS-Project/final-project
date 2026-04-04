package com.merge.final_project.user.users.signUp;

import com.merge.final_project.user.users.LoginType;
import com.merge.final_project.user.users.User;
import com.merge.final_project.user.users.UserStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

import static com.merge.final_project.user.users.signUp.UserHashGenerater.generateUserHash;
@RequiredArgsConstructor
@Service
public class UserSignUpServiceImpl implements UserSignUpService{


    private final UserSignUpRepository userSignUpRepository;

    private final PasswordEncoder passwordEncoder;

    @Override
    public void register(UserSignUpRequestDTO requestDto) throws IllegalAccessException {
        if(requestDto.getLoginType()==null){
            throw new IllegalAccessException("잘못된 접근입니다.");
        }
        if(userSignUpRepository.existsByPhone(requestDto.getPhone())){
            throw new IllegalArgumentException("이미 존재하는 회원 정보입니다.");
        }
        if (requestDto.getLoginType() == LoginType.LOCAL) {
            if (userSignUpRepository.existsByEmailAndLoginType(requestDto.getEmail(), LoginType.LOCAL)) {
                throw new IllegalArgumentException("이미 존재하는 이메일입니다..");
            }
            registerLocal(requestDto);
            return;
        }
        throw new IllegalArgumentException("잘못된 접근입니다.");
    }

    @Override
    public void registerLocal(UserSignUpRequestDTO dto) {
        User user = User.builder()
                .email(dto.getEmail())
                .passwordHash(passwordEncoder.encode(dto.getPassword()))
                .name(dto.getName())
                .phone(dto.getPhone())
                .birth(dto.getBirth())
                .nameHash(generateUniqueHash()) // 중복 체크가 포함된 메서드 호출
                .profilePath(dto.getProfilePath()) // DTO에 있는 경로 반영
                .status(UserStatus.ACTIVE)
                .loginType(LoginType.LOCAL)
                .loginCount(0)
                .build();

        userSignUpRepository.save(user);
    }

    @Override
    public void registerGoogle(UserSignUpRequestDTO dto) {
        User user = User.builder()
                .email(dto.getEmail())
                .passwordHash(null)
                .name(dto.getName())
                .phone(dto.getPhone())
                .birth(dto.getBirth())
                .nameHash(generateUniqueHash()) // 중복 체크가 포함된 메서드 호출
                .profilePath(dto.getProfilePath())
                .status(UserStatus.ACTIVE)
                .loginType(LoginType.GOOGLE)
                .loginCount(0)
                .build();

        userSignUpRepository.save(user);
    }
    // 중복되지 않는 해시가 나올 때까지 반복 생성
    private String generateUniqueHash() {
        String hash;
        do {
            hash = UserHashGenerater.generateUserHash();
        } while (userSignUpRepository.existsByNameHash(hash));
        return hash;
    }

}
