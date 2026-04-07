package com.merge.final_project.notification.email;

import com.merge.final_project.global.exceptions.BusinessException;
import com.merge.final_project.global.exceptions.ErrorCode;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.util.concurrent.CompletableFuture;


@Slf4j
@Service
@RequiredArgsConstructor
public class GmailServiceImpl implements GmailService {

    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;

    @Override
    public CompletableFuture<Void> sendSignupMail(String to, String foundationName, String tempPassword) {
        Context context = new Context();
        context.setVariable("foundationName", foundationName);
        context.setVariable("tempPassword", tempPassword);

        String html = templateEngine.process("mail/temp-password", context);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");
            helper.setTo(to);
            helper.setSubject("[giveNtoken] 가입 신청 승인 및 임시 비밀번호 안내");
            helper.setText(html, true);
            mailSender.send(message);
        } catch (MessagingException e) {
            log.error("메일 발송 실패 - 수신주소: {}", to);
            throw new BusinessException(ErrorCode.MAIL_SEND_FAILED);
        }

        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Void> sendRejectMail(String to, String foundationName, String rejectReason) {
        Context context = new Context();
        context.setVariable("foundationName", foundationName);
        context.setVariable("rejectReason", rejectReason);

        String html = templateEngine.process("mail/reject-mail", context);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");
            helper.setTo(to);
            helper.setSubject("[giveNtoken] 가입 신청 반려 안내");
            helper.setText(html, true);
            mailSender.send(message);
        } catch (MessagingException e) {
            log.error("메일 발송 실패 - 수신주소: {}", to);
            throw new BusinessException(ErrorCode.MAIL_SEND_FAILED);
        }

        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Void> sendInactiveMail(String to, String subject, String content) {
        return CompletableFuture.completedFuture(null);
    }
}
