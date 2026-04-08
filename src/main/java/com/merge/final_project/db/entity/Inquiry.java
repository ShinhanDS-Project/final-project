package com.merge.final_project.db.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity(name = "DbInquiry")
@Table(name = "inquiry")
@Getter
@Setter
@NoArgsConstructor
public class Inquiry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "inquiry_no")
    private Integer inquiryNo;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "title")
    private String title;

    @Column(name = "status")
    private String status;

    @Column(name = "\"content\"")
    private String content;

    @Column(name = "gno")
    private Integer gno;

    @Column(name = "ono")
    private Integer ono;

    @Column(name = "\"nested\"")
    private Integer nested;

    @Column(name = "admin_no", nullable = false)
    private Integer adminNo;

    @Column(name = "writer_type")
    private String writerType;

    @Column(name = "writer_id", nullable = false)
    private Integer writerId;

    @Column(name = "writer_name_snapshot")
    private String writerNameSnapshot;
}
