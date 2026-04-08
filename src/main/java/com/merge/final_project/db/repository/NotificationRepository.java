package com.merge.final_project.db.repository;

import com.merge.final_project.db.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Integer> {
}

