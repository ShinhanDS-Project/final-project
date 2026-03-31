package com.merge.final_project.org.illegalfoundation;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

@Entity
@Table(name = "illegal_foundation")
@Getter
@NoArgsConstructor
public class IllegalFoundation {

    @Id
    @Column(name = "illegal_no", nullable = false)
    private String illegalNo;

    @Column(name = "name")
    private String name;

    @Column(name = "representative")
    private String representative;

    @Column(name = "address")
    private String address;

    @Column(name = "reason")
    private String reason;
}
