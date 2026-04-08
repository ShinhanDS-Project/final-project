package com.merge.final_project.db.repository;

import com.merge.final_project.db.entity.Admin;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminRepository extends JpaRepository<Admin, Integer> {
}

