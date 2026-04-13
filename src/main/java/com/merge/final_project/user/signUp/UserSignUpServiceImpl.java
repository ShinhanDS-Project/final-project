package com.merge.final_project.user.signUp;

import com.merge.final_project.auth.useraccount.SignupWalletHookService;
import com.merge.final_project.global.exceptions.BusinessException;
import com.merge.final_project.global.exceptions.ErrorCode;
import com.merge.final_project.global.utils.FileUtil;
import com.merge.final_project.global.utils.S3FileService;
import com.merge.final_project.user.signUp.dto.UserSignUpResponseDTO;
import com.merge.final_project.user.users.LoginType;
import com.merge.final_project.user.users.User;
import com.merge.final_project.user.users.UserStatus;
import com.merge.final_project.user.signUp.dto.UserSignUpRequestDTO;
import com.merge.final_project.user.verify.VerificationService;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RequiredArgsConstructor
@Service
@Transactional(rollbackFor = Exception.class)
public class UserSignUpServiceImpl implements UserSignUpService{


    private final UserSignUpRepository userSignUpRepository;
    private final VerificationService verificationService;
    private final PasswordEncoder passwordEncoder;
    private final S3FileService s3FileService;
    private final SignupWalletHookService signupWalletHookService;
    private final FileUtil fileUtil;

    @Transactional
    @Override
    public void register(UserSignUpRequestDTO requestDto, MultipartFile file) throws IOException {
        // 1. 공통 검증: 전화번호 중복 체크
        if (requestDto.getLoginType() == null) {
            throw new IllegalArgumentException("로그인 타입은 필수입니다.");
        }
        if (userSignUpRepository.existsByPhone(requestDto.getPhone())) {
            throw new IllegalArgumentException("이미 가입된 전화번호입니다.");
        }
        //닉네임 중복 여부
        if (userSignUpRepository.existsByNameHash((requestDto.getNameHash()))){
            throw new BusinessException(ErrorCode.DUPLICATE_NICKNAME);
        }
        // 로그인 타입에 따른 특화 검증 로직 호출
        if (requestDto.getLoginType() == LoginType.LOCAL) {
            validateLocalUser(requestDto);
        } else if (requestDto.getLoginType() == LoginType.GOOGLE) {
            validateGoogleUser(requestDto);
        } else {
            throw new IllegalArgumentException("잘못된 로그인 타입입니다.");
        }
// 2. 사진 저장 (파일이 넘어왔을 때만 실행)
        String savedPath = null;
        if (file != null && !file.isEmpty()) {
            // 위 1번의 검증 로직 포함...
            String storedName = s3FileService.saveFile(file);
            savedPath = storedName; // rollback 시 delete용 key
            requestDto.setProfilePath(s3FileService.getFilePath(storedName)); // DB 저장용 URL
        }

        try {
            // 실제 가입 로직
            if (requestDto.getLoginType() == LoginType.LOCAL) {
                registerLocal(requestDto);
            } else {
                registerGoogle(requestDto);
            }
        } catch (Exception e) {
            // DB 저장 실패 시 저장했던 파일 삭제
            if (savedPath != null) {
                try {
                    s3FileService.deleteFile(savedPath);
                } catch (Exception deleteEx) {
                   e.addSuppressed(deleteEx);
                                }
            }
            throw e; // 예외를 다시 던져서 GlobalExceptionHandler가 처리하게 함
        }
    }

    @Override
    public boolean findNickName(String nickName) {
        //닉네임 중복 여부
        if (userSignUpRepository.existsByNameHash(nickName)){
            throw new BusinessException(ErrorCode.DUPLICATE_NICKNAME);

        }
        return true;
    }


    private void registerLocal(UserSignUpRequestDTO dto) {
        User user = User.builder()
                .email(dto.getEmail())
                .passwordHash(passwordEncoder.encode(dto.getPassword()))
                .name(dto.getName())
                .phone(dto.getPhone())
                .birth(dto.getBirth())
//                .nameHash(generateUniqueHash()) // 중복 체크가 포함된 메서드 호출
                .nameHash(dto.getNameHash())//닉네임으로 변경
                .profilePath(dto.getProfilePath()) // DTO에 있는 경로 반영
                .status(UserStatus.ACTIVE)
                .loginType(LoginType.LOCAL)
                .loginCount(0)
                .build();
        userSignUpRepository.save(user);

        // 회원 저장이 끝난 직후 지갑 생성 훅을 호출해 users.wallet_no까지 연결한다.
        signupWalletHookService.onUserSignupCompleted(user.getUserNo());
    }




    private void registerGoogle(UserSignUpRequestDTO dto) {
        User user = User.builder()
                .email(dto.getEmail())
                .passwordHash(null)
                .name(dto.getName())
                .phone(dto.getPhone())
                .birth(dto.getBirth())
                .nameHash(dto.getNameHash()) //닉네임 용도로 변경
                .profilePath(dto.getProfilePath())
                .status(UserStatus.ACTIVE)
                .loginType(LoginType.GOOGLE)
                .loginCount(0)
                .build();

        userSignUpRepository.save(user);

        // 소셜 가입 사용자도 동일한 지갑 생성 흐름을 사용한다.
        signupWalletHookService.onUserSignupCompleted(user.getUserNo());
    }
    // 로컬 가입 전용 검증
    private void validateLocalUser(UserSignUpRequestDTO dto) {
        if (dto.getEmail() == null || dto.getEmail().isBlank()) {
            throw new IllegalArgumentException("이메일은 필수입니다.");
        }
        if (dto.getName() == null || dto.getName().isBlank()) {
            throw new IllegalArgumentException("이름은 필수입니다.");

        }
        if (dto.getPassword() == null || dto.getPassword().isBlank()) {
            throw new IllegalArgumentException("비밀번호는 필수입니다.");
        }
        if (userSignUpRepository.existsByEmailAndLoginType(dto.getEmail(), LoginType.LOCAL)) {
            throw new IllegalArgumentException("이미 존재하는 로컬 계정입니다.");
        }
        if (!verificationService.isVerifiedEmail(dto.getEmail())) {
            throw new IllegalArgumentException("이메일 인증이 완료되지 않았습니다.");
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
        // 구글은 이미 인증된 이메일을 사용하므로 verificationService를 생략.
    }

}
