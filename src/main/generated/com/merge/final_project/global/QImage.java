package com.merge.final_project.global;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QImage is a Querydsl query type for Image
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QImage extends EntityPathBase<Image> {

    private static final long serialVersionUID = -2045359791L;

    public static final QImage image = new QImage("image");

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final NumberPath<Long> imgNo = createNumber("imgNo", Long.class);

    public final StringPath imgOrgName = createString("imgOrgName");

    public final StringPath imgPath = createString("imgPath");

    public final StringPath imgStoredName = createString("imgStoredName");

    public final StringPath purpose = createString("purpose");

    public final StringPath targetName = createString("targetName");

    public final NumberPath<Long> targetNo = createNumber("targetNo", Long.class);

    public QImage(String variable) {
        super(Image.class, forVariable(variable));
    }

    public QImage(Path<? extends Image> path) {
        super(path.getType(), path.getMetadata());
    }

    public QImage(PathMetadata metadata) {
        super(Image.class, metadata);
    }

}

