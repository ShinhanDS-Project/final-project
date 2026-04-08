package com.merge.final_project.db.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity(name = "DbImage")
@Table(name = "image")
@Getter
@Setter
@NoArgsConstructor
public class Image {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "img_no")
    private Integer imgNo;

    @Column(name = "img_path")
    private String imgPath;

    @Column(name = "img_org_name", nullable = false)
    private String imgOrgName;

    @Column(name = "img_stored_name", nullable = false)
    private String imgStoredName;

    @Column(name = "target_name", nullable = false)
    private String targetName;

    @Column(name = "target_no", nullable = false)
    private Integer targetNo;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "purpose")
    private String purpose;
}
