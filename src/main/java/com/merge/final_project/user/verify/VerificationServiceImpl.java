package com.merge.final_project.user.verify;

import com.merge.final_project.user.signUp.UserSignUpRepository;
import com.merge.final_project.user.verify.dto.UserVerifyRequestDTO;
import com.merge.final_project.user.verify.dto.UserVerifyResponseDTO;
import jakarta.transaction.Transactional;
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
//      1. 만료 시간 검사
        if (verification.getExpiredAt() == null || verification.getExpiredAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("인증시간이 만료되었습니다. 다시 시도해주세요.");
        }
        // 2. 인증 시도 횟수 초과 원천 차단
        if (verification.getAttemptCount() >= 5) {
            throw new IllegalArgumentException("인증 실패 횟수(5회)를 초과하여 무효 처리되었습니다. 인증번호를 다시 발급받아 주세요.");
        }
        // 3. 인증번호 일치 여부 확인
        if (!verification.getVerificationCode().equals(code)) {
            int currentAttempt = verification.getAttemptCount() + 1;
            verification.setAttemptCount(currentAttempt);
            // 방금 실패로 인해 5회에 도달했다면 해당 인증 무효화(만료 처리)
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


    private void issueVerificationCode(String email, String subject) {
        EmailVerification verification = emailVerificationRepository.findByEmail(email)
                .orElse(null);

        String code = createVerificationCode();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiredAt = now.plusMinutes(5); // 새로운 만료 시간 (5분 후)
        if (verification != null) {
            // 만료 여부와 상관없이 너무 빈번한 요청 제한 (예: 1분 이내 재요청 금지)
            // 'updatedAt' 필드가 엔티티에 있다고 가정할 때 훨씬 깔끔함
            LocalDateTime existingExpiredAt = verification.getExpiredAt();
            int currentCount = verification.getRequestCount();

            //시간 확인 -> 만료시간 + 4분보다 크다면 요청한 지 1분이 채 안 됐다는 의미
            if (existingExpiredAt != null && existingExpiredAt.isAfter(now.plusMinutes(4))) {
                throw new IllegalStateException("인증번호는 1분마다 재요청할 수 있습니다.");
            }


            //요청 횟수 5건 이상 불가
            if (currentCount >= 5) {
                throw new IllegalStateException("인증번호 요청 횟수(5회)를 초과했습니다. 나중에 다시 시도해주세요.");
            }
            int newCount = currentCount + 1;

            // 엔티티 업데이트 (카운트도 함께 갱신)
            verification.updateVerification(code, expiredAt);
            verification.setRequestCount(newCount);
        } else {
            // 최초 요청 시
            verification = EmailVerification.builder()
                    .email(email)
                    .verificationCode(code)
                    .expiredAt(expiredAt)
                    .requestCount(1) // 최초 1회 세팅
                    .attemptCount(0) //실패 시도 0회 세팅
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