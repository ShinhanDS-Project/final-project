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
    public UserVerifyResponseDTO sendVerificationCode(UserVerifyRequestDTO dto) {
        String email = dto.getEmail();

        if (userSignUpRepository.existsByEmailAndLoginType(email, dto.getLoginType())) {
            throw new IllegalStateException("이미 가입된 이메일입니다.");
        }

        issueVerificationCode(email, "[giveNtoken] 회원가입 인증 번호입니다.");

        return UserVerifyResponseDTO.builder()
                .success(true)
                .available(true)
                .message("인증번호가 발송되었습니다. 이메일을 확인해주세요.")
                .build();
    }

    @Override
    public void sendPasswordResetCode(String email) {
        issueVerificationCode(email, "[giveNtoken] 비밀번호 재설정 인증 번호입니다.");
    }

    @Override
    public void verifyCode(String email, String code) {
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

        LocalDateTime now = LocalDateTime.now();
        int currentCount = 0;

        if (verification != null) {
            if (verification.getExpiredAt() != null && verification.getExpiredAt().isBefore(now)) {
                currentCount = 0;
            } else {
                currentCount = verification.getRequestCount();

                if (verification.getExpiredAt() != null && verification.getExpiredAt().isAfter(now.plusMinutes(4))) {
                    throw new IllegalStateException("인증번호는 1분마다 재요청할 수 있습니다.");
                }

                if (currentCount >= 5) {
                    throw new IllegalStateException("인증번호 요청 횟수(5회)를 초과했습니다. 잠시 후 다시 시도해주세요.");
                }
            }
        }

        String code = createVerificationCode();
        LocalDateTime expiredAt = now.plusMinutes(5);
        int newCount = currentCount + 1;

        EmailVerification savedVerification = (verification != null)
                ? updateExistingVerification(verification, code, expiredAt, newCount)
                : createNewVerification(email, code, expiredAt, newCount);

        emailVerificationRepository.save(savedVerification);
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