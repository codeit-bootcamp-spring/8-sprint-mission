package com.sprint.mission.discodeit.event.listener;

import com.sprint.mission.discodeit.dto.user.UserResponse;
import com.sprint.mission.discodeit.event.UserLogInOutEvent;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.SseService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class UserLogInOutEventListener {

  private final SseService sseService;
  private final UserRepository userRepository;
  private final UserMapper userMapper;

  @Async("eventTaskExecutor")
  @EventListener
  @Transactional(readOnly = true)
  public void handleUserLogInOutEvent(UserLogInOutEvent event) {

    userRepository.findById(event.userId()).ifPresent(user -> {
      UserResponse response = userMapper.toUserResponse(user, event.isOnline());
      sseService.broadcast("users.updated", response);
    });
  }
}
