package com.sprint.mission.discodeit.event.listener;

import com.sprint.mission.discodeit.dto.dto.ChannelDto;
import com.sprint.mission.discodeit.dto.dto.MessageDto;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.Role;
import com.sprint.mission.discodeit.event.MessageCreatedEvent;
import com.sprint.mission.discodeit.event.RoleUpdatedEvent;
import com.sprint.mission.discodeit.event.S3UploadFailedEvent;
import com.sprint.mission.discodeit.event.Sse.NotificationCreatedEvent;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.ChannelService;
import com.sprint.mission.discodeit.service.NotificationService;
import com.sprint.mission.discodeit.service.Sse.SseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
// @Component
@RequiredArgsConstructor
public class NotificationRequiredEventListener {

    @Value("${discodeit.admin.username}")
    private String adminName;

    private final NotificationService notificationService;
    private final ChannelService channelService;

    private final ReadStatusRepository readStatusRepository;
    private final UserRepository userRepository;

    @Async("notificationTaskExecutor")
    @TransactionalEventListener
    public void on(MessageCreatedEvent event) {
        MessageDto message = event.getData();
        UUID channelId = message.channelId();
        ChannelDto channel = channelService.find(channelId);

        log.info("[NotificationListener] MessageCreatedEvent 수신");

        Set<UUID> receiverIds = readStatusRepository.findAllByChannelIdAndNotificationEnabledTrue(channelId)
                .stream()
                .map(readStatus -> readStatus.getUser().getId())
                .filter(receiverId -> !receiverId.equals(message.author().id()))
                .collect(Collectors.toSet());
        String title = message.author().username()
                .concat(
                        channel.type().equals(ChannelType.PUBLIC) ?
                                String.format(" (#%s)", channel.name()) : ""
                );
        String content = message.content();

        notificationService.create(receiverIds, title, content);
    }

    @Async("notificationTaskExecutor")
    @TransactionalEventListener
    public void on(RoleUpdatedEvent event) {
        UUID userId = event.userId();
        Role oldRole = event.oldRole();
        Role newRole = event.newRole();
        log.info("[NotificationListener] RoleUpdatedEvent 수신");

        String title = "권한이 변경되었습니다.";
        String content = String.format("%s -> %s", oldRole, newRole);

        notificationService.create(Set.of(userId), title, content);

        log.info("[NotificationListener] 권한 변경 알림 생성 완료");
    }

    @Async("notificationTaskExecutor")
    @TransactionalEventListener
    public void on(S3UploadFailedEvent event) {
        String requestId = event.getRequestId();
        UUID binaryContentId = event.getBinaryContentId();
        Throwable e = event.getE();
        log.info("[NotificationListener] S3UploadFailedEvent 수신");

        String title = "S3 파일 업로드 실패";

        StringBuilder sb = new StringBuilder();
        sb.append("RequestId: ").append(requestId).append("\n");
        sb.append("BinaryContentId: ").append(binaryContentId).append("\n");
        sb.append("Error: ").append(e.getMessage()).append("\n");

        String content = sb.toString();

        Set<UUID> receiverIds = userRepository.findByUsername(adminName)
                .map(user -> Set.of(user.getId()))
                .orElseGet(() -> {
                    log.error("S3 업로드 실패 알림을 보낼 관리자를 찾을 수 없습니다. (adminName: {})", adminName);
                    return Set.of();
                });

        notificationService.create(receiverIds, title, content);

        log.info("[NotificationListener] S3 파일 업로드 실패 알림 발송 완료");
    }
}
