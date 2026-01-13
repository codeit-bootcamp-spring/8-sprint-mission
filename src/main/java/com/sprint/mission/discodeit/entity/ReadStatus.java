package com.sprint.mission.discodeit.entity;


import com.sprint.mission.discodeit.entity.base.BaseUpdatableEntity;
import java.time.Instant;
import lombok.Getter;

/*
    사용자가 채널 별 마지막으로 메시지를 읽은 시간을 표현하는 도메인 모델입니다.

     [필드 설명]
    • user                   : 유저 객체
    • channel                : 채널 객체
    • lastReadAt             : 마지막으로 메시지를 읽은 시간

 */
@Getter
public class ReadStatus extends BaseUpdatableEntity {

  private User user;
  private Channel channel;

  private Instant lastReadAt;

  public ReadStatus(User user, Channel channel, Instant lastReadAt) {
    this.user = user;
    this.channel = channel;
    this.lastReadAt = lastReadAt;
  }

  public void update(Instant newLastReadAt) {
    if (newLastReadAt != null) {
      this.lastReadAt = newLastReadAt;
    }
  }
}
