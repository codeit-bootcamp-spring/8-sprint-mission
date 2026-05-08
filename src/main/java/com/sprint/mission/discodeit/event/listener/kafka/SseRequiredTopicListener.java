package com.sprint.mission.discodeit.event.listener.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.dto.dto.ChannelDto;
import com.sprint.mission.discodeit.dto.dto.NotificationDto;
import com.sprint.mission.discodeit.dto.dto.UserDto;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.event.Sse.Channel.ChannelCreatedEvent;
import com.sprint.mission.discodeit.event.Sse.Channel.ChannelDeletedEvent;
import com.sprint.mission.discodeit.event.Sse.Channel.ChannelUpdatedEvent;
import com.sprint.mission.discodeit.event.Sse.NotificationCreatedEvent;
import com.sprint.mission.discodeit.event.Sse.User.UserCreatedEvent;
import com.sprint.mission.discodeit.event.Sse.User.UserDeletedEvent;
import com.sprint.mission.discodeit.event.Sse.User.UserUpdatedEvent;
import com.sprint.mission.discodeit.event.UserLoginOutEvent;
import com.sprint.mission.discodeit.service.Sse.SseService;
import com.sprint.mission.discodeit.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class SseRequiredTopicListener {
    private final SseService sseService;
    private final UserService userService;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = "discodeit.NotificationCreatedEvent",
            groupId = "${spring.kafka.consumer.group-id}-sse-${random.uuid}"
    )
    public void onNotificationCreatedEvent(String kafkaEvent) {
        try {
            NotificationCreatedEvent event = objectMapper.readValue(
                    kafkaEvent,
                    NotificationCreatedEvent.class
            );

            List<NotificationDto> notifications = event.getData();
            notifications.forEach(notification -> {
                UUID receiverId = notification.receiverId();
                sseService.send(Set.of(receiverId), "notifications.created", notification);
            });
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @KafkaListener(
            topics = "discodeit.ChannelCreatedEvent",
            groupId = "${spring.kafka.consumer.group-id}-sse-${random.uuid}"
    )
    public void onChannelCreatedEvent(String kafkaEvent) {
        try {
            ChannelCreatedEvent event = objectMapper.readValue(
                    kafkaEvent,
                    ChannelCreatedEvent.class
            );

            handleChannelEvent("channels.created", event.getData());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @KafkaListener(
            topics = "discodeit.ChannelUpdatedEvent",
            groupId = "${spring.kafka.consumer.group-id}-sse-${random.uuid}"
    )
    public void onChannelUpdatedEvent(String kafkaEvent) {
        try {
            ChannelUpdatedEvent event = objectMapper.readValue(
                    kafkaEvent,
                    ChannelUpdatedEvent.class
            );

            handleChannelEvent("channels.updated", event.getTo());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @KafkaListener(
            topics = "discodeit.ChannelDeletedEvent",
            groupId = "${spring.kafka.consumer.group-id}-sse-${random.uuid}"
    )
    public void onChannelDeletedEvent(String kafkaEvent) {
        try {
            ChannelDeletedEvent event = objectMapper.readValue(
                    kafkaEvent,
                    ChannelDeletedEvent.class
            );

            handleChannelEvent("channels.deleted", event.getData());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @KafkaListener(
            topics = "discodeit.UserCreatedEvent",
            groupId = "${spring.kafka.consumer.group-id}-sse-${random.uuid}"
    )
    public void onUserCreatedEvent(String kafkaEvent) {
        try {
            UserCreatedEvent event = objectMapper.readValue(
                    kafkaEvent,
                    UserCreatedEvent.class
            );

            sseService.broadcast("users.created", event.getData());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @KafkaListener(
            topics = "discodeit.UserUpdatedEvent",
            groupId = "${spring.kafka.consumer.group-id}-sse-${random.uuid}"
    )
    public void onUserUpdatedEvent(String kafkaEvent) {
        try {
            UserUpdatedEvent event = objectMapper.readValue(
                    kafkaEvent,
                    UserUpdatedEvent.class
            );

            sseService.broadcast("users.updated", event.getData());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @KafkaListener(
            topics = "discodeit.UserDeletedEvent",
            groupId = "${spring.kafka.consumer.group-id}-sse-${random.uuid}"
    )
    public void onUserDeletedEvent(String kafkaEvent) {
        try {
            UserDeletedEvent event = objectMapper.readValue(
                    kafkaEvent,
                    UserDeletedEvent.class
            );

            sseService.broadcast("users.deleted", event.getData());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @KafkaListener(
            topics = "discodeit.UserLoginOutEvent",
            groupId = "${spring.kafka.consumer.group-id}-sse-${random.uuid}"
    )
    public void onUserLoginOutEvent(String kafkaEvent) {
        try {
            UserLoginOutEvent event = objectMapper.readValue(
                    kafkaEvent,
                    UserLoginOutEvent.class
            );
            UUID userId = event.userId();
            UserDto userDto = userService.find(userId);
            sseService.broadcast("users.updated", userDto);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private void handleChannelEvent(String eventName, ChannelDto eventData) {
        if (eventData.type().equals(ChannelType.PUBLIC)) {
            sseService.broadcast(eventName, eventData);
        } else {
            Set<UUID> receiverIds = eventData.participants().stream()
                    .map(UserDto::id)
                    .collect(Collectors.toSet());
            sseService.send(receiverIds, eventName, eventData);
        }
    }
}
