package com.merge.final_project.user.verify;

import com.merge.final_project.user.signUp.UserSignUpRepository;
import com.merge.final_project.user.verify.dto.UserVerifyRequestDTO;
import com.merge.final_project.user.verify.dto.UserVerifyResponseDTO;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class VerificationServiceImpl implements VerificationService {

    private final EmailVerificationRepository emailVerificationRepository;
    private final UserSignUpRepository userSignUpRepository;
    private final JavaMailSender mailSender;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    @Override
    @Transactional
    public UserVerifyResponseDTO sendVerificationCode(UserVerifyRequestDTO dto) {
        // 1. 가입 여부 확인
        if (userSignUpRepository.existsByEmailAndLoginType(dto.getEmail(), dto.getLoginType())) {
            throw new IllegalStateException("이미 가입된 이메일입니다.");
        }

        issueVerificationCode(dto.getEmail(), "[giveNtoken] 회원가입 인증 번호입니다.");

        return UserVerifyResponseDTO.builder()
                .success(true)
                .message("인증번호가 발송되었습니다.")
                .build();
    }

    @Override
    public void sendPasswordResetCode(String email) {
        issueVerificationCode(email, "[giveNtoken] 비밀번호 재설정 인증 번호입니다.");
    }

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

        if (!verification.getVerificationCode().equals(code)) {
            throw new IllegalArgumentException("인증번호가 일치하지 않습니다.");
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

    private void issueVerificationCode(String email, String subject) {
        EmailVerification verification = emailVerificationRepository.findByEmail(email)
                .orElse(null);

        String code = createVerificationCode();
        LocalDateTime now = LocalDateTime.now();

        if (verification != null) {
            // 만료 여부와 상관없이 너무 빈번한 요청 제한 (예: 1분 이내 재요청 금지)
            // 'updatedAt' 필드가 엔티티에 있다고 가정할 때 훨씬 깔끔함
            if (verification.getExpiredAt().isAfter(now.minusMinutes(1))) {
                throw new IllegalStateException("1분 후에 다시 시도해주세요.");
            }

            if (verification.getRequestCount() >= 5) {
                // 5회 초과 시 특정 시간이 지나야 초기화되도록 로직 보완 필요
                throw new IllegalStateException("요청 횟수 초과");
            }

            verification.updateVerification(code, now.plusMinutes(5));
        } else {
            verification = EmailVerification.builder()
                    .email(email)
                    .verificationCode(code)
                    .expiredAt(now.plusMinutes(5))
                    .requestCount(1)
                    .build();
        }

        emailVerificationRepository.save(verification);
        // 트랜잭션 외부에서 실행하거나, 이벤트를 발행하여 비동기로 처리하는 것을 추천
        sendEmail(email, code, subject);
    }

    private EmailVerification updateExistingVerification(
            EmailVerification verification,
            String code,
            LocalDateTime expiredAt,
            int requestCount
    ) {
        verification.setVerificationCode(code);
        verification.setVerified(false);
        verification.setExpiredAt(expiredAt);
        verification.setRequestCount(requestCount);
        return verification;
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

    private void sendEmail(String to, String code, String subject) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText("인증 번호: " + code + "\n5분 이내에 입력해주세요.");
        mailSender.send(message);
    }

    private String createVerificationCode() {
        return String.format("%06d", SECURE_RANDOM.nextInt(1_000_000));
    }
}