package com.merge.final_project.report.finalreport.entitiy;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QFinalReport is a Querydsl query type for FinalReport
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QFinalReport extends EntityPathBase<FinalReport> {

    private static final long serialVersionUID = 222064549L;

    public static final QFinalReport finalReport = new QFinalReport("finalReport");

    public final EnumPath<com.merge.final_project.report.finalreport.ReportApprovalStatus> approvalStatus = createEnum("approvalStatus", com.merge.final_project.report.finalreport.ReportApprovalStatus.class);

    public final NumberPath<Long> beneficiary_no = createNumber("beneficiary_no", Long.class);

    public final NumberPath<Long> campaign_no = createNumber("campaign_no", Long.class);

    public final StringPath content = createString("content");

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final DateTimePath<java.time.LocalDateTime> dueAt = createDateTime("dueAt", java.time.LocalDateTime.class);

    public final StringPath imagePath = createString("imagePath");

    public final NumberPath<Long> key_no = createNumber("key_no", Long.class);

    public final StringPath rejectReason = createString("rejectReason");

    public final NumberPath<Long> reportNo = createNumber("reportNo", Long.class);

    public final NumberPath<Long> settlementNo = createNumber("settlementNo", Long.class);

    public final StringPath title = createString("title");

    public final StringPath usagePurpose = createString("usagePurpose");

    public QFinalReport(String variable) {
        super(FinalReport.class, forVariable(variable));
    }

    public QFinalReport(Path<? extends FinalReport> path) {
        super(path.getType(), path.getMetadata());
    }

    public QFinalReport(PathMetadata metadata) {
        super(FinalReport.class, metadata);
    }

}

