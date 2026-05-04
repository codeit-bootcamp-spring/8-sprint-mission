package com.sprint.mission.discodeit.event.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.dto.data.BinaryContentDto;
import com.sprint.mission.discodeit.dto.data.ChannelDto;
import com.sprint.mission.discodeit.dto.data.NotificationDto;
import com.sprint.mission.discodeit.dto.data.UserDto;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.event.BinaryContentStatusUpdatedEvent;
import com.sprint.mission.discodeit.event.ChannelCreatedEvent;
import com.sprint.mission.discodeit.event.ChannelDeletedEvent;
import com.sprint.mission.discodeit.event.ChannelUpdatedEvent;
import com.sprint.mission.discodeit.event.NotificationCreatedEvent;
import com.sprint.mission.discodeit.event.UserCreatedEvent;
import com.sprint.mission.discodeit.event.UserDeletedEvent;
import com.sprint.mission.discodeit.event.UserLogInOutEvent;
import com.sprint.mission.discodeit.event.UserUpdatedEvent;
import com.sprint.mission.discodeit.service.UserService;
import com.sprint.mission.discodeit.sse.SseServiceInterface;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class SseRequiredTopicListener {

  private final SseServiceInterface sseService;
  private final UserService userService;
  private final ObjectMapper objectMapper;

  @KafkaListener(topics = "discodeit.NotificationCreatedEvent", groupId = "sse-${random.uuid}")
  public void onNotificationCreatedEvent(String kafkaEvent) {
    try {
      NotificationCreatedEvent event = objectMapper.readValue(kafkaEvent,
          NotificationCreatedEvent.class);

      List<NotificationDto> notifications = event.getData();
      notifications.forEach(notification -> {
        UUID receiverId = notification.receiverId();
        sseService.send(Set.of(receiverId), "notifications.created", notification);
      });
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }

  }

  @KafkaListener(topics = "discodeit.BinaryContentStatusUpdatedEvent", groupId = "sse-${random.uuid}")
  public void onBinaryContentStatusUpdatedEvent(String kafkaEvent) {
    try {
      BinaryContentStatusUpdatedEvent event = objectMapper.readValue(kafkaEvent,
          BinaryContentStatusUpdatedEvent.class);
      BinaryContentDto binaryContent = event.getBinaryContent();
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

      log.debug("Channel event received: {}", channelEvent);
      String eventName = "channels.created";
      ChannelDto eventData = channelEvent.getData();

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

      log.debug("Channel event received: {}", channelEvent);
      String eventName = "channels.updated";
      ChannelDto eventData = channelEvent.getTo();

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

      log.debug("Channel event received: {}", channelEvent);
      String eventName = "channels.deleted";
      ChannelDto eventData = channelEvent.getData();

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

      log.debug("User event received: {}", userEvent);
      String eventName = "users.created";
      UserDto eventData = userEvent.getData();

      handleUserEvent(eventName, eventData);

    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  @KafkaListener(topics = "discodeit.UserUpdatedEvent", groupId = "sse-${random.uuid}")
  public void onUserUpdatedEvent(String kafkaEvent) {
    try {
      UserUpdatedEvent userEvent = objectMapper.readValue(kafkaEvent, UserUpdatedEvent.class);

      log.debug("User event received: {}", userEvent);
      String eventName = "users.updated";
      UserDto eventData = userEvent.getTo();

      handleUserEvent(eventName, eventData);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  @KafkaListener(topics = "discodeit.UserDeletedEvent", groupId = "sse-${random.uuid}")
  public void onUserDeletedEvent(String kafkaEvent) {
    try {
      UserDeletedEvent userEvent = objectMapper.readValue(kafkaEvent, UserDeletedEvent.class);

      log.debug("User event received: {}", userEvent);
      String eventName = "users.deleted";
      UserDto eventData = userEvent.getData();

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
      UUID userId = event.getUserId();
      UserDto user = userService.find(userId);

      handleUserEvent(eventName, user);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }

  }

  private void handleUserEvent(String eventName, UserDto eventData) {
    sseService.broadcast(eventName, eventData);
  }
}
