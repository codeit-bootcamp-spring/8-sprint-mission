package com.sprint.mission.discodeit.exception;

import java.util.Map;
import java.util.UUID;

public abstract class NotificationException extends DiscodeitException {
    public NotificationException(ErrorCode errorCode, Map<String, Object> details) {
        super(errorCode, details);
    }

    public static class NotificationNotFoundException extends NotificationException {

        public NotificationNotFoundException(UUID notificationId) {
            super(ErrorCode.NOTIFICATION_NOT_FOUND, Map.of("notificationId", notificationId));
        }
    }

    public static class NotificationAccessDeniedException extends NotificationException {

        public NotificationAccessDeniedException(UUID notificationId) {
            super(ErrorCode.NOTIFICATION_ACCESS_DENIED, Map.of("notificationId", notificationId));
        }
    }
}
