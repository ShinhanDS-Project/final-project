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
    private Integer emailVerificationNo;

    @Column(name="email")
    private String email;

    @CreationTimestamp
    @Column(name="verification_code",nullable = false, length = 20)
    private String verificationCode;

    @Column(name = "expired_at", nullable = false)
    private LocalDateTime expiredAt;

    @Column(name="verified",nullable = false)
    private boolean verified;

    // EmailVerification 엔티티 내부에 추가
    public void updateVerification(String code, LocalDateTime expiredAt) {
        this.verificationCode = code;
        this.expiredAt = expiredAt;
        this.verified = false; // 재발송 시 다시 미인증 상태로
    }
}
