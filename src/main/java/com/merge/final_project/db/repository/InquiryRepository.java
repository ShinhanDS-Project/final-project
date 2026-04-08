package com.merge.final_project.db.repository;

import com.merge.final_project.db.entity.Inquiry;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InquiryRepository extends JpaRepository<Inquiry, Integer> {
}

