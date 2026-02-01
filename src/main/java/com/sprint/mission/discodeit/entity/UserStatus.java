package com.sprint.mission.discodeit.entity;

import com.sprint.mission.discodeit.entity.base.BaseUpdatableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.time.Duration;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "user_statuses", schema = "discodeit_user")
public class UserStatus extends BaseUpdatableEntity implements Serializable {

  private static final int SESSION_TIMEOUT_MINUTES = 5;
  private static final long serialVersionUID = 1L;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(
      name = "user_id",
      unique = true,
      nullable = false
  )
  private User user;

  @Column(
      name = "last_active_at",
      nullable = false
  )
  private Instant lastActiveAt;

  public UserStatus(User user, Instant lastActiveAt) {
    this.user = user;
    this.lastActiveAt = lastActiveAt;
  }

  public void update(Instant lastActiveAt) {
    if (lastActiveAt != null && !lastActiveAt.equals(this.lastActiveAt)) {
      this.lastActiveAt = lastActiveAt;
    }
  }

  public Boolean isOnline() {
    Instant instantFiveMinutesAgo = Instant.now()
        .minus(Duration.ofMinutes(SESSION_TIMEOUT_MINUTES));
    return lastActiveAt.isAfter(instantFiveMinutesAgo);
  }
}
