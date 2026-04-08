package com.merge.final_project.db.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity(name = "DbAdmin")
@Table(name = "\"admin\"")
@Getter
@Setter
@NoArgsConstructor
public class Admin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "admin_no")
    private Integer adminNo;

    @Column(name = "admin_id", nullable = false)
    private String adminId;

    @Column(name = "\"password\"", nullable = false)
    private String password;

    @Column(name = "\"name\"")
    private String name;

    @Column(name = "admin_role")
    private String adminRole;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
