package com.sprint.mission.discodeit.exception.notification;

import com.sprint.mission.discodeit.exception.ErrorCode;
import java.util.Map;

public class NotificationDeleteNotAllowedException extends NotificationException {

  public NotificationDeleteNotAllowedException() {
    super(ErrorCode.NOTIFICATION_NOT_FOUND);
  }
}
