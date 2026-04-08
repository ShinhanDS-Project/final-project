package com.merge.final_project.db.repository;

import com.merge.final_project.db.entity.EmailSendList;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmailSendListRepository extends JpaRepository<EmailSendList, Integer> {
}

