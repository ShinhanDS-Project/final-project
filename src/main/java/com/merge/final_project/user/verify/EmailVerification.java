package com.merge.final_project.user.verify;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name="email_verification")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailVerification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long emailVerifyNo;

    @Column(name="email")
    private String email;

    @Column(name="verification_code",nullable = false, length = 20)
    private String verificationCode;

    @CreationTimestamp
    @Column(name="created_at",nullable=false)
     private LocalDateTime createdAt;

    @Column(name = "expired_at", nullable = false)
    private LocalDateTime expiredAt;

    @Column(name="verified",nullable = false)
    private boolean verified;

    @Column(name="request_count", nullable = false)
    @Builder.Default // Builder 패턴 사용 시 기본값 지정에 필요함
    private int requestCount=0;

    @Column(name="attempt_count",nullable = false)
    @Builder.Default // Builder 패턴 사용 시 기본값 지정에 필요함
    private int attemptCount=0;
    public void updateVerification(String code, LocalDateTime expiredAt) {
        this.verificationCode = code;
        this.expiredAt = expiredAt;
        this.verified = false; // 재발송 시 다시 미인증 상태로
        this.attemptCount = 0; // [필수 추가] 재발송 시 실패 횟수를 0으로 초기화
    }
}
