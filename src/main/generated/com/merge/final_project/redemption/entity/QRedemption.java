package com.merge.final_project.redemption.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QRedemption is a Querydsl query type for Redemption
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QRedemption extends EntityPathBase<Redemption> {

    private static final long serialVersionUID = 1380646996L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QRedemption redemption = new QRedemption("redemption");

    public final NumberPath<Long> amount = createNumber("amount", Long.class);

    public final NumberPath<Long> blockNumber = createNumber("blockNumber", Long.class);

    public final DateTimePath<java.time.LocalDateTime> cashPaidAt = createDateTime("cashPaidAt", java.time.LocalDateTime.class);

    public final StringPath failureReason = createString("failureReason");

    public final DateTimePath<java.time.LocalDateTime> processedAt = createDateTime("processedAt", java.time.LocalDateTime.class);

    public final NumberPath<Long> redemptionNo = createNumber("redemptionNo", Long.class);

    public final DateTimePath<java.time.LocalDateTime> requestedAt = createDateTime("requestedAt", java.time.LocalDateTime.class);

    public final NumberPath<Long> requesterNo = createNumber("requesterNo", Long.class);

    public final EnumPath<com.merge.final_project.redemption.RequesterType> requesterType = createEnum("requesterType", com.merge.final_project.redemption.RequesterType.class);

    public final EnumPath<com.merge.final_project.redemption.RedemptionStatus> status = createEnum("status", com.merge.final_project.redemption.RedemptionStatus.class);

    public final com.merge.final_project.blockchain.entity.QTransaction transaction;

    public final NumberPath<Long> version = createNumber("version", Long.class);

    public final com.merge.final_project.wallet.entity.QWallet wallet;

    public QRedemption(String variable) {
        this(Redemption.class, forVariable(variable), INITS);
    }

    public QRedemption(Path<? extends Redemption> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QRedemption(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QRedemption(PathMetadata metadata, PathInits inits) {
        this(Redemption.class, metadata, inits);
    }

    public QRedemption(Class<? extends Redemption> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.transaction = inits.isInitialized("transaction") ? new com.merge.final_project.blockchain.entity.QTransaction(forProperty("transaction"), inits.get("transaction")) : null;
        this.wallet = inits.isInitialized("wallet") ? new com.merge.final_project.wallet.entity.QWallet(forProperty("wallet"), inits.get("wallet")) : null;
    }

}

