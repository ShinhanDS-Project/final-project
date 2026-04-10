package com.merge.final_project.campaign.settlement;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QSettlement is a Querydsl query type for Settlement
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QSettlement extends EntityPathBase<Settlement> {

    private static final long serialVersionUID = -2014874447L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QSettlement settlement = new QSettlement("settlement");

    public final NumberPath<Long> amount = createNumber("amount", Long.class);

    public final com.merge.final_project.recipient.beneficiary.entity.QBeneficiary beneficiary;

    public final NumberPath<Long> beneficiaryAmount = createNumber("beneficiaryAmount", Long.class);

    public final com.merge.final_project.campaign.campaigns.entity.QCampaign campaign;

    public final com.merge.final_project.org.QFoundation foundation;

    public final NumberPath<Long> foundationAmount = createNumber("foundationAmount", Long.class);

    public final DateTimePath<java.time.LocalDateTime> settledAt = createDateTime("settledAt", java.time.LocalDateTime.class);

    public final NumberPath<Long> settlementNo = createNumber("settlementNo", Long.class);

    public final EnumPath<SettlementStatus> status = createEnum("status", SettlementStatus.class);

    public final StringPath transactionCode = createString("transactionCode");

    public QSettlement(String variable) {
        this(Settlement.class, forVariable(variable), INITS);
    }

    public QSettlement(Path<? extends Settlement> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QSettlement(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QSettlement(PathMetadata metadata, PathInits inits) {
        this(Settlement.class, metadata, inits);
    }

    public QSettlement(Class<? extends Settlement> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.beneficiary = inits.isInitialized("beneficiary") ? new com.merge.final_project.recipient.beneficiary.entity.QBeneficiary(forProperty("beneficiary"), inits.get("beneficiary")) : null;
        this.campaign = inits.isInitialized("campaign") ? new com.merge.final_project.campaign.campaigns.entity.QCampaign(forProperty("campaign"), inits.get("campaign")) : null;
        this.foundation = inits.isInitialized("foundation") ? new com.merge.final_project.org.QFoundation(forProperty("foundation"), inits.get("foundation")) : null;
    }

}

