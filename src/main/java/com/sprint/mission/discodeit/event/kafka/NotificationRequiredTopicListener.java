package com.sprint.mission.discodeit.event.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.event.MessageCreatedEvent;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Component
public class NotificationRequiredTopicListener {

  private final ObjectMapper objectMapper;
  private final NotificationService notificationService;
  private final ReadStatusRepository readStatusRepository;
  private final UserRepository userRepository;

  @KafkaListener(topics = "discodeit.MessageCreatedEvent")
  public void onMessageCreatedEvent(String kafkaEvent) {
    try {
      MessageCreatedEvent event = objectMapper.readValue(kafkaEvent, MessageCreatedEvent.class);
      UUID channelId = event.getMessage().getChannel().getId();
      UUID authorId = event.getMessage().getAuthor().getId();

      readStatusRepository.findAllByChannelIdAndNotificationEnabledTrue(channelId).stream()
          .map(rs -> rs.getUser().getId())
          .filter(receiverId -> !receiverId.equals(authorId))
          .forEach(receiverId -> {
            userRepository.findById(receiverId).ifPresent(receiver -> {
              notificationService.create(
                  receiver,
                  "새 메시지",
                  event.getMessage().getContent()
              );
            });
          });

      log.info("[Kafka Consumer] 메시지 알림 처리 완료");
    } catch (JsonProcessingException e) {
      log.error("수신 데이터 변환 에러", e);
    }
  }
}
