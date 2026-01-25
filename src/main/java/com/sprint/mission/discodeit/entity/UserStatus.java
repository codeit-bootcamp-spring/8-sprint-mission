package com.sprint.mission.discodeit.entity;

import com.sprint.mission.discodeit.entity.base.BaseUpdatableEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "user_statuses")
@Getter
@NoArgsConstructor
public class UserStatus extends BaseUpdatableEntity {

  // User와의 One-to-One 양방향 관계
  // schema.sql: ON DELETE CASCADE, UNIQUE 제약조건
  // User가 삭제되면 UserStatus도 삭제되어야 함
  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false, unique = true)
  private User user;

  @Column(name = "last_active_at", nullable = false)
  private Instant lastActiveAt;

  // 생성자
  public UserStatus(User user, Instant lastActiveAt) {
    this.setId(UUID.randomUUID());
    this.user = user;
    this.lastActiveAt = lastActiveAt;
  }

  // User만 받는 생성자 (lastActiveAt은 현재 시간으로 설정)
  public UserStatus(User user) {
    this.setId(UUID.randomUUID());
    this.user = user;
    this.lastActiveAt = Instant.now();
  }

  // update 메소드
  public void update(Instant lastActiveAt) {
    this.lastActiveAt = lastActiveAt;
  }

  // lastActiveAt 업데이트 메소드 (현재 시간으로)
  public void updateLastAccessAt() {
    this.lastActiveAt = Instant.now();
  }

  // 온라인 상태 확인 메소드 (5분 이내 활동이면 온라인)
  public boolean isOnline() {
    if (lastActiveAt == null) {
      return false;
    }
    Instant fiveMinutesAgo = Instant.now().minusSeconds(300);
    return lastActiveAt.isAfter(fiveMinutesAgo);
  }

  // 헬퍼 메서드
  public UUID getUserId() {
    return user != null ? user.getId() : null;
  }
}
