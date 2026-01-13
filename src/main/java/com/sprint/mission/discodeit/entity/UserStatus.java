package com.sprint.mission.discodeit.entity;

import com.sprint.mission.discodeit.entity.base.BaseUpdatableEntity;
import java.time.Duration;
import java.time.Instant;
import lombok.Getter;

/*
    사용자 별로 마지막으로 접속한 시간을 나타낸다. -> 사용자의 온라인 상태를 확인하기 위해 활용

    [필드 설명]
    • user                : 유저 객체
    • lastActiveAt        : 마지막 접속 시간

    [메서드]
    • isOnline: 마지막 접속 시간이 현재 기준으로 5분 이내이면 접속 중인 유저로 간주하는 메서드

 */
@Getter
public class UserStatus extends BaseUpdatableEntity {

  private static final int ONLINE_VERIFICATION_MINUTES = 5;

  private User user;

  private Instant lastActiveAt;

  public UserStatus(User user, Instant lastActiveAt) {
    this.user = user;
    this.lastActiveAt = lastActiveAt;
  }

  public void update(Instant newLastConnAt) {
    if (newLastConnAt != null) {
      this.lastActiveAt = newLastConnAt;
    }
  }

  public boolean isOnline() {

    if (lastActiveAt == null) {
      return false;
    }

    return lastActiveAt.isAfter(
        Instant.now().minus(Duration.ofMinutes(ONLINE_VERIFICATION_MINUTES)));
  }
}
