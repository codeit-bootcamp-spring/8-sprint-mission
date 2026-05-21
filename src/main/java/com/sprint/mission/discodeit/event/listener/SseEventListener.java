package com.sprint.mission.discodeit.event.listener;


import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Notification;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.event.BinaryContentUpdatedEvent;
import com.sprint.mission.discodeit.event.ChannelEvent;
import com.sprint.mission.discodeit.event.ChannelEvent.ChannelEventType;
import com.sprint.mission.discodeit.event.NotificationCreatedEvent;
import com.sprint.mission.discodeit.event.UserEvent;
import com.sprint.mission.discodeit.event.UserEvent.UserEventType;
import com.sprint.mission.discodeit.exception.binarycontent.BinaryContentNotFoundException;
import com.sprint.mission.discodeit.exception.channel.ChannelNotFoundException;
import com.sprint.mission.discodeit.exception.notification.NotificationNotFoundException;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.jwt.JwtRegistry;
import com.sprint.mission.discodeit.mapper.BinaryContentMapper;
import com.sprint.mission.discodeit.mapper.ChannelMapper;
import com.sprint.mission.discodeit.mapper.NotificationMapper;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.NotificationRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.SseService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class SseEventListener {
  private final SseService sseService;
  private final NotificationRepository notificationRepository;
  private final NotificationMapper notificationMapper;
  private final BinaryContentRepository binaryContentRepository;
  private final BinaryContentMapper binaryContentMapper;
  private final ChannelRepository channelRepository;
  private final ChannelMapper channelMapper;
  private final UserRepository userRepository;
  private final UserMapper userMapper;
  private final JwtRegistry jwtRegistry;

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handleNotificationCreated(NotificationCreatedEvent event) {
    UUID notificationId = event.notificationId();
    UUID receiverId = event.receiverId();

    Notification notification = notificationRepository.findById(notificationId)
        .orElseThrow(()-> NotificationNotFoundException.withId(notificationId));

    sseService.send(
        List.of(receiverId),
        "notifications.created",
        notificationMapper.toDto(notification)
    );
  }

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handleBinaryContentUpdated(BinaryContentUpdatedEvent event){
    UUID binaryContentId = event.binaryContentId();
    UUID uploaderId = event.uploaderId();

    BinaryContent binaryContent = binaryContentRepository.findById(binaryContentId)
        .orElseThrow(()-> BinaryContentNotFoundException.withId(binaryContentId));

    sseService.send(
        List.of(uploaderId),
        "binaryContents.updated",
        binaryContentMapper.toDto(binaryContent)
    );
  }

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handleChannelEvent(ChannelEvent event){
    UUID channelId = event.channelId();
    ChannelEventType type = event.type();
    List<UUID> memberIds = event.memberIds();

    String eventName = switch (type) {
      case CREATED -> "channels.created";
      case UPDATED -> "channels.updated";
      case DELETED -> "channels.deleted";
    };

    if (type != ChannelEvent.ChannelEventType.DELETED) {
      Channel channel = channelRepository.findById(channelId)
          .orElseThrow(() -> ChannelNotFoundException.withId(channelId));

      sseService.send(memberIds, eventName, channelMapper.toDto(channel));
    } else {
      sseService.send(memberIds, eventName, channelId);
    }
  }

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handleUserEvent(UserEvent event) {
    UUID userId = event.userId();
    UserEventType eventType = event.type();

    String eventName = switch (eventType) {
      case CREATED -> "users.created";
      case UPDATED -> "users.updated";
      case DELETED -> "users.deleted";
    };

    if (eventType != UserEvent.UserEventType.DELETED) {
      User user = userRepository.findById(userId)
          .orElseThrow(() -> UserNotFoundException.withId(userId));

      boolean isOnline = jwtRegistry.hasActiveJwtInformationByUserId(userId);

      sseService.broadcast(eventName, userMapper.toDto(user,isOnline));
    } else {
      sseService.broadcast(eventName, userId);
    }

  }

}
