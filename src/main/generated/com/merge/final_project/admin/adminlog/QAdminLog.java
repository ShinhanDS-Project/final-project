package com.merge.final_project.admin.adminlog;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QAdminLog is a Querydsl query type for AdminLog
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QAdminLog extends EntityPathBase<AdminLog> {

    private static final long serialVersionUID = 376328238L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QAdminLog adminLog = new QAdminLog("adminLog");

    public final com.merge.final_project.global.QBaseCreatedAtEntity _super = new com.merge.final_project.global.QBaseCreatedAtEntity(this);

    public final EnumPath<ActionType> actionType = createEnum("actionType", ActionType.class);

    public final com.merge.final_project.admin.QAdmin admin;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final StringPath description = createString("description");

    public final NumberPath<Long> logNo = createNumber("logNo", Long.class);

    public final NumberPath<Long> targetNo = createNumber("targetNo", Long.class);

    public final EnumPath<TargetType> targetType = createEnum("targetType", TargetType.class);

    public QAdminLog(String variable) {
        this(AdminLog.class, forVariable(variable), INITS);
    }

    public QAdminLog(Path<? extends AdminLog> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QAdminLog(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QAdminLog(PathMetadata metadata, PathInits inits) {
        this(AdminLog.class, metadata, inits);
    }

    public QAdminLog(Class<? extends AdminLog> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.admin = inits.isInitialized("admin") ? new com.merge.final_project.admin.QAdmin(forProperty("admin")) : null;
    }

}

