package com.sprint.mission.discodeit.event.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.dto.UserDto;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.event.SseBroadcastMessage;
import com.sprint.mission.discodeit.event.UserLogInOutEvent;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.BinaryContentMapper;
import com.sprint.mission.discodeit.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserLoginOutEventListener {

  private final ObjectMapper objectMapper;
  private final ApplicationEventPublisher eventPublisher;
  private final UserRepository userRepository;
  private final BinaryContentMapper binaryContentMapper;

  @KafkaListener(topics = "discodeit.UserLogInOutEvent", groupId = "user-status-group")
  public void onUserLogInOut(String kafkaEvent) throws JsonProcessingException {
    UserLogInOutEvent event = objectMapper.readValue(kafkaEvent, UserLogInOutEvent.class);

    User user = userRepository.findById(event.userId())
        .orElseThrow(() -> new UserNotFoundException(event.userId()));

    UserDto userDto = new UserDto(
        user.getId(),
        user.getUsername(),
        user.getEmail(),
        binaryContentMapper.toDto(user.getProfile()),
        event.isLogin(),
        user.getRole()
    );

    eventPublisher.publishEvent(
        new SseBroadcastMessage("users.updated", userDto, null)
    );
  }
}
