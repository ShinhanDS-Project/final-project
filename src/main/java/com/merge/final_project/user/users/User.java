package com.merge.final_project.user.users;



import com.merge.final_project.wallet.entity.Wallet;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
@Entity
@Table(name = "users",uniqueConstraints = {
        @UniqueConstraint(name = "uk_user_email_login_type", columnNames = {"email", "login_type"})
}) //로그인 타입,별로 이메일 중복 허용ㅇ
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="user_no")
    private Long userNo;

    // 중복 제거: String 타입은 지우고 Enum 타입만 남깁니다.
    @Enumerated(EnumType.STRING)
    @Column(name="login_type", nullable = false)
    private LoginType loginType;

    @Column(name = "password_hash", length = 255)
    private String passwordHash;

    // 중복 제거: String 타입은 지우고 Enum 타입만 남깁니다.
    @Enumerated(EnumType.STRING)
    @Column(name="status", nullable = false)
    private UserStatus status; // ACTIVE, INACTIVE, 정지 등

    @Column(name="email", nullable = false)
    private String email;

    @Column(name="name_hash", unique = true, nullable = false)
    private String nameHash;

    @Column(name="name", nullable = false)
    private String name;

    @Column(name="phone", unique = true)
    private String phone;

    @Column(name="birth", nullable = false)
    private LocalDate birth;

    @Column(name="profile_path")
    private String profilePath;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wallet_no") // null 가능
    private Wallet wallet;

    @CreationTimestamp
    @Column(name="created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name="updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name="login_count")
    private Integer loginCount;

    //namehash 바꿔야함
    public void updateNameHash(String nameHash) {
        this.nameHash = nameHash;
    }
}
