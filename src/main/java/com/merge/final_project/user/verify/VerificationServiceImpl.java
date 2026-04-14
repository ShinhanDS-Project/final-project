package com.merge.final_project.user.verify;

import com.merge.final_project.user.signUp.UserSignUpRepository;
import com.merge.final_project.user.verify.dto.UserVerifyRequestDTO;
import com.merge.final_project.user.verify.dto.UserVerifyResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class VerificationServiceImpl implements VerificationService {

    private final EmailVerificationRepository emailVerificationRepository;
    private final UserSignUpRepository userSignUpRepository;
    private final VerificationIssuer verificationIssuer;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    @Override
    @Transactional
    public UserVerifyResponseDTO sendVerificationCode(UserVerifyRequestDTO dto) {
        if (userSignUpRepository.existsByEmailAndLoginType(dto.getEmail(), dto.getLoginType())) {
            throw new IllegalStateException("이미 가입된 이메일입니다.");
        }

        // createCode -> createVerificationCode로 이름 매칭
        verificationIssuer.issue(dto.getEmail(), "[giveNtoken] 회원가입 인증 번호입니다.", createVerificationCode());

        return UserVerifyResponseDTO.builder()
                .success(true)
                .message("인증번호가 발송되었습니다.")
                .build();
    }

    @Override
    @Transactional
    public void sendPasswordResetCode(String email) {
        // 비밀번호 재설정 시에도 동일한 Throttling 정책 적용
        verificationIssuer.issue(email, "[giveNtoken] 비밀번호 재설정 인증 번호입니다.", createVerificationCode());
    }

    @Transactional(noRollbackFor = IllegalArgumentException.class)
    @Override
    public boolean verifyCode(String email, String code) {
        EmailVerification verification = emailVerificationRepository.findByEmailForUpdate(email)
                .orElseThrow(() -> new IllegalArgumentException("인증 요청 이력이 없는 이메일입니다."));

        if (verification.isVerified()) {
            throw new IllegalStateException("이미 인증이 완료된 이메일입니다.");
        }

        if (verification.getExpiredAt() == null || verification.getExpiredAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("인증시간이 만료되었습니다. 다시 시도해주세요.");
        }

        if (verification.getAttemptCount() >= 5) {
            throw new IllegalArgumentException("인증 실패 횟수(5회)를 초과하여 무효 처리되었습니다.");
        }

        if (!verification.getVerificationCode().equals(code)) {
            int currentAttempt = verification.getAttemptCount() + 1;
            verification.setAttemptCount(currentAttempt);

            if (currentAttempt >= 5) {
                verification.setExpiredAt(LocalDateTime.now());
                throw new IllegalArgumentException("인증번호가 5회 틀려 무효 처리되었습니다.");
            }
            throw new IllegalArgumentException("인증번호가 일치하지 않습니다. (남은 횟수: " + (5 - currentAttempt) + "회)");
        }

        verification.setVerified(true);
        return true;
    }

    @Override
    public boolean isVerifiedEmail(String email) {
        return emailVerificationRepository.findByEmail(email)
                .map(EmailVerification::isVerified)
                .orElse(false);
    }

    @Override
    public void deleteVerification(String email) {
        emailVerificationRepository.deleteByEmail(email);
    }

    private String createVerificationCode() {
        return String.format("%06d", SECURE_RANDOM.nextInt(1_000_000));
    }
}