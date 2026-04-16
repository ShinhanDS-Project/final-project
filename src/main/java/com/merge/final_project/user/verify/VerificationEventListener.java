package com.merge.final_project.user.verify;

import com.merge.final_project.global.exceptions.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class VerificationEventListener {
    private final JavaMailSender mailSender;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleVerificationEvent(VerificationEvent event) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(event.email());
        message.setSubject(event.subject());
        message.setText("인증 번호: " + event.code() + "\n5분 이내에 입력해주세요.");
        try {
            mailSender.send(message);
        } catch (MailException ex) {
            // 1. 에러 코드와 함께 커스텀 로깅 (여전히 로그는 필요하지만 훨씬 체계적임)
            ErrorCode error = ErrorCode.MAIL_SEND_FAILED;

            // 규격화된 포맷으로 출력
            System.err.printf("[%s] %s - 대상: %s%n", error.getCode(), error.getMessage(), event.email());
                           }
    }
}
