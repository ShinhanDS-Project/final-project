package com.merge.final_project.donation.payment;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QPayment is a Querydsl query type for Payment
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QPayment extends EntityPathBase<Payment> {

    private static final long serialVersionUID = -1129601981L;

    public static final QPayment payment = new QPayment("payment");

    public final NumberPath<java.math.BigDecimal> amount = createNumber("amount", java.math.BigDecimal.class);

    public final NumberPath<Long> campaignNo = createNumber("campaignNo", Long.class);

    public final StringPath orderKey = createString("orderKey");

    public final DateTimePath<java.time.LocalDateTime> paidAt = createDateTime("paidAt", java.time.LocalDateTime.class);

    public final EnumPath<PaymentMethod> payment_method = createEnum("payment_method", PaymentMethod.class);

    public final StringPath paymentKey = createString("paymentKey");

    public final NumberPath<Long> paymentNo = createNumber("paymentNo", Long.class);

    public final EnumPath<PaymentStatus> status = createEnum("status", PaymentStatus.class);

    public final NumberPath<Long> userNo = createNumber("userNo", Long.class);

    public QPayment(String variable) {
        super(Payment.class, forVariable(variable));
    }

    public QPayment(Path<? extends Payment> path) {
        super(path.getType(), path.getMetadata());
    }

    public QPayment(PathMetadata metadata) {
        super(Payment.class, metadata);
    }

}

