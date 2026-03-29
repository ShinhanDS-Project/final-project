package com.merge.final_project.inquiry;

import com.merge.final_project.global.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "inquiry")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Inquiry extends BaseEntity {

    @Id
    @Column(name = "inquiry_no")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long inquiryNo;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    private String title;

    @Enumerated(EnumType.STRING)
    private InquiryStatus status;

    private String content;
    private String gno;
    private String ono;
    private String nested;

    @Column(name = "admin_no")
    private Long adminNo;

    @Column(name = "login_no")
    private Long loginNo;
}
