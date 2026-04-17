package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.dto.NotificationDto;
import com.sprint.mission.discodeit.service.NotificationService;
import com.sprint.mission.discodeit.service.auth.DiscodeitUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<List<NotificationDto>> findNotifications(
            @AuthenticationPrincipal DiscodeitUserDetails userDetails
    ) {
        UUID receiverId = userDetails.getUserDto().id();

        List<NotificationDto> notifications = notificationService.findAllByReceiver(receiverId);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(notifications);
    }

    @DeleteMapping(path = "/{notificationId}")
    public ResponseEntity<Void> confirmNotification(
            @PathVariable UUID notificationId,
            @AuthenticationPrincipal DiscodeitUserDetails userDetails
    ) {
        notificationService.confirmAndDelete(notificationId, userDetails.getUserDto().id());
        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .build();
    }
}
