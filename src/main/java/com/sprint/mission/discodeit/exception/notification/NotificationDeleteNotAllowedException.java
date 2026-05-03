package com.sprint.mission.discodeit.exception.notification;

import com.sprint.mission.discodeit.exception.ErrorCode;

public class NotificationDeleteNotAllowedException extends NotificationException {

  public NotificationDeleteNotAllowedException() {
    super(ErrorCode.NOTIFICATION_DELETE_NOT_ALLOWED);
  }
}
