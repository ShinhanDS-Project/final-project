package com.merge.final_project.user.signUp;

import com.merge.final_project.user.signUp.dto.UserSignUpResponseDTO;
import com.merge.final_project.user.users.LoginType;
import com.merge.final_project.user.users.User;
import com.merge.final_project.user.users.UserStatus;
import com.merge.final_project.user.signUp.dto.UserSignUpRequestDTO;
import com.merge.final_project.user.verify.VerificationService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
@RequiredArgsConstructor
@Service
public class UserSignUpServiceImpl implements UserSignUpService{


    private final UserSignUpRepository userSignUpRepository;
    private final VerificationService verificationService;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    @Override
    public void register(UserSignUpRequestDTO requestDto)  {
        // 1. 공통 검증: 전화번호 중복 체크
        if (requestDto.getLoginType() == null) {
            throw new IllegalArgumentException("로그인 타입은 필수입니다.");
        }
        if (userSignUpRepository.existsByPhone(requestDto.getPhone())) {
            throw new IllegalArgumentException("이미 가입된 전화번호입니다.");
        }

        // 로그인 타입에 따른 특화 검증 로직 호출
        if (requestDto.getLoginType() == LoginType.LOCAL) {
            validateLocalUser(requestDto);
            registerLocal(requestDto);
        } else if (requestDto.getLoginType() == LoginType.GOOGLE) {
            validateGoogleUser(requestDto);
            registerGoogle(requestDto);
        } else {
            throw new IllegalArgumentException("잘못된 로그인 타입입니다.");
        }

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
    // 로컬 가입 전용 검증
    private void validateLocalUser(UserSignUpRequestDTO dto) {
        if (dto.getPassword() == null || dto.getPassword().isBlank()) {
            throw new IllegalArgumentException("비밀번호는 필수입니다.");
        }
        if (userSignUpRepository.existsByEmailAndLoginType(dto.getEmail(), LoginType.LOCAL)) {
            throw new IllegalArgumentException("이미 존재하는 로컬 계정입니다.");
        }
        if (!verificationService.isVerifiedEmail(dto.getEmail())) {
            throw new IllegalArgumentException("이메일 인증이 완료되지 않았습니다.");
        }
        if (dto.getEmail() == null || dto.getEmail().isBlank()) {
            throw new IllegalArgumentException("이메일은 필수입니다.");
        }
        if (dto.getName() == null || dto.getName().isBlank()) {
            throw new IllegalArgumentException("이름은 필수입니다.");

        }
    }

    // 구글 가입 전용 검증
    private void validateGoogleUser(UserSignUpRequestDTO dto) {
        if (dto.getEmail() == null || dto.getEmail().isBlank()) {
            throw new IllegalArgumentException("구글 계정 이메일이 없습니다.");
        }
        if (dto.getName() == null || dto.getName().isBlank()) {
            throw new IllegalArgumentException("구글 계정 이름이 없습니다.");
        }
        if (userSignUpRepository.existsByEmailAndLoginType(dto.getEmail(), LoginType.GOOGLE)) {
            throw new IllegalArgumentException("이미 가입된 구글 계정입니다.");
        }
        // 구글은 이미 인증된 이메일을 사용하므로 verificationService를 생략합니다.
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
