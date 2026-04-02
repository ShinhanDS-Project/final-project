package com.merge.final_project.user.users;


import com.merge.final_project.wallet.Wallet;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
@Entity
@Table(name = "user")
@Getter
@NoArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int userNo;

    @Column(name="login_type")
    private String loginType;

    @Column(name = "password_hash", length = 255)
    private String passwordHash;

    @Column(name="status")
    private String status; // active inactive, 정지

    @Column(name="email")
    private String email;

    @Column(name="name_hash")
    private String nameHash;

    @Column(name="name")
    private String name;

    @Column(name="phone")
    private String phone;

    @Column(name="birth")
    private LocalDate birth;

    @Column(name="profile_path")
    private String profilePath;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wallet_no", nullable = false)
    private Wallet wallet;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt; //처음에 생성시점의 시각을 createdAt과 updatedAT 둘다 갱신해둘것
}
