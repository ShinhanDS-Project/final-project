package com.merge.final_project.user.signUp;

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
        if(requestDto.getLoginType()==null){
            throw new IllegalArgumentException("잘못된 접근입니다.");
        }
        if(userSignUpRepository.existsByPhone(requestDto.getPhone())){
            throw new IllegalArgumentException("이미 존재하는 회원 정보입니다.");
        }
        if (requestDto.getLoginType() == LoginType.LOCAL &&
                (requestDto.getPassword() == null || requestDto.getPassword().isBlank())) {
            throw new IllegalArgumentException("비밀번호는 필수입니다.");
        }
        if (requestDto.getLoginType() == LoginType.LOCAL) {
            //부가기능 추가 : 이메일 인증
            if (userSignUpRepository.existsByEmailAndLoginType(requestDto.getEmail(), LoginType.LOCAL)) {
                throw new IllegalArgumentException("이미 존재하는 이메일입니다..");
            }
            if (!verificationService.isVerifiedEmail(requestDto.getEmail())) {
                throw new IllegalArgumentException("이메일 인증이 완료되지 않았습니다.");
            }
            registerLocal(requestDto);
            return;
        }
        if (requestDto.getLoginType() == LoginType.GOOGLE) {
            //부가기능 추가 : 이메일 인증
            if (userSignUpRepository.existsByEmailAndLoginType(requestDto.getEmail(), LoginType.GOOGLE)) {
                throw new IllegalArgumentException("이미 존재하는 이메일입니다..");
            }
            registerGoogle(requestDto);
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
