package com.sprint.mission.discodeit.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QUser is a Querydsl query type for User
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QUser extends EntityPathBase<User> {

    private static final long serialVersionUID = -1220877403L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QUser user = new QUser("user");

    public final com.sprint.mission.discodeit.entity.base.QBaseUpdatableEntity _super = new com.sprint.mission.discodeit.entity.base.QBaseUpdatableEntity(this);

    //inherited
    public final DateTimePath<java.time.Instant> createdAt = _super.createdAt;

    public final StringPath email = createString("email");

    //inherited
    public final ComparablePath<java.util.UUID> id = _super.id;

    public final StringPath password = createString("password");

    public final QBinaryContent profile;

    //inherited
    public final DateTimePath<java.time.Instant> updatedAt = _super.updatedAt;

    public final StringPath username = createString("username");

    public final QUserStatus userStatus;

    public QUser(String variable) {
        this(User.class, forVariable(variable), INITS);
    }

    public QUser(Path<? extends User> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QUser(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QUser(PathMetadata metadata, PathInits inits) {
        this(User.class, metadata, inits);
    }

    public QUser(Class<? extends User> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.profile = inits.isInitialized("profile") ? new QBinaryContent(forProperty("profile")) : null;
        this.userStatus = inits.isInitialized("userStatus") ? new QUserStatus(forProperty("userStatus"), inits.get("userStatus")) : null;
    }

}

