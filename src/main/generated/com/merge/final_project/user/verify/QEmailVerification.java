package com.merge.final_project.user.verify;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QEmailVerification is a Querydsl query type for EmailVerification
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QEmailVerification extends EntityPathBase<EmailVerification> {

    private static final long serialVersionUID = -2091876796L;

    public static final QEmailVerification emailVerification = new QEmailVerification("emailVerification");

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final StringPath email = createString("email");

    public final NumberPath<Long> emailVerifyNo = createNumber("emailVerifyNo", Long.class);

    public final DateTimePath<java.time.LocalDateTime> expiredAt = createDateTime("expiredAt", java.time.LocalDateTime.class);

    public final NumberPath<Integer> requestCount = createNumber("requestCount", Integer.class);

    public final StringPath verificationCode = createString("verificationCode");

    public final BooleanPath verified = createBoolean("verified");

    public QEmailVerification(String variable) {
        super(EmailVerification.class, forVariable(variable));
    }

    public QEmailVerification(Path<? extends EmailVerification> path) {
        super(path.getType(), path.getMetadata());
    }

    public QEmailVerification(PathMetadata metadata) {
        super(EmailVerification.class, metadata);
    }

}

