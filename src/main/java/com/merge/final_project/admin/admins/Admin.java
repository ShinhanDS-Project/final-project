package com.merge.final_project.admin.admins;

import com.merge.final_project.global.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "admin")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Admin extends BaseEntity {

    @Id
    @Column(name = "admin_no")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long adminNo;

    private String adminId;
    private String password;
    private String name;
    private String adminRole;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
