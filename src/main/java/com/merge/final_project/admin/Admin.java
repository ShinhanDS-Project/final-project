package com.merge.final_project.admin;

import com.merge.final_project.global.BaseUpdatedAtEntity;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "admin")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Admin extends BaseUpdatedAtEntity implements UserDetails {

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

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(adminRole));
    }

    @Override
    public String getUsername() {
        return adminId;
    }
}
