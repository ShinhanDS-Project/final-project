package com.merge.final_project.org.foundationapplication;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "foundation_applications")
@Getter
@NoArgsConstructor
public class FoundationApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long applicationsNo;

    @Column(name = "proposer_email")
    private String proposerEmail;

    @Column(name = "proposer_name")
    private String proposerName;

    @Enumerated(EnumType.STRING)
    @Column(name = "foundation_type")
    private FoundationType foundationType;

    @Column(name = "foundation_name")
    private String foundationName;

    @Column(name = "representative_name")
    private String representativeName;

    @Column(name = "foundation_description")
    private String foundationDescription;

    @Column(name = "foundation_page")
    private String foundationPage;

    @Column(name = "image")
    private String image;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "business_registration_number")
    private String businessRegistrationNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "approve_status")
    private ApplicationStatus approveStatus;

    @Column(name = "rejected_at")
    private LocalDateTime rejectedAt;

    @Column(name = "commission_rate")
    private BigDecimal commissionRate;

    @Column(name = "account")
    private String account;
}
