package com.sprint.mission.discodeit.repository.impl;

import static com.sprint.mission.discodeit.entity.QBinaryContent.binaryContent;
import static com.sprint.mission.discodeit.entity.QMessage.message;
import static com.sprint.mission.discodeit.entity.QUser.user;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.repository.MessageRepositoryCustom;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

@RequiredArgsConstructor
public class MessageRepositoryImpl implements MessageRepositoryCustom {

  private final JPAQueryFactory queryFactory;

  @Override
  public Slice<Message> findAllByChannelIdWithCursor(UUID channelId, Instant cursor,
      Pageable pageable) {

    List<Message> content = queryFactory
        .selectFrom(message)
        .leftJoin(message.author, user).fetchJoin()
        .leftJoin(user.profile, binaryContent).fetchJoin()
        .where(
            message.channel.id.eq(channelId),
            cursorLt(cursor)
        )
        .orderBy(message.createdAt.desc())
        .limit(pageable.getPageSize() + 1)
        .fetch();

    //다음 요소가 있는가?
    boolean hasNext = false;
    if (content.size() > pageable.getPageSize()) {
      content.remove(pageable.getPageSize());
      hasNext = true;
    }

    return new SliceImpl<>(content, pageable, hasNext);
  }

  //현재 내가 본 기준점보다 작은 커서 값 찾기
  private BooleanExpression cursorLt(Instant cursor) {
    return cursor == null ? null : message.createdAt.lt(cursor);
  }

  @Override
  public Optional<Message> findTopByChannelIdOrderByCreatedAtDesc(UUID channelId) {
    return Optional.ofNullable(queryFactory
        .selectFrom(message)
        .where(message.channel.id.eq(channelId))
        .orderBy(message.createdAt.desc())
        .fetchFirst());
  }
}
