package com.merge.final_project.user.verify;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class VerificationServiceImpl implements VerificationService {
    @Autowired
    EmailVerificationRepository emailVerificationRepository;
    @Autowired
    JavaMailSender mailSender;

    @Transactional
    @Override
    public void sendVerificationCode(String email) {

        //코드 생성
        String code=createVerificationCode();
        LocalDateTime expiredAt=LocalDateTime.now().plusMinutes(5);

        EmailVerification emailVerification=emailVerificationRepository.findByEmail(email)
                .map(existing->{
                    existing.setVerificationCode(code);
                    existing.setVerified(false);
                    existing.setExpiredAt(expiredAt);
                    return existing;
                })
                .orElse(
                        EmailVerification.builder()
                        .email(email)
                        .verified(false)
                        .verificationCode(code)
                        .expiredAt(expiredAt)
                        .build()
                );

        emailVerificationRepository.save(emailVerification);
        //여기서 이메일 발송 코드 짜기 (부가기능 2)

        sendEmail(email,code);
        System.out.println("인증코드:"+code);

    }

    @Override
    @Transactional
    public void verifyCode(String email, String code) {
        //코드 확인-> expired 일자 확인, 또는 일치하는지 확인할 것
        EmailVerification emailVerification= emailVerificationRepository.findByEmail(email)
                .orElseThrow(()->new IllegalArgumentException("인증 불가한 이메일입니다"));

        //1. expired 일자가 현재 시간보다 이전인 경우
        if(emailVerification.getExpiredAt().isBefore(LocalDateTime.now())){
            throw new IllegalArgumentException("인증시간이 만료된 이메일입니다. 다시 시도해주세요.");
        }
        //2. 일치하는 이메일인지 확인
        if(!emailVerification.getVerificationCode().equals(code)){
            throw new IllegalArgumentException("일치하지 않습니다. 다시 시도해주세요");
        }

        emailVerification.setVerified(true);


    }

    @Override
    @Transactional
    public boolean isVerifiedEmail(String email) {
        //가입전 확인-> 실제로 맞췄는지 확인
        return emailVerificationRepository.findByEmail(email)
                .map(EmailVerification::isVerified)
                .orElse(false);
    }

    @Override
    public void deleteVerification(String email) {
        //배치코드? 또는 삭제
        emailVerificationRepository.deleteByEmail(email);
    }
    private void sendEmail(String to, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("[BlueHeart] 회원가입 인증 번호입니다.");
        message.setText("인증 번호: " + code + "\n5분 이내에 입력해주세요.");
        mailSender.send(message);
    }

    private String createVerificationCode() {
        return String.format("%06d", (int) (Math.random() * 1000000));
    }
}
