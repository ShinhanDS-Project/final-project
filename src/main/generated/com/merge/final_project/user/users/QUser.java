package com.merge.final_project.user.users;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QUser is a Querydsl query type for User
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QUser extends EntityPathBase<User> {

    private static final long serialVersionUID = -2135941037L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QUser user = new QUser("user");

    public final DatePath<java.time.LocalDate> birth = createDate("birth", java.time.LocalDate.class);

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final StringPath email = createString("email");

    public final NumberPath<Integer> loginCount = createNumber("loginCount", Integer.class);

    public final EnumPath<LoginType> loginType = createEnum("loginType", LoginType.class);

    public final StringPath name = createString("name");

    public final StringPath nameHash = createString("nameHash");

    public final StringPath passwordHash = createString("passwordHash");

    public final StringPath phone = createString("phone");

    public final StringPath profilePath = createString("profilePath");

    public final EnumPath<UserStatus> status = createEnum("status", UserStatus.class);

    public final DateTimePath<java.time.LocalDateTime> updatedAt = createDateTime("updatedAt", java.time.LocalDateTime.class);

    public final NumberPath<Long> userNo = createNumber("userNo", Long.class);

    public final com.merge.final_project.wallet.entity.QWallet wallet;

    public QUser(String variable) {
        this(User.class, forVariable(variable), INITS);
    }

    public QUser(Path<? extends User> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QUser(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QUser(PathMetadata metadata, PathInits inits) {
        this(User.class, metadata, inits);
    }

    public QUser(Class<? extends User> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.wallet = inits.isInitialized("wallet") ? new com.merge.final_project.wallet.entity.QWallet(forProperty("wallet"), inits.get("wallet")) : null;
    }

}

