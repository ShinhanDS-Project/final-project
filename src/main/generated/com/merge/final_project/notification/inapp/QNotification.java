package com.merge.final_project.notification.inapp;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QNotification is a Querydsl query type for Notification
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QNotification extends EntityPathBase<Notification> {

    private static final long serialVersionUID = 1256684799L;

    public static final QNotification notification = new QNotification("notification");

    public final com.merge.final_project.global.QBaseCreatedAtEntity _super = new com.merge.final_project.global.QBaseCreatedAtEntity(this);

    public final StringPath content = createString("content");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final BooleanPath isRead = createBoolean("isRead");

    public final NumberPath<Long> notificationNo = createNumber("notificationNo", Long.class);

    public final EnumPath<NotificationType> notificationType = createEnum("notificationType", NotificationType.class);

    public final DateTimePath<java.time.LocalDateTime> readAt = createDateTime("readAt", java.time.LocalDateTime.class);

    public final NumberPath<Long> receiverNo = createNumber("receiverNo", Long.class);

    public final EnumPath<RecipientType> recipientType = createEnum("recipientType", RecipientType.class);

    public QNotification(String variable) {
        super(Notification.class, forVariable(variable));
    }

    public QNotification(Path<? extends Notification> path) {
        super(path.getType(), path.getMetadata());
    }

    public QNotification(PathMetadata metadata) {
        super(Notification.class, metadata);
    }

}

