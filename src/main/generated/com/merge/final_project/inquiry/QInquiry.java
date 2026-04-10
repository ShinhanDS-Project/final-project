package com.merge.final_project.inquiry;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QInquiry is a Querydsl query type for Inquiry
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QInquiry extends EntityPathBase<Inquiry> {

    private static final long serialVersionUID = 1152720575L;

    public static final QInquiry inquiry = new QInquiry("inquiry");

    public final com.merge.final_project.global.QBaseEntity _super = new com.merge.final_project.global.QBaseEntity(this);

    public final NumberPath<Long> adminNo = createNumber("adminNo", Long.class);

    public final StringPath content = createString("content");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final StringPath gno = createString("gno");

    public final NumberPath<Long> inquiryNo = createNumber("inquiryNo", Long.class);

    public final NumberPath<Long> loginNo = createNumber("loginNo", Long.class);

    public final StringPath nested = createString("nested");

    public final StringPath ono = createString("ono");

    public final EnumPath<InquiryStatus> status = createEnum("status", InquiryStatus.class);

    public final StringPath title = createString("title");

    public final DateTimePath<java.time.LocalDateTime> updatedAt = createDateTime("updatedAt", java.time.LocalDateTime.class);

    public QInquiry(String variable) {
        super(Inquiry.class, forVariable(variable));
    }

    public QInquiry(Path<? extends Inquiry> path) {
        super(path.getType(), path.getMetadata());
    }

    public QInquiry(PathMetadata metadata) {
        super(Inquiry.class, metadata);
    }

}

