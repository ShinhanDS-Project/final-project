package com.merge.final_project.admin.admins;

import com.merge.final_project.admin.adminlog.AdminLog;
import com.merge.final_project.global.BaseUpdatedAtEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "admin")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Admin extends BaseUpdatedAtEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "admin_no")
    private Long adminNo;

    @Column(name = "admin_id", nullable = false)
    private String adminId;
    private String password;
    private String name;

    @Column(name = "admin_role")
    private String adminRole;

}
