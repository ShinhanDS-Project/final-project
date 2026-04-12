package com.merge.final_project.user.users;

import com.merge.final_project.global.exceptions.BusinessException;
import com.merge.final_project.global.exceptions.ErrorCode;
import com.merge.final_project.global.jwt.JwtTokenProvider;
import com.merge.final_project.global.utils.MaskingUtils;
import com.merge.final_project.global.utils.S3FileService;
import com.merge.final_project.user.users.dto.support.*;
import com.merge.final_project.user.verify.VerificationService;
import com.merge.final_project.user.users.dto.login.UserLoginRequestDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RequiredArgsConstructor
@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository; // 또는 UserMapper (MyBatis 사용 시)
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final VerificationService verificationService;
    private final S3FileService fileService;
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

    // 1단계: 비밀번호 재설정 요청
    @Override
    public void requestPasswordReset(ChangePasswordRequestDTO dto) {
        User user = userRepository.findByEmailAndName(dto.getEmail(), dto.getName())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        verificationService.sendPasswordResetCode(user.getEmail());
    }

    // 2단계: 인증코드 확인
    @Override
    public void confirmPasswordCode(String email, String code) {
        User user = userRepository.findByEmailAndLoginType(email, LoginType.LOCAL)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        verificationService.verifyCode(user.getEmail(), code);
    }

    // 로그인 후 마이페이지 비밀번호 변경
    @Override
    @Transactional
    public void editPassword(Long userNo, EditPasswordDTO dto) {
        User user = userRepository.findById(userNo)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (!passwordEncoder.matches(dto.getCurrentPassword(), user.getPasswordHash())) {
            throw new RuntimeException("현재 비밀번호가 일치하지 않습니다.");
        }

        if (dto.getNewPassword() == null || dto.getNewPassword().isBlank()) {
            throw new IllegalArgumentException("새 비밀번호는 필수야.");
        }
        if (!passwordEncoder.matches(dto.getCurrentPassword(), dto.getNewPassword())) {
            throw new IllegalArgumentException("현재 비밀번호가 일치하지 않아.");
        }
    }

    // 3단계: 로그인 전 최종 비밀번호 재설정

    @Transactional
    public void resetPassword(ChangeResetPasswordRequestDTO dto) {
        User user = userRepository.findByEmailAndLoginType(dto.getEmail(), LoginType.LOCAL)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (!verificationService.isVerifiedEmail(dto.getEmail())) {
            throw new RuntimeException("이메일 인증이 완료되지 않았습니다.");
        }

        if (!dto.getNewPassword().equals(dto.getNewPassword2())) {
            throw new RuntimeException("새 비밀번호와 비밀번호 확인이 일치하지 않습니다.");
        }

        if (passwordEncoder.matches(dto.getNewPassword(), user.getPasswordHash())) {
            throw new RuntimeException("기존 비밀번호와 동일한 비밀번호는 사용할 수 없습니다.");
        }
        user.setsLoginCount(0);
        verificationService.deleteVerification(dto.getEmail());
    }

    @Override
    public MyInfoResponseDTO getMyInfo(Long userNo) {
        User user = userRepository.findById(userNo)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        return MyInfoResponseDTO.builder()
                .email(user.getEmail())
                .name(user.getName())
                .phone(user.getPhone())
                .nameHash(user.getNameHash())
                .profilePath(user.getProfilePath())
                .build();

    }

    @Transactional
    @Override
    public void updateMyInfo(Long userNo, UpdateMyInfoRequestDTO dto) {
        User user = userRepository.findById(userNo)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 닉네임 수정
        if(dto.getNameHash()!=null &&!dto.getNameHash().isBlank()){
            if (!dto.getNameHash().equals(user.getNameHash())
                    && userRepository.existsByNameHash(dto.getNameHash())) {
                throw new BusinessException(ErrorCode.DUPLICATE_NICKNAME);
            }
            user.setNameHash(dto.getNameHash());
        }
        // 프로필 사진 수정
        MultipartFile file = dto.getProfileImage();
        if (file != null && !file.isEmpty()) {
            try {
                if (user.getProfilePath() != null && !user.getProfilePath().isBlank()) {
                    fileService.deleteFile(user.getProfilePath());
                }

                String storedName = fileService.saveFile(file);
                user.setProfilePath(storedName);
            } catch (Exception e) {
                throw new RuntimeException("프로필 이미지 업로드에 실패했습니다.", e);
            }
        }
    }

    @Override
    public List<MyDonationResponseDTO> getMyDonations(Long userNo) {
        return List.of();
    }


}

