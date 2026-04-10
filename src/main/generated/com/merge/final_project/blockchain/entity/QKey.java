package com.merge.final_project.blockchain.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QKey is a Querydsl query type for Key
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QKey extends EntityPathBase<Key> {

    private static final long serialVersionUID = 1063106701L;

    public static final QKey key = new QKey("key1");

    public final StringPath aesKey = createString("aesKey");

    public final NumberPath<Long> keyNo = createNumber("keyNo", Long.class);

    public final StringPath privateKey = createString("privateKey");

    public QKey(String variable) {
        super(Key.class, forVariable(variable));
    }

    public QKey(Path<? extends Key> path) {
        super(path.getType(), path.getMetadata());
    }

    public QKey(PathMetadata metadata) {
        super(Key.class, metadata);
    }

}

