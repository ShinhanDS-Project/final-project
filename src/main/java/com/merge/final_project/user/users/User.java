package com.merge.final_project.user.users;


import com.merge.final_project.wallet.Wallet;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
@Entity
@Table(name = "user", uniqueConstraints = {
        @UniqueConstraint(name = "uk_user_email_login_type", columnNames = {"email", "login_type"}),
        @UniqueConstraint(name = "uk_user_phone", columnNames = {"phone"})
})
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="user_no")
    private Integer userNo;


    @Column(name="password_hash")
    private String passwordHash;


    @Column(name="email",nullable = false)
    private String email;

    @Column(name="name_hash",unique = true, nullable = false)
    private String nameHash;

    @Column(name="name",nullable = false)
    private String name;

    @Column(name="phone",unique = true)
    private String phone;

    @Column(name="birth",nullable = false)
    private LocalDate birth;

    @Column(name="profile_path")
    private String profilePath;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wallet_no") //null처리 가능
    private Wallet wallet;

    @Enumerated(EnumType.STRING)
    @Column(name="login_type",nullable = false)
    private LoginType loginType;

    @Enumerated(EnumType.STRING)
    @Column(name="status",nullable = false)
    private UserStatus status;

    @CreationTimestamp
    @Column(name="created_at",nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name="updated_at",nullable = false)
    private LocalDateTime updatedAt;//처음에 생성시점의 시각을 createdAt과 updatedAT 둘다 갱신해둘것

    @Column(name="login_count")
    private Integer loginCount;
}
