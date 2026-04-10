package com.merge.final_project.campaign.campaigns.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QCampaign is a Querydsl query type for Campaign
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QCampaign extends EntityPathBase<Campaign> {

    private static final long serialVersionUID = -685432749L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QCampaign campaign = new QCampaign("campaign");

    public final com.merge.final_project.global.QBaseCreatedAtEntity _super = new com.merge.final_project.global.QBaseCreatedAtEntity(this);

    public final DateTimePath<java.time.LocalDateTime> achievedAt = createDateTime("achievedAt", java.time.LocalDateTime.class);

    public final EnumPath<com.merge.final_project.campaign.campaigns.ApprovalStatus> approvalStatus = createEnum("approvalStatus", com.merge.final_project.campaign.campaigns.ApprovalStatus.class);

    public final DateTimePath<java.time.LocalDateTime> approvedAt = createDateTime("approvedAt", java.time.LocalDateTime.class);

    public final NumberPath<Long> beneficiaryNo = createNumber("beneficiaryNo", Long.class);

    public final NumberPath<Long> campaignNo = createNumber("campaignNo", Long.class);

    public final EnumPath<com.merge.final_project.campaign.campaigns.CampaignStatus> campaignStatus = createEnum("campaignStatus", com.merge.final_project.campaign.campaigns.CampaignStatus.class);

    public final StringPath category = createString("category");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Long> currentAmount = createNumber("currentAmount", Long.class);

    public final StringPath description = createString("description");

    public final DateTimePath<java.time.LocalDateTime> endAt = createDateTime("endAt", java.time.LocalDateTime.class);

    public final com.merge.final_project.org.QFoundation foundation;

    public final NumberPath<Long> foundationNo = createNumber("foundationNo", Long.class);

    public final StringPath imagePath = createString("imagePath");

    public final StringPath rejectReason = createString("rejectReason");

    public final DateTimePath<java.time.LocalDateTime> startAt = createDateTime("startAt", java.time.LocalDateTime.class);

    public final NumberPath<Long> targetAmount = createNumber("targetAmount", Long.class);

    public final StringPath title = createString("title");

    public final DateTimePath<java.time.LocalDateTime> updatedAt = createDateTime("updatedAt", java.time.LocalDateTime.class);

    public final DateTimePath<java.time.LocalDateTime> usageEndAt = createDateTime("usageEndAt", java.time.LocalDateTime.class);

    public final DateTimePath<java.time.LocalDateTime> usageStartAt = createDateTime("usageStartAt", java.time.LocalDateTime.class);

    public final NumberPath<Long> walletNo = createNumber("walletNo", Long.class);

    public QCampaign(String variable) {
        this(Campaign.class, forVariable(variable), INITS);
    }

    public QCampaign(Path<? extends Campaign> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QCampaign(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QCampaign(PathMetadata metadata, PathInits inits) {
        this(Campaign.class, metadata, inits);
    }

    public QCampaign(Class<? extends Campaign> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.foundation = inits.isInitialized("foundation") ? new com.merge.final_project.org.QFoundation(forProperty("foundation"), inits.get("foundation")) : null;
    }

}

