package com.merge.final_project.wallet.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QWallet is a Querydsl query type for Wallet
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QWallet extends EntityPathBase<Wallet> {

    private static final long serialVersionUID = 1201703024L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QWallet wallet = new QWallet("wallet");

    public final NumberPath<java.math.BigDecimal> balance = createNumber("balance", java.math.BigDecimal.class);

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final com.merge.final_project.blockchain.entity.QKey key;

    public final DateTimePath<java.time.LocalDateTime> lastUsedAt = createDateTime("lastUsedAt", java.time.LocalDateTime.class);

    public final NumberPath<Long> ownerNo = createNumber("ownerNo", Long.class);

    public final EnumPath<WalletStatus> status = createEnum("status", WalletStatus.class);

    public final StringPath walletAddress = createString("walletAddress");

    public final StringPath walletHash = createString("walletHash");

    public final NumberPath<Long> walletNo = createNumber("walletNo", Long.class);

    public final EnumPath<WalletType> walletType = createEnum("walletType", WalletType.class);

    public QWallet(String variable) {
        this(Wallet.class, forVariable(variable), INITS);
    }

    public QWallet(Path<? extends Wallet> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QWallet(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QWallet(PathMetadata metadata, PathInits inits) {
        this(Wallet.class, metadata, inits);
    }

    public QWallet(Class<? extends Wallet> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.key = inits.isInitialized("key") ? new com.merge.final_project.blockchain.entity.QKey(forProperty("key")) : null;
    }

}

