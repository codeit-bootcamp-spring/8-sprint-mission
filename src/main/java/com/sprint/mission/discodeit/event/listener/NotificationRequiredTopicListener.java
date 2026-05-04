package com.sprint.mission.discodeit.event.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.dto.dto.ChannelDto;
import com.sprint.mission.discodeit.dto.dto.MessageDto;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.Role;
import com.sprint.mission.discodeit.event.MessageCreatedEvent;
import com.sprint.mission.discodeit.event.RoleUpdatedEvent;
import com.sprint.mission.discodeit.event.S3UploadFailedEvent;
import com.sprint.mission.discodeit.event.UserLoginOutEvent;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.ChannelService;
import com.sprint.mission.discodeit.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Component
public class NotificationRequiredTopicListener {

    @Value("${discodeit.admin.username}")
    private String adminName;

    private final NotificationService notificationService;
    private final ReadStatusRepository readStatusRepository;
    private final ChannelService channelService;
    private final UserRepository userRepository;

    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "discodeit.MessageCreatedEvent")
    public void onMessageCreatedEvent(String kafkaEvent) {
        try {
            MessageCreatedEvent event = objectMapper.readValue(kafkaEvent, MessageCreatedEvent.class);

            MessageDto message = event.getData();
            UUID channelId = message.channelId();
            ChannelDto channel = channelService.find(channelId);

            Set<UUID> receiverIds = readStatusRepository.findAllByChannelIdAndNotificationEnabledTrue(channelId)
                    .stream().map(readStatus -> readStatus.getUser().getId())
                    .filter(receiverId -> !receiverId.equals(message.author().id()))
                    .collect(Collectors.toSet());

            log.info("author id: {}", message.author().id());
            log.info("receiverIds: {}", receiverIds);

            String title = message.author().username()
                    .concat(
                            channel.type().equals(ChannelType.PUBLIC) ?
                                    String.format(" (%s)", channel.name()) : ""
                    );
            String content = message.content();

            notificationService.create(receiverIds, title, content);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @KafkaListener(topics = "discodeit.RoleUpdatedEvent")
    public void onRoleUpdatedEvent(String kafkaEvent) {
        try {
            RoleUpdatedEvent event = objectMapper.readValue(kafkaEvent, RoleUpdatedEvent.class);
            UUID userId = event.userId();
            Role oldRole = event.oldRole();
            Role newRole = event.newRole();

            String title = "권한이 변경되었습니다.";
            String content = String.format("%s -> %s", oldRole, newRole);

            notificationService.create(Set.of(userId), title, content);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @KafkaListener(topics = "discodeit.S3UploadFailedEvent")
    public void onS3UploadFailedEvent(String kafkaEvent) {
        try {
            S3UploadFailedEvent event = objectMapper.readValue(kafkaEvent, S3UploadFailedEvent.class);
            String requestId = event.getRequestId();
            UUID binaryContentId = event.getBinaryContentId();
            Throwable e = event.getE();

            String title = "S3 파일 업로드 실패";

            StringBuilder sb = new StringBuilder();
            sb.append("RequestId: ").append(requestId).append("\n");
            sb.append("BinaryContentId: ").append(binaryContentId).append("\n");
            sb.append("Error: ").append(e).append("\n");
            String content = sb.toString();

            Set<UUID> receiverIds = userRepository.findByUsername(adminName)
                    .map(user -> Set.of(user.getId()))
                    .orElseGet(() -> {
                        log.error("S3 업로드 실패 알림을 보낼 관리자를 찾을 수 없습니다. (adminName: {})", adminName);
                        return Set.of();
                    });
            notificationService.create(receiverIds, title, content);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

//    @KafkaListener(
//            topics = "discodeit.UserLoginOutEvent",
//            groupId = "${spring.kafka.consumer.group-id}"
//    )
//    public void onUserLoginOutEvent(String kafkaEvent) {
//        try {
//            UserLoginOutEvent event = objectMapper.readValue(kafkaEvent, UserLoginOutEvent.class);
//            UUID userId = event.userId();
//            boolean isLogin = event.isLogin();
//
//            String title = "온라인 상태 변경";
//            String content = isLogin ? "온라인" : "오프라인";
//
//            notificationService.create(Set.of(userId), title, content);
//        } catch (JsonProcessingException e) {
//            throw new RuntimeException(e);
//        }
//    }
}
