package com.sprint.mission.discodeit.event;

import java.util.UUID;
import lombok.Data;

@Data
public class UserLogInOutEvent {

  private final UUID userId;
  private final boolean isLogin;
}
