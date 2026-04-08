package com.merge.final_project.db.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity(name = "DbAdminLog")
@Table(name = "admin_log")
@Getter
@Setter
@NoArgsConstructor
public class AdminLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_no")
    private Integer logNo;

    @Column(name = "action_type")
    private String actionType;

    @Column(name = "target_type", nullable = false)
    private String targetType;

    @Column(name = "target_no", nullable = false)
    private Integer targetNo;

    @Column(name = "description")
    private String description;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "admin_no", nullable = false)
    private Integer adminNo;
}
