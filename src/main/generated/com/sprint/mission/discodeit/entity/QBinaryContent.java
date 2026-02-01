package com.sprint.mission.discodeit.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QBinaryContent is a Querydsl query type for BinaryContent
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QBinaryContent extends EntityPathBase<BinaryContent> {

    private static final long serialVersionUID = 453094046L;

    public static final QBinaryContent binaryContent = new QBinaryContent("binaryContent");

    public final com.sprint.mission.discodeit.entity.base.QBaseEntity _super = new com.sprint.mission.discodeit.entity.base.QBaseEntity(this);

    public final StringPath contentType = createString("contentType");

    //inherited
    public final DateTimePath<java.time.Instant> createdAt = _super.createdAt;

    public final StringPath fileName = createString("fileName");

    //inherited
    public final ComparablePath<java.util.UUID> id = _super.id;

    public final NumberPath<Long> size = createNumber("size", Long.class);

    public QBinaryContent(String variable) {
        super(BinaryContent.class, forVariable(variable));
    }

    public QBinaryContent(Path<? extends BinaryContent> path) {
        super(path.getType(), path.getMetadata());
    }

    public QBinaryContent(PathMetadata metadata) {
        super(BinaryContent.class, metadata);
    }

}

