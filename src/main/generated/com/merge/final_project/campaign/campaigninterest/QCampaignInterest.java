package com.merge.final_project.campaign.campaigninterest;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QCampaignInterest is a Querydsl query type for CampaignInterest
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QCampaignInterest extends EntityPathBase<CampaignInterest> {

    private static final long serialVersionUID = -1064879951L;

    public static final QCampaignInterest campaignInterest = new QCampaignInterest("campaignInterest");

    public final com.merge.final_project.global.QBaseEntity _super = new com.merge.final_project.global.QBaseEntity(this);

    public final NumberPath<Long> campaignNo = createNumber("campaignNo", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Long> interestNo = createNumber("interestNo", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final NumberPath<Long> userNo = createNumber("userNo", Long.class);

    public QCampaignInterest(String variable) {
        super(CampaignInterest.class, forVariable(variable));
    }

    public QCampaignInterest(Path<? extends CampaignInterest> path) {
        super(path.getType(), path.getMetadata());
    }

    public QCampaignInterest(PathMetadata metadata) {
        super(CampaignInterest.class, metadata);
    }

}

