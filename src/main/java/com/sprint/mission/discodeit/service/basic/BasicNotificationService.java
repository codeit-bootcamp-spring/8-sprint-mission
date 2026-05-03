package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.cache.CacheNames;
import com.sprint.mission.discodeit.dto.data.NotificationDto;
import com.sprint.mission.discodeit.entity.Notification;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.mapper.NotificationMapper;
import com.sprint.mission.discodeit.repository.NotificationRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.security.DiscodeitUserDetails;
import com.sprint.mission.discodeit.service.NotificationService;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class BasicNotificationService implements NotificationService {

  private final NotificationRepository notificationRepository;
  private final UserRepository userRepository;
  private final NotificationMapper notificationMapper;

  @Override
  @Transactional(readOnly = true)
  @Cacheable(
      cacheNames = CacheNames.NOTIFICATIONS_BY_USER,
      key = "@cacheSpelKeys.userIdFrom(#authentication)"
  )
  public List<NotificationDto> findAllForCurrentUser(Authentication authentication) {
    UUID receiverId = currentUserId(authentication);
    return notificationRepository.findAllByReceiverForListing(receiverId).stream()
        .map(notificationMapper::toDto)
        .toList();
  }

  @Override
  @Transactional
  @CacheEvict(
      cacheNames = CacheNames.NOTIFICATIONS_BY_USER,
      key = "@cacheSpelKeys.userIdFrom(#authentication)"
  )
  public void delete(UUID notificationId, Authentication authentication) {
    UUID receiverId = currentUserId(authentication);
    Notification notification = notificationRepository.findById(notificationId)
        .orElseThrow(() -> new NoSuchElementException(
            "Notification with id " + notificationId + " not found"));
    if (!notification.getReceiver().getId().equals(receiverId)) {
      throw new AccessDeniedException("본인의 알림만 삭제할 수 있습니다.");
    }
    notificationRepository.delete(notification);
  }

  @Override
  @Transactional
  @CacheEvict(cacheNames = CacheNames.NOTIFICATIONS_BY_USER, key = "#receiverUserId")
  public void createForReceiver(UUID receiverUserId, String title, String content) {
    User receiver = userRepository.findById(receiverUserId).orElse(null);
    if (receiver == null) {
      return;
    }
    notificationRepository.save(new Notification(receiver, title, content));
  }

  private static UUID currentUserId(Authentication authentication) {
    if (authentication == null
        || !(authentication.getPrincipal() instanceof DiscodeitUserDetails details)) {
      throw new IllegalStateException("Unauthenticated");
    }
    return details.getUserDto().id();
  }
}
