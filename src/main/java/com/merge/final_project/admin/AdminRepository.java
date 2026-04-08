package com.merge.final_project.admin;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdminRepository extends JpaRepository<Admin, Long> {
    Optional<Admin> findByAdminId(String adminId);

    String adminId(String adminId);
}
