package com.merge.final_project.notification;

import org.springframework.data.jpa.repository.JpaRepository;

public interface EmailQueueRepository extends JpaRepository<EmailQueue, Long> {
}
