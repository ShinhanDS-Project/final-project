package com.merge.final_project.campaign.useplan.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QUsePlan is a Querydsl query type for UsePlan
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QUsePlan extends EntityPathBase<UsePlan> {

    private static final long serialVersionUID = 1027182976L;

    public static final QUsePlan usePlan = new QUsePlan("usePlan");

    public final NumberPath<Long> campaignNo = createNumber("campaignNo", Long.class);

    public final NumberPath<Long> planAmount = createNumber("planAmount", Long.class);

    public final StringPath planContent = createString("planContent");

    public final NumberPath<Long> usePlanNo = createNumber("usePlanNo", Long.class);

    public QUsePlan(String variable) {
        super(UsePlan.class, forVariable(variable));
    }

    public QUsePlan(Path<? extends UsePlan> path) {
        super(path.getType(), path.getMetadata());
    }

    public QUsePlan(PathMetadata metadata) {
        super(UsePlan.class, metadata);
    }

}

