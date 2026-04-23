package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.api.NotificationApi;
import com.sprint.mission.discodeit.dto.notification.NotificationDto;
import com.sprint.mission.discodeit.security.DiscodeitUserDetails;
import com.sprint.mission.discodeit.service.NotificationService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController implements NotificationApi {

  private final NotificationService notificationService;

  @Override
  public List<NotificationDto> getNotifications(@AuthenticationPrincipal DiscodeitUserDetails userDetails) {
    return notificationService.findAllByReceiverId(userDetails.getUserDto().id());
  }

  @Override
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void checkNotification(@PathVariable UUID notificationId, @AuthenticationPrincipal DiscodeitUserDetails userDetails) {
    notificationService.delete(notificationId, userDetails.getUserDto().id());
  }
}
