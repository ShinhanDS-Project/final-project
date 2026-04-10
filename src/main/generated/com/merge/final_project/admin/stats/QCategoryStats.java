package com.merge.final_project.admin.stats;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QCategoryStats is a Querydsl query type for CategoryStats
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QCategoryStats extends EntityPathBase<CategoryStats> {

    private static final long serialVersionUID = 1242830194L;

    public static final QCategoryStats categoryStats = new QCategoryStats("categoryStats");

    public final com.merge.final_project.global.QBaseEntity _super = new com.merge.final_project.global.QBaseEntity(this);

    public final NumberPath<Long> categoryCampaignCount = createNumber("categoryCampaignCount", Long.class);

    public final StringPath categoryName = createString("categoryName");

    public final NumberPath<Long> categoryStatsNo = createNumber("categoryStatsNo", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final StringPath donationAmount = createString("donationAmount");

    public final DatePath<java.time.LocalDate> statsDate = createDate("statsDate", java.time.LocalDate.class);

    public final DateTimePath<java.time.LocalDateTime> updatedAt = createDateTime("updatedAt", java.time.LocalDateTime.class);

    public QCategoryStats(String variable) {
        super(CategoryStats.class, forVariable(variable));
    }

    public QCategoryStats(Path<? extends CategoryStats> path) {
        super(path.getType(), path.getMetadata());
    }

    public QCategoryStats(PathMetadata metadata) {
        super(CategoryStats.class, metadata);
    }

}

