package com.merge.final_project.user.verify;

import com.merge.final_project.user.signUp.UserSignUpRepository;
import com.merge.final_project.user.verify.dto.UserVerifyRequestDTO;
import com.merge.final_project.user.verify.dto.UserVerifyResponseDTO;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
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
    private final ApplicationEventPublisher applicationEventPublisher;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final String SIGNUP_SUBJECT = "[giveNtoken] 회원가입 인증 번호입니다.";
    private static final String RESET_SUBJECT = "[giveNtoken] 비밀번호 재설정 인증 번호입니다.";

    @Override
    @Transactional
    public UserVerifyResponseDTO sendVerificationCode(UserVerifyRequestDTO dto) {
        if (userSignUpRepository.existsByEmailAndLoginType(dto.getEmail(), dto.getLoginType())) {
            throw new IllegalStateException("이미 가입한 이메일입니다.");
        }

        applicationEventPublisher.publishEvent(
                new VerificationCodeIssueEvent(dto.getEmail(), SIGNUP_SUBJECT)
        );

        return UserVerifyResponseDTO.builder()
                .success(true)
                .message("인증번호가 발송되었습니다")
                .build();
    }

    @Override
    public void sendPasswordResetCode(String email) {
        applicationEventPublisher.publishEvent(
                new VerificationCodeIssueEvent(email, RESET_SUBJECT)
        );
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
        if (verification.getAttemptCount() >= 5) {
            throw new IllegalArgumentException("인증 실패 횟수(5회)를 초과하여 무효 처리되었습니다. 인증번호를 다시 발급받아 주세요.");
        }
        if (!verification.getVerificationCode().equals(code)) {
            int currentAttempt = verification.getAttemptCount() + 1;
            verification.setAttemptCount(currentAttempt);

            if (currentAttempt >= 5) {
                verification.setExpiredAt(LocalDateTime.now());
                throw new IllegalArgumentException("인증번호가 5회 틀려 해당 인증번호는 무효 처리되었습니다. 인증번호를 다시 발급받아 주세요.");
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

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    protected void issueVerificationCode(VerificationCodeIssueEvent event) {
        String email = event.email();
        String subject = event.subject();

        EmailVerification verification = emailVerificationRepository.findByEmail(email)
                .orElse(null);

        String code = createVerificationCode();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiredAt = now.plusMinutes(5);

        if (verification != null) {
            LocalDateTime existingExpiredAt = verification.getExpiredAt();
            int currentCount = verification.getRequestCount();

            if (existingExpiredAt != null && existingExpiredAt.isAfter(now.plusMinutes(4))) {
                throw new IllegalStateException("인증번호는 1분마다 재요청할 수 있습니다.");
            }

            if (currentCount >= 5) {
                throw new IllegalStateException("인증번호 요청 횟수(5회)를 초과했습니다. 잠시 후 다시 시도해주세요.");
            }

            verification.updateVerification(code, expiredAt);
            verification.setRequestCount(currentCount + 1);
        } else {
            verification = EmailVerification.builder()
                    .email(email)
                    .verificationCode(code)
                    .expiredAt(expiredAt)
                    .requestCount(1)
                    .attemptCount(0)
                    .build();
        }

        emailVerificationRepository.save(verification);
        sendEmail(email, code, subject);
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
