package com.sprint.mission.discodeit.entity;

import com.sprint.mission.discodeit.entity.base.BaseUpdatableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "read_statuses",
    uniqueConstraints = {@UniqueConstraint(columnNames = {"user_id", "channel_id"})}
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReadStatus extends BaseUpdatableEntity {

  @NotNull
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @NotNull
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "channel_id", nullable = false)
  private Channel channel;

  @NotNull
  @PastOrPresent
  @Column(name = "last_read_at", nullable = false)
  private Instant lastReadAt;

  @Column(name = "notification_enabled", nullable = false)
  private boolean notificationEnabled;

  public ReadStatus(User user, Channel channel, Instant lastReadAt) {
    this.user = user;
    this.channel = channel;
    this.lastReadAt = lastReadAt;
    this.notificationEnabled = (channel.getType() == ChannelType.PUBLIC);
  }

  public void update(Instant newLastReadAt, boolean newNotificationEnabled) {
    if (newLastReadAt != null && !newLastReadAt.equals(this.lastReadAt)) {
      this.lastReadAt = newLastReadAt;
    }

    if (newNotificationEnabled != this.notificationEnabled) {
      this.notificationEnabled = newNotificationEnabled;
    }
  }
}
