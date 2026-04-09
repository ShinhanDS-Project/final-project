package com.merge.final_project.org.illegalfoundation;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "illegal_foundation")
@Getter
@NoArgsConstructor
public class IllegalFoundation {

    @Id
    @Column(name="illegal_no")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long illegalNo;

    @Column(name = "name")
    private String name;

    @Column(name = "representative")
    private String representative;

    @Column(name = "reason")
    private String reason;
}
