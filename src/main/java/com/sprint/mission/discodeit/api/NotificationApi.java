package com.sprint.mission.discodeit.api;

import com.sprint.mission.discodeit.dto.notification.NotificationDto;
import com.sprint.mission.discodeit.security.DiscodeitUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Tag(name = "Notification", description = "Notification API")
public interface NotificationApi {

  @Operation(summary = "내 알림 목록 조회", description = "현재 로그인한 사용자의 모든 알림을 최신순으로 조회합니다.")
  @GetMapping
  List<NotificationDto> getNotifications(@AuthenticationPrincipal DiscodeitUserDetails userDetails);

  @Operation(summary = "알림 확인(삭제)", description = "특정 알림을 확인하여 삭제 처리합니다. 본인의 알림만 삭제 가능합니다.")
  @DeleteMapping("/{notificationId}")
  void checkNotification(@PathVariable UUID notificationId, @AuthenticationPrincipal DiscodeitUserDetails userDetails);
}
