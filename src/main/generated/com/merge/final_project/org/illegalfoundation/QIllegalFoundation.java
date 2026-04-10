package com.merge.final_project.org.illegalfoundation;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QIllegalFoundation is a Querydsl query type for IllegalFoundation
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QIllegalFoundation extends EntityPathBase<IllegalFoundation> {

    private static final long serialVersionUID = 2137520601L;

    public static final QIllegalFoundation illegalFoundation = new QIllegalFoundation("illegalFoundation");

    public final NumberPath<Long> illegalNo = createNumber("illegalNo", Long.class);

    public final StringPath name = createString("name");

    public final StringPath reason = createString("reason");

    public final StringPath representative = createString("representative");

    public QIllegalFoundation(String variable) {
        super(IllegalFoundation.class, forVariable(variable));
    }

    public QIllegalFoundation(Path<? extends IllegalFoundation> path) {
        super(path.getType(), path.getMetadata());
    }

    public QIllegalFoundation(PathMetadata metadata) {
        super(IllegalFoundation.class, metadata);
    }

}

