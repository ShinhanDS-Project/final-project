package com.merge.final_project.db.repository;

import com.merge.final_project.db.entity.Image;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ImageRepository extends JpaRepository<Image, Integer> {
}

