package com.merge.final_project.global;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Builder
@Table(name = "image")
public class Image {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "img_no")
    private Long imgNo;

    @Column(name = "img_path", nullable = false)
    private String imgPath;

    @Column(name = "img_org_name", nullable = false)
    private String imgOrgName;

    @Column(name = "img_stored_name", nullable = false)
    private String imgStoredName;

    @Column(name = "target_name", nullable = false)
    private String targetName; // 💡 "final_report" 등이 들어갈 자리

    @Column(name = "target_no", nullable = false)
    private Long targetNo; // 💡 보고서 번호(no)가 들어갈 자리

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "purpose",nullable = true)
    private String purpose;
}