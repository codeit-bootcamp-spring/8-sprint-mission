package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.NotificationDto;
import com.sprint.mission.discodeit.entity.DiscodeitUserDetails;
import com.sprint.mission.discodeit.service.NotificationService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

  private final NotificationService notificationService;

  @GetMapping
  public ResponseEntity<List<NotificationDto>> findAll(
      @AuthenticationPrincipal DiscodeitUserDetails userDetails
  ) {
    UUID userId = userDetails.getUserDto().id();

    List<NotificationDto> result = notificationService.findAll(userId);

    return ResponseEntity
        .status(HttpStatus.OK)
        .body(result);
  }

  @DeleteMapping("/{notificationId}")
  public ResponseEntity<Void> delete(
      @AuthenticationPrincipal DiscodeitUserDetails userDetails,
      @PathVariable UUID notificationId
  ) {
    UUID userId = userDetails.getUserDto().id();

    notificationService.deleteByIdIfOwner(userId, notificationId);

    return ResponseEntity
        .status(HttpStatus.NO_CONTENT)
        .build();
  }

}
