package com.sprint.mission.discodeit.entity;


import com.sprint.mission.discodeit.entity.base.BaseUpdatableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/*
    사용자가 채널 별 마지막으로 메시지를 읽은 시간을 표현하는 도메인 모델입니다.

     [필드 설명]
    • user                   : 유저 객체
    • channel                : 채널 객체
    • lastReadAt             : 마지막으로 메시지를 읽은 시간

 */
@Entity
@Table(name = "read_statuses", uniqueConstraints = {
    @UniqueConstraint(name = "uk_read_statuses_user_channel", columnNames = {"user_id",
        "channel_id"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReadStatus extends BaseUpdatableEntity {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "channel_id", nullable = false)
  private Channel channel;

  @Column(name = "last_read_at", nullable = false)
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
