package com.merge.final_project.user.verify;

import com.merge.final_project.user.signUp.UserSignUpRepository;
import com.merge.final_project.user.verify.dto.UserVerifyRequestDTO;
import com.merge.final_project.user.verify.dto.UserVerifyResponseDTO;
import org.jspecify.annotations.NonNull;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class VerificationServiceImpl implements VerificationService {

    private final EmailVerificationRepository emailVerificationRepository;
    private final UserSignUpRepository userSignUpRepository;
    private final JavaMailSender mailSender;
    private final VerificationIssuer verificationIssuer;
    private static final SecureRandom random = new SecureRandom();

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    @Override
    public UserVerifyResponseDTO sendVerificationCode(@NonNull UserVerifyRequestDTO dto) {
        // 1. 가입 여부 확인
        if (userSignUpRepository.existsByEmailAndLoginType(dto.getEmail(), dto.getLoginType())) {
            throw new IllegalStateException("이미 가입된 이메일입니다.");
        }
        // 2. 1분 제한 등 검증 로직 호출 후 발급 클래스에 위임
        checkRequestPolicy(dto.getEmail());
        // 3. 외부 빈 호출 (중요: REQUIRES_NEW가 적용된 프록시 호출)
        verificationIssuer.issue(dto.getEmail(), "[giveNtoken] 회원가입 인증 번호입니다.", createVerificationCode());

        return UserVerifyResponseDTO.builder()
                .success(true)
                .message("인증번호가 발송되었습니다.")
                .build();
    }

    @Override
    public void sendPasswordResetCode(String email) {
        verificationIssuer.issue(email, "[giveNtoken] 비밀번호 재설정 인증 번호입니다.", createVerificationCode());
    }

    // noRollbackFor를 통해 실패 횟수(UPDATE)가 롤백되지 않고 저장되게 함
    @Transactional(noRollbackFor = IllegalArgumentException.class)
    @Override
    public boolean verifyCode(String email, String code) {
        EmailVerification verification = emailVerificationRepository.findByEmail(email)
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
                // 여기서 예외가 터져도 noRollbackFor 덕분에 attemptCount와 expiredAt은 DB에 저장됨
                throw new IllegalArgumentException("인증번호가 5회 틀려 무효 처리되었습니다.");
            }
            throw new IllegalArgumentException("인증번호가 일치하지 않습니다. (남은 횟수: " + (5 - currentAttempt) + "회)");
        }

        verification.setVerified(true);
        return true;
    }
    //1분 이내 재요청 금지 등 정책 체크 로직을 별도 메서드로 분리
    private void checkRequestPolicy(String email) {
        emailVerificationRepository.findByEmail(email).ifPresent(v -> {
            if (v.getExpiredAt() != null && v.getExpiredAt().isAfter(LocalDateTime.now().plusMinutes(4))) {
                throw new IllegalStateException("인증번호는 1분마다 재요청할 수 있습니다.");
            }
            if (v.getRequestCount() >= 5) {
                throw new IllegalStateException("인증번호 요청 횟수(5회)를 초과했습니다.");
            }
        });
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



    private EmailVerification createNewVerification(
            String email,
            String code,
            LocalDateTime expiredAt,
            int requestCount
    ) {
        return EmailVerification.builder()
                .email(email)
                .verificationCode(code)
                .verified(false)
                .expiredAt(expiredAt)
                .requestCount(requestCount)
                .build();
    }


    private String createVerificationCode() {
        return String.format("%06d", SECURE_RANDOM.nextInt(1_000_000));
    }
}