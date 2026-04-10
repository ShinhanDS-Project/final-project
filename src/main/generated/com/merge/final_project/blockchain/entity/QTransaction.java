package com.merge.final_project.blockchain.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QTransaction is a Querydsl query type for Transaction
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QTransaction extends EntityPathBase<Transaction> {

    private static final long serialVersionUID = 982441740L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QTransaction transaction = new QTransaction("transaction");

    public final NumberPath<Long> amount = createNumber("amount", Long.class);

    public final NumberPath<Long> blockNum = createNumber("blockNum", Long.class);

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final EnumPath<TransactionEventType> eventType = createEnum("eventType", TransactionEventType.class);

    public final com.merge.final_project.wallet.entity.QWallet fromWallet;

    public final NumberPath<java.math.BigDecimal> gasFee = createNumber("gasFee", java.math.BigDecimal.class);

    public final DateTimePath<java.time.LocalDateTime> sentAt = createDateTime("sentAt", java.time.LocalDateTime.class);

    public final EnumPath<TransactionStatus> status = createEnum("status", TransactionStatus.class);

    public final com.merge.final_project.wallet.entity.QWallet toWallet;

    public final StringPath transactionCode = createString("transactionCode");

    public final NumberPath<Long> transactionNo = createNumber("transactionNo", Long.class);

    public final StringPath txHash = createString("txHash");

    public QTransaction(String variable) {
        this(Transaction.class, forVariable(variable), INITS);
    }

    public QTransaction(Path<? extends Transaction> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QTransaction(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QTransaction(PathMetadata metadata, PathInits inits) {
        this(Transaction.class, metadata, inits);
    }

    public QTransaction(Class<? extends Transaction> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.fromWallet = inits.isInitialized("fromWallet") ? new com.merge.final_project.wallet.entity.QWallet(forProperty("fromWallet"), inits.get("fromWallet")) : null;
        this.toWallet = inits.isInitialized("toWallet") ? new com.merge.final_project.wallet.entity.QWallet(forProperty("toWallet"), inits.get("toWallet")) : null;
    }

}

