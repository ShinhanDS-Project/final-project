package com.merge.final_project.org;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QFoundation is a Querydsl query type for Foundation
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QFoundation extends EntityPathBase<Foundation> {

    private static final long serialVersionUID = -501548034L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QFoundation foundation = new QFoundation("foundation");

    public final com.merge.final_project.global.QBaseEntity _super = new com.merge.final_project.global.QBaseEntity(this);

    public final StringPath account = createString("account");

    public final EnumPath<AccountStatus> accountStatus = createEnum("accountStatus", AccountStatus.class);

    public final StringPath bankName = createString("bankName");

    public final StringPath businessRegistrationNumber = createString("businessRegistrationNumber");

    public final StringPath campaignWallet1 = createString("campaignWallet1");

    public final StringPath campaignWallet2 = createString("campaignWallet2");

    public final StringPath campaignWallet3 = createString("campaignWallet3");

    public final StringPath contactPhone = createString("contactPhone");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final StringPath description = createString("description");

    public final NumberPath<java.math.BigDecimal> feeRate = createNumber("feeRate", java.math.BigDecimal.class);

    public final StringPath foundationEmail = createString("foundationEmail");

    public final StringPath foundationHash = createString("foundationHash");

    public final StringPath foundationName = createString("foundationName");

    public final NumberPath<Long> foundationNo = createNumber("foundationNo", Long.class);

    public final StringPath foundationPassword = createString("foundationPassword");

    public final EnumPath<FoundationType> foundationType = createEnum("foundationType", FoundationType.class);

    public final StringPath profilePath = createString("profilePath");

    public final StringPath rejectReason = createString("rejectReason");

    public final StringPath representativeName = createString("representativeName");

    public final EnumPath<ReviewStatus> reviewStatus = createEnum("reviewStatus", ReviewStatus.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final com.merge.final_project.wallet.entity.QWallet wallet;

    public QFoundation(String variable) {
        this(Foundation.class, forVariable(variable), INITS);
    }

    public QFoundation(Path<? extends Foundation> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QFoundation(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QFoundation(PathMetadata metadata, PathInits inits) {
        this(Foundation.class, metadata, inits);
    }

    public QFoundation(Class<? extends Foundation> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.wallet = inits.isInitialized("wallet") ? new com.merge.final_project.wallet.entity.QWallet(forProperty("wallet"), inits.get("wallet")) : null;
    }

}

