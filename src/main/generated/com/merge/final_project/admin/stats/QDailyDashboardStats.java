package com.merge.final_project.admin.stats;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QDailyDashboardStats is a Querydsl query type for DailyDashboardStats
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QDailyDashboardStats extends EntityPathBase<DailyDashboardStats> {

    private static final long serialVersionUID = -1538812075L;

    public static final QDailyDashboardStats dailyDashboardStats = new QDailyDashboardStats("dailyDashboardStats");

    public final com.merge.final_project.global.QBaseEntity _super = new com.merge.final_project.global.QBaseEntity(this);

    public final NumberPath<Long> achivedCampaignCount = createNumber("achivedCampaignCount", Long.class);

    public final NumberPath<Long> activeCampaignCount = createNumber("activeCampaignCount", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final StringPath dailyDonationAmount = createString("dailyDonationAmount");

    public final NumberPath<Long> dailyDonationCount = createNumber("dailyDonationCount", Long.class);

    public final NumberPath<Long> dailyNo = createNumber("dailyNo", Long.class);

    public final NumberPath<Long> newFoundationCount = createNumber("newFoundationCount", Long.class);

    public final NumberPath<Long> newUserCount = createNumber("newUserCount", Long.class);

    public final NumberPath<Long> pendingFoundationCount = createNumber("pendingFoundationCount", Long.class);

    public final NumberPath<Long> pendingInquiryCount = createNumber("pendingInquiryCount", Long.class);

    public final DatePath<java.time.LocalDate> statsDate = createDate("statsDate", java.time.LocalDate.class);

    public final DateTimePath<java.time.LocalDateTime> updatedAt = createDateTime("updatedAt", java.time.LocalDateTime.class);

    public QDailyDashboardStats(String variable) {
        super(DailyDashboardStats.class, forVariable(variable));
    }

    public QDailyDashboardStats(Path<? extends DailyDashboardStats> path) {
        super(path.getType(), path.getMetadata());
    }

    public QDailyDashboardStats(PathMetadata metadata) {
        super(DailyDashboardStats.class, metadata);
    }

}

