package com.sprint.mission.discodeit.event.listener;

import com.sprint.mission.discodeit.dto.dto.ChannelDto;
import com.sprint.mission.discodeit.dto.dto.MessageDto;
import com.sprint.mission.discodeit.entity.*;
import com.sprint.mission.discodeit.event.MessageCreatedEvent;
import com.sprint.mission.discodeit.event.RoleUpdatedEvent;
import com.sprint.mission.discodeit.repository.NotificationRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.ChannelService;
import com.sprint.mission.discodeit.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationRequiredEventListener {

    private final NotificationService notificationService;
    private final ReadStatusRepository readStatusRepository;
    private final ChannelService channelService;
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
}
