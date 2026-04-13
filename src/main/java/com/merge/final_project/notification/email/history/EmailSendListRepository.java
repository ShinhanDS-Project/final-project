package com.merge.final_project.notification.email.history;

import com.merge.final_project.notification.email.EmailStatus;
import com.merge.final_project.notification.email.EmailTemplateType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EmailSendListRepository extends JpaRepository<EmailSendList, Long> {
    Page<EmailSendList> findByTemplateType(EmailTemplateType templateType, Pageable pageable);
    List<EmailSendList> findByEmailStatus(EmailStatus emailStatus);
}
