package com.merge.final_project.db.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity(name = "DbIllegalFoundation")
@Table(name = "illegal_foundation")
@Getter
@Setter
@NoArgsConstructor
public class IllegalFoundation {

    @Id
    @Column(name = "illegal_no")
    private Integer illegalNo;

    @Column(name = "\"name\"")
    private String name;

    @Column(name = "representative")
    private String representative;

    @Column(name = "reason")
    private String reason;
}
