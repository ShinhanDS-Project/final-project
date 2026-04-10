package com.merge.final_project.donation.donations;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QDonation is a Querydsl query type for Donation
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QDonation extends EntityPathBase<Donation> {

    private static final long serialVersionUID = -1905850502L;

    public static final QDonation donation = new QDonation("donation");

    public final NumberPath<Long> campaignNo = createNumber("campaignNo", Long.class);

    public final DateTimePath<java.time.LocalDateTime> donatedAt = createDateTime("donatedAt", java.time.LocalDateTime.class);

    public final NumberPath<Long> donationNo = createNumber("donationNo", Long.class);

    public final StringPath isAnonymous = createString("isAnonymous");

    public final NumberPath<Long> paymentNo = createNumber("paymentNo", Long.class);

    public final NumberPath<Long> userNo = createNumber("userNo", Long.class);

    public final NumberPath<Long> walletNo = createNumber("walletNo", Long.class);

    public QDonation(String variable) {
        super(Donation.class, forVariable(variable));
    }

    public QDonation(Path<? extends Donation> path) {
        super(path.getType(), path.getMetadata());
    }

    public QDonation(PathMetadata metadata) {
        super(Donation.class, metadata);
    }

}

