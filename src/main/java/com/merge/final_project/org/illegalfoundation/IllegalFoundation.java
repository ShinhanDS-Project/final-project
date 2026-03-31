package com.merge.final_project.org.illegalfoundation;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

@Entity
@Table(name = "illegal_foundation")
@Getter
@NoArgsConstructor
public class IllegalFoundation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long illegalNo;

    @Column(name = "name")
    private String name;

    @Column(name = "representative")
    private String representative;

    @Column(name = "address")
    private String address;

    @Column(name = "reason")
    private String reason;
}
