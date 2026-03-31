package com.merge.final_project.auth.role;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

@Entity
@Table(name = "role")
@Getter
@NoArgsConstructor
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long roleNo;
    @Id
    @Column(name = "role_no", nullable = false)
    private RoleType roleType;
}
