package com.merge.final_project.global;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QBaseUpdatedAtEntity is a Querydsl query type for BaseUpdatedAtEntity
 */
@Generated("com.querydsl.codegen.DefaultSupertypeSerializer")
public class QBaseUpdatedAtEntity extends EntityPathBase<BaseUpdatedAtEntity> {

    private static final long serialVersionUID = 440767030L;

    public static final QBaseUpdatedAtEntity baseUpdatedAtEntity = new QBaseUpdatedAtEntity("baseUpdatedAtEntity");

    public final DateTimePath<java.time.LocalDateTime> updatedAt = createDateTime("updatedAt", java.time.LocalDateTime.class);

    public QBaseUpdatedAtEntity(String variable) {
        super(BaseUpdatedAtEntity.class, forVariable(variable));
    }

    public QBaseUpdatedAtEntity(Path<? extends BaseUpdatedAtEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QBaseUpdatedAtEntity(PathMetadata metadata) {
        super(BaseUpdatedAtEntity.class, metadata);
    }

}

