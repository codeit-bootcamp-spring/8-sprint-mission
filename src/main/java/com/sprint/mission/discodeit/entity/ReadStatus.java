package com.sprint.mission.discodeit.entity;

import com.sprint.mission.discodeit.entity.base.BaseUpdatableEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "read_statuses", uniqueConstraints = {
    @UniqueConstraint(name = "uk_read_statuses_user_channel", columnNames = {"user_id", "channel_id"})
})
@Getter
@NoArgsConstructor
public class ReadStatus extends BaseUpdatableEntity {

  // User와의 Many-to-One 관계
  // schema.sql: ON DELETE CASCADE
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  // Channel과의 Many-to-One 관계
  // schema.sql: ON DELETE CASCADE
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "channel_id", nullable = false)
  private Channel channel;

  @Column(name = "last_read_at", nullable = false)
  private Instant lastReadAt;

  // 생성자
  public ReadStatus(User user, Channel channel, Instant lastReadAt) {
    this.user = user;
    this.channel = channel;
    this.lastReadAt = lastReadAt;
  }

  // update 메소드
  public void update(Instant lastReadAt) {
    this.lastReadAt = lastReadAt;
  }

  // updateLastReadAt 메소드 (별칭)
  public void updateLastReadAt(Instant lastReadAt) {
    this.lastReadAt = lastReadAt;
  }

  // updateLastReadMessage 메소드 (하위 호환성 - 메시지 ID는 무시)
  public void updateLastReadMessage(UUID messageId) {
    // 메시지 ID는 사용하지 않음, 현재 시간으로 업데이트
    this.lastReadAt = java.time.Instant.now();
  }

  // 헬퍼 메서드들
  public UUID getUserId() {
    return user != null ? user.getId() : null;
  }

  public UUID getChannelId() {
    return channel != null ? channel.getId() : null;
  }
}
