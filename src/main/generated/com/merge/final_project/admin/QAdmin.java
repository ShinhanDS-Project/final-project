package com.merge.final_project.admin;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QAdmin is a Querydsl query type for Admin
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QAdmin extends EntityPathBase<Admin> {

    private static final long serialVersionUID = -1464132209L;

    public static final QAdmin admin = new QAdmin("admin");

    public final com.merge.final_project.global.QBaseUpdatedAtEntity _super = new com.merge.final_project.global.QBaseUpdatedAtEntity(this);

    public final StringPath adminId = createString("adminId");

    public final NumberPath<Long> adminNo = createNumber("adminNo", Long.class);

    public final StringPath adminRole = createString("adminRole");

    public final StringPath name = createString("name");

    public final StringPath password = createString("password");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QAdmin(String variable) {
        super(Admin.class, forVariable(variable));
    }

    public QAdmin(Path<? extends Admin> path) {
        super(path.getType(), path.getMetadata());
    }

    public QAdmin(PathMetadata metadata) {
        super(Admin.class, metadata);
    }

}

