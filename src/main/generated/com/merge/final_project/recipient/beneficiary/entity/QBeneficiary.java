package com.merge.final_project.recipient.beneficiary.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QBeneficiary is a Querydsl query type for Beneficiary
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QBeneficiary extends EntityPathBase<Beneficiary> {

    private static final long serialVersionUID = -1476445191L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QBeneficiary beneficiary = new QBeneficiary("beneficiary");

    public final StringPath account = createString("account");

    public final StringPath beneficiaryHash = createString("beneficiaryHash");

    public final NumberPath<Long> beneficiaryNo = createNumber("beneficiaryNo", Long.class);

    public final EnumPath<com.merge.final_project.recipient.beneficiary.BeneficiaryType> beneficiaryType = createEnum("beneficiaryType", com.merge.final_project.recipient.beneficiary.BeneficiaryType.class);

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final StringPath email = createString("email");

    public final StringPath entryCode = createString("entryCode");

    public final NumberPath<Long> key_no = createNumber("key_no", Long.class);

    public final StringPath name = createString("name");

    public final StringPath password = createString("password");

    public final StringPath phone = createString("phone");

    public final DateTimePath<java.time.LocalDateTime> updatedAt = createDateTime("updatedAt", java.time.LocalDateTime.class);

    public final com.merge.final_project.wallet.entity.QWallet wallet;

    public QBeneficiary(String variable) {
        this(Beneficiary.class, forVariable(variable), INITS);
    }

    public QBeneficiary(Path<? extends Beneficiary> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QBeneficiary(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QBeneficiary(PathMetadata metadata, PathInits inits) {
        this(Beneficiary.class, metadata, inits);
    }

    public QBeneficiary(Class<? extends Beneficiary> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.wallet = inits.isInitialized("wallet") ? new com.merge.final_project.wallet.entity.QWallet(forProperty("wallet"), inits.get("wallet")) : null;
    }

}

