package com.merge.final_project.db.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity(name = "DbKey")
@Table(name = "\"key\"")
@Getter
@Setter
@NoArgsConstructor
public class KeyEntity {

    @Column(name = "private_key", nullable = false)
    private String privateKey;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "key_no")
    private Long keyNo;

    @Column(name = "aes_key")
    private String aesKey;
}
