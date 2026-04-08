package com.merge.final_project.blockchain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "key")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Key {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "key_no")
    private Long keyNo;

    @Column(name = "private_key", nullable = false)
    private String privateKey;

    @Column(name = "aes_key")
    private String aesKey;

}