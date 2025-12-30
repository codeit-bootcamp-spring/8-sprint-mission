package com.sprint.mission.discodeit.entity;


import lombok.Getter;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/*
    사용자가 채널 별 마지막으로 메시지를 읽은 시간을 표현하는 도메인 모델입니다.

     [필드 설명]
    • userId                 : 유저의 id
    • channelId              : 채널의 id
    • lastReadAt             : 마지막으로 메시지를 읽은 시간

 */
@Getter
public class ReadStatus extends BaseEntity implements Serializable {

  @Serial
  private static final long serialVersionUID = 1L;

  private UUID userId;
  private UUID channelId;
  private Instant lastReadAt;

  public ReadStatus(UUID userId, UUID channelId, Instant lastReadAt) {
    this.userId = userId;
    this.channelId = channelId;
    this.lastReadAt = lastReadAt;
  }

  public void update(Instant newLastReadAt) {
    if (newLastReadAt != null && !newLastReadAt.equals(this.lastReadAt)) {
      this.lastReadAt = newLastReadAt;
      updateCall();
    }
  }
}
