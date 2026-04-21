package com.sprint.mission.discodeit.exception.notification;

import com.sprint.mission.discodeit.exception.ErrorCode;

public class NotificationFailedException extends NotificationException {

  public NotificationFailedException(Throwable cause) {
    super(ErrorCode.NOTIFICATION_FAILED, cause);
  }
}
