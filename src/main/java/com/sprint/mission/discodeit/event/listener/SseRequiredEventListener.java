package com.sprint.mission.discodeit.event.listener;

import com.sprint.mission.discodeit.dto.data.BinaryContentDto;
import com.sprint.mission.discodeit.dto.data.ChannelDto;
import com.sprint.mission.discodeit.dto.data.NotificationDto;
import com.sprint.mission.discodeit.dto.data.UserDto;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.event.BinaryContentStatusUpdatedEvent;
import com.sprint.mission.discodeit.event.ChannelCreatedEvent;
import com.sprint.mission.discodeit.event.ChannelDeletedEvent;
import com.sprint.mission.discodeit.event.ChannelEvent;
import com.sprint.mission.discodeit.event.ChannelUpdatedEvent;
import com.sprint.mission.discodeit.event.NotificationCreatedEvent;
import com.sprint.mission.discodeit.event.UserCreatedEvent;
import com.sprint.mission.discodeit.event.UserDeletedEvent;
import com.sprint.mission.discodeit.event.UserEvent;
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
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@RequiredArgsConstructor
//@Component
public class SseRequiredEventListener {

  private final SseServiceInterface sseService;
  private final UserService userService;

  @Async("eventTaskExecutor")
  @TransactionalEventListener
  public void on(NotificationCreatedEvent event) {
    List<NotificationDto> notifications = event.getData();
    notifications.forEach(notification -> {
      UUID receiverId = notification.receiverId();
      sseService.send(Set.of(receiverId), "notifications.created", notification);
    });
  }

  @Async("eventTaskExecutor")
  @TransactionalEventListener
  public void on(BinaryContentStatusUpdatedEvent event) {
    BinaryContentDto binaryContent = event.getBinaryContent();
    sseService.broadcast("binaryContents.updated", binaryContent);
  }

  @Async("eventTaskExecutor")
  @TransactionalEventListener
  public void on(ChannelEvent channelEvent) {
    log.debug("Channel event received: {}", channelEvent);
    String eventName = null;
    ChannelDto eventData = null;

    if (channelEvent instanceof ChannelCreatedEvent createdEvent) {
      eventName = "channels.created";
      eventData = createdEvent.getData();
    } else if (channelEvent instanceof ChannelUpdatedEvent updatedEvent) {
      eventName = "channels.updated";
      eventData = updatedEvent.getTo();
    } else if (channelEvent instanceof ChannelDeletedEvent deletedEvent) {
      eventName = "channels.deleted";
      eventData = deletedEvent.getData();
    }
    if (eventName != null && eventData != null) {
      if (eventData.type().equals(ChannelType.PUBLIC)) {
        sseService.broadcast(eventName, eventData);
      } else {
        Set<UUID> receiverIds = eventData.participants().stream().map(UserDto::id)
            .collect(Collectors.toSet());
        sseService.send(receiverIds, eventName, eventData);
      }
    }
  }


  @Async("eventTaskExecutor")
  @TransactionalEventListener
  public void on(UserEvent userEvent) {
    log.debug("User event received: {}", userEvent);
    String eventName = null;
    UserDto eventData = null;

    if (userEvent instanceof UserCreatedEvent createdEvent) {
      eventName = "users.created";
      eventData = createdEvent.getData();
    } else if (userEvent instanceof UserUpdatedEvent updatedEvent) {
      eventName = "users.updated";
      eventData = updatedEvent.getTo();
    } else if (userEvent instanceof UserDeletedEvent deletedEvent) {
      eventName = "users.deleted";
      eventData = deletedEvent.getData();
    }

    if (eventName != null && eventData != null) {
      sseService.broadcast(eventName, eventData);
    }
  }

  @Async("eventTaskExecutor")
  @EventListener
  public void on(UserLogInOutEvent event) {
    String eventName = "users.updated";
    UUID userId = event.getUserId();
    UserDto user = userService.find(userId);
    sseService.broadcast(eventName, user);
  }
}
