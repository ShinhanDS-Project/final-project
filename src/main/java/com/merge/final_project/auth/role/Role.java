package com.merge.final_project.auth.role;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

@Entity
@Table(name = "role")
@Getter
@NoArgsConstructor
public class Role {
    @Id
    @Column(name = "role_no", nullable = false)
    private String roleNo;
    @Id
    @Column(name = "role_no", nullable = false)
    private RoleType roleType;
}
