package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.controller.api.NotificationApi;
import com.sprint.mission.discodeit.dto.data.NotificationDto;
import com.sprint.mission.discodeit.service.NotificationService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.sprint.mission.discodeit.security.DiscodeitUserDetails;

@Slf4j
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController implements NotificationApi {

  private final NotificationService notificationService;

  @GetMapping
  public ResponseEntity<List<NotificationDto>> findAllByReceiverId(
      @AuthenticationPrincipal DiscodeitUserDetails userDetails) {
    if (userDetails == null) {
      log.warn("userDetails가 전달되지 않았습니다. 빈 목록을 반환합니다.");
      return ResponseEntity.ok(List.of());
    }
    List<NotificationDto> notifications = notificationService.findAllByReceiverId(userDetails.getUserDto().id());
    return ResponseEntity.ok(notifications);
  }

  @DeleteMapping("/{notificationId}")
  public ResponseEntity<Void> delete(
      @PathVariable UUID notificationId,
      @AuthenticationPrincipal DiscodeitUserDetails userDetails) {
    notificationService.delete(notificationId, userDetails.getUserDto().id());
    return ResponseEntity.noContent().build();
  }
}
