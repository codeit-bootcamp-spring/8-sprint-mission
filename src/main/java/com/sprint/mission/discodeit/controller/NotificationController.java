package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.controller.api.NotificationApi;
import com.sprint.mission.discodeit.dto.data.NotificationDto;
import com.sprint.mission.discodeit.service.NotificationService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/notifications")
public class NotificationController implements NotificationApi {

  private final NotificationService notificationService;

  @GetMapping
  @Override
  public ResponseEntity<List<NotificationDto>> findAll(Authentication authentication) {
    return ResponseEntity.ok(notificationService.findAllForCurrentUser(authentication));
  }

  @DeleteMapping("/{notificationId}")
  @Override
  public ResponseEntity<Void> delete(
      @PathVariable("notificationId") UUID notificationId,
      Authentication authentication) {
    notificationService.delete(notificationId, authentication);
    return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
  }
}
