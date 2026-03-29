package com.merge.final_project.admin.adminlog;

import com.merge.final_project.global.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "admin_log")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminLog extends BaseEntity {

    @Id
    @Column(name = "log_no")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long logNo;

    @Enumerated(EnumType.STRING)
    @Column(name = "action_type")
    private ActionType actionType;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type")
    private TargetType targetType;

    @Column(name = "target_no")
    private Long targetNo;

    private String description;

    @Column(name = "admin_no")
    private Long adminNo;
}
