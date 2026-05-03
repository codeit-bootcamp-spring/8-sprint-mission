package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.auth.service.DiscodeitUserDetails;
import com.sprint.mission.discodeit.dto.data.NotificationDto;
import com.sprint.mission.discodeit.service.NotificationService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
public class NotificationController {

  private final NotificationService notificationService;

  @GetMapping
  public ResponseEntity<List<NotificationDto>> find(
      @AuthenticationPrincipal DiscodeitUserDetails discodeitUserDetails) {
    UUID receiverId = discodeitUserDetails.getUser().id();
    List<NotificationDto> notifications = notificationService.findAll(receiverId);

    return ResponseEntity.ok(notifications);
  }

  @DeleteMapping("{notificationId}")
  public ResponseEntity<Void> delete(
      @PathVariable UUID notificationId,
      @AuthenticationPrincipal DiscodeitUserDetails discodeitUserDetails) {
    UUID receiverId = discodeitUserDetails.getUser().id();
    notificationService.delete(notificationId, receiverId);
    return ResponseEntity.noContent().build();
  }

}
