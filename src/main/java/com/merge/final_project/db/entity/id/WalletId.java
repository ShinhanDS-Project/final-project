package com.merge.final_project.db.entity.id;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
public class WalletId implements Serializable {

    @Column(name = "wallet_no")
    private Long walletNo;

    @Column(name = "key_no")
    private Long keyNo;
}
