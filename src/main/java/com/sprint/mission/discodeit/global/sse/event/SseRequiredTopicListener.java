package com.sprint.mission.discodeit.global.sse.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.domain.binarycontent.dto.domain.BinaryContentDto;
import com.sprint.mission.discodeit.domain.binarycontent.event.BinaryContentStatusUpdatedEvent;
import com.sprint.mission.discodeit.domain.channel.domain.enums.ChannelType;
import com.sprint.mission.discodeit.domain.channel.dto.domain.ChannelDto;
import com.sprint.mission.discodeit.domain.channel.event.ChannelCreatedEvent;
import com.sprint.mission.discodeit.domain.channel.event.ChannelDeletedEvent;
import com.sprint.mission.discodeit.domain.channel.event.ChannelUpdatedEvent;
import com.sprint.mission.discodeit.domain.notification.dto.entity.NotificationDto;
import com.sprint.mission.discodeit.domain.notification.event.NotificationCreatedEvent;
import com.sprint.mission.discodeit.domain.user.dto.domain.UserDto;
import com.sprint.mission.discodeit.domain.user.event.UserCreatedEvent;
import com.sprint.mission.discodeit.domain.user.event.UserDeletedEvent;
import com.sprint.mission.discodeit.domain.user.event.UserLogInOutEvent;
import com.sprint.mission.discodeit.domain.user.event.UserUpdatedEvent;
import com.sprint.mission.discodeit.domain.user.service.UserService;
import com.sprint.mission.discodeit.global.sse.service.SseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Component
public class SseRequiredTopicListener {

    private final SseService sseService;
    private final UserService userService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "discodeit.NotificationCreatedEvent", groupId = "sse-${random.uuid}")
    public void onNotificationCreatedEvent(String kafkaEvent) {
        try {
            NotificationCreatedEvent event = objectMapper.readValue(kafkaEvent,
                    NotificationCreatedEvent.class);

            NotificationDto notification = event.notification();
            sseService.send(Set.of(notification.receiverId()), "notifications.created", notification);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

    }

    @KafkaListener(topics = "discodeit.BinaryContentStatusUpdatedEvent", groupId = "sse-${random.uuid}")
    public void onBinaryContentStatusUpdatedEvent(String kafkaEvent) {
        try {
            BinaryContentStatusUpdatedEvent event = objectMapper.readValue(kafkaEvent,
                    BinaryContentStatusUpdatedEvent.class);
            BinaryContentDto binaryContent = event.binaryContentDto();
            sseService.broadcast("binaryContents.updated", binaryContent);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @KafkaListener(topics = "discodeit.ChannelCreatedEvent", groupId = "sse-${random.uuid}")
    public void onChannelCreatedEvent(String kafkaEvent) {
        try {
            ChannelCreatedEvent channelEvent = objectMapper.readValue(kafkaEvent,
                    ChannelCreatedEvent.class);

            String eventName = "channels.created";
            ChannelDto eventData = channelEvent.channelDto();

            handleChannelEvent(eventName, eventData);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @KafkaListener(topics = "discodeit.ChannelUpdatedEvent", groupId = "sse-${random.uuid}")
    public void onChannelUpdatedEvent(String kafkaEvent) {
        try {
            ChannelUpdatedEvent channelEvent = objectMapper.readValue(kafkaEvent,
                    ChannelUpdatedEvent.class);

            String eventName = "channels.updated";
            ChannelDto eventData = channelEvent.to();

            handleChannelEvent(eventName, eventData);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @KafkaListener(topics = "discodeit.ChannelDeletedEvent", groupId = "sse-${random.uuid}")
    public void onChannelDeletedEvent(String kafkaEvent) {
        try {
            ChannelDeletedEvent channelEvent = objectMapper.readValue(kafkaEvent,
                    ChannelDeletedEvent.class);

            String eventName = "channels.deleted";
            ChannelDto eventData = channelEvent.data();

            handleChannelEvent(eventName, eventData);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private void handleChannelEvent(String eventName, ChannelDto eventData) {
        if (eventData.type().equals(ChannelType.PUBLIC)) {
            sseService.broadcast(eventName, eventData);
        } else {
            Set<UUID> receiverIds = eventData.participants().stream().map(UserDto::id)
                    .collect(Collectors.toSet());
            sseService.send(receiverIds, eventName, eventData);
        }
    }

    @KafkaListener(topics = "discodeit.UserCreatedEvent", groupId = "sse-${random.uuid}")
    public void onUserCreatedEvent(String kafkaEvent) {
        try {
            UserCreatedEvent userEvent = objectMapper.readValue(kafkaEvent, UserCreatedEvent.class);

            String eventName = "users.created";
            UserDto eventData = userEvent.data();

            handleUserEvent(eventName, eventData);

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @KafkaListener(topics = "discodeit.UserUpdatedEvent", groupId = "sse-${random.uuid}")
    public void onUserUpdatedEvent(String kafkaEvent) {
        try {
            UserUpdatedEvent userEvent = objectMapper.readValue(kafkaEvent, UserUpdatedEvent.class);

            String eventName = "users.updated";
            UserDto eventData = userEvent.to();

            handleUserEvent(eventName, eventData);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @KafkaListener(topics = "discodeit.UserDeletedEvent", groupId = "sse-${random.uuid}")
    public void onUserDeletedEvent(String kafkaEvent) {
        try {
            UserDeletedEvent userEvent = objectMapper.readValue(kafkaEvent, UserDeletedEvent.class);

            String eventName = "users.deleted";
            UserDto eventData = userEvent.data();

            handleUserEvent(eventName, eventData);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @KafkaListener(topics = "discodeit.UserLogInOutEvent", groupId = "sse-${random.uuid}")
    public void onUserLogInOutEvent(String kafkaEvent) {
        try {
            UserLogInOutEvent event = objectMapper.readValue(kafkaEvent, UserLogInOutEvent.class);

            String eventName = "users.updated";
            UUID userId = event.userId();
            UserDto user = userService.findById(userId);

            handleUserEvent(eventName, user);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

    }

    private void handleUserEvent(String eventName, UserDto eventData) {
        sseService.broadcast(eventName, eventData);
    }
}

