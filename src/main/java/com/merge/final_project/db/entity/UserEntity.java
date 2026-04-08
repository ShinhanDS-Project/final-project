package com.merge.final_project.db.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity(name = "DbUser")
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_no")
    private Integer userNo;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "password_hash")
    private String passwordHash;

    @Column(name = "name_hash")
    private String nameHash;

    @Column(name = "\"name\"", nullable = false)
    private String name;

    @Column(name = "phone")
    private String phone;

    @Column(name = "birth", nullable = false)
    private LocalDate birth;

    @Column(name = "profile_path")
    private String profilePath;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "wallet_no")
    private Integer walletNo;

    @Column(name = "privatekey_no")
    private Integer privatekeyNo;

    @Column(name = "login_type", nullable = false)
    private String loginType;

    @Column(name = "login_count")
    private Integer loginCount;
}
