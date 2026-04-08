package com.merge.final_project.db.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity(name = "DbDonationReceipt")
@Table(name = "donation_receipt")
@Getter
@Setter
@NoArgsConstructor
public class DonationReceipt {

    @Id
    @Column(name = "donation_receipt_no")
    private String donationReceiptNo;

    @Column(name = "\"name\"")
    private String name;

    @Column(name = "field2")
    private String field2;

    @Column(name = "address")
    private String address;

    @Column(name = "address_code")
    private String addressCode;

    @Column(name = "address_detail")
    private String addressDetail;

    @Column(name = "approve_status")
    private String approveStatus;

    @Column(name = "receipt_status")
    private String receiptStatus;

    @Column(name = "already_applied")
    private String alreadyApplied;

    @Column(name = "donation_no", nullable = false)
    private String donationNo;

    @Column(name = "transaction_no", nullable = false)
    private String transactionNo;

    @Column(name = "key_no", nullable = false)
    private String keyNo;

    @Column(name = "campaign_no", nullable = false)
    private Integer campaignNo;
}
