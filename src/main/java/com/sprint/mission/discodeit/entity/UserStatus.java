package com.sprint.mission.discodeit.entity;

import com.sprint.mission.discodeit.entity.base.BaseUpdatableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.Duration;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/*
    사용자 별로 마지막으로 접속한 시간을 나타낸다. -> 사용자의 온라인 상태를 확인하기 위해 활용

    [필드 설명]
    • user                : 유저 객체
    • lastActiveAt        : 마지막 접속 시간

    [메서드]
    • isOnline: 마지막 접속 시간이 현재 기준으로 5분 이내이면 접속 중인 유저로 간주하는 메서드

 */
@Entity
@Table(name = "user_statuses")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserStatus extends BaseUpdatableEntity {

  private static final int ONLINE_VERIFICATION_MINUTES = 5;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false, unique = true)
  private User user;

  @Column(name = "last_active_at", nullable = false)
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
