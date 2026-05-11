package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.response.NotificationDto;
import com.sprint.mission.discodeit.entity.Notification;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.notification.NotificationForbiddenException;
import com.sprint.mission.discodeit.exception.notification.NotificationNotFoundException;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.repository.NotificationRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.NotificationService;
import com.sprint.mission.discodeit.service.sse.SseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class BasicNotificationService implements NotificationService {
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final CacheManager cacheManager;
    private final SseService sseService;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void create(Set<UUID> receiverIds, String title, String content) {
        if (receiverIds.isEmpty()) {
            return;
        }
        log.debug("새 알림 생성 시작: receiverIds={}", receiverIds);
        List<Notification> notifications = receiverIds.stream()
                .map(receiverId -> new Notification(receiverId, title, content))
                .toList();

        notificationRepository.saveAll(notifications);

        Cache cache = cacheManager.getCache("notifications");
        if (cache != null) {
            for (UUID receiverId : receiverIds) {
                cache.evict(receiverId);
            }
        }

        // SSE 이벤트 발송: 알림 생성 (수신자들에게만 발송)
        for (Notification notification : notifications) {
            NotificationDto dto = NotificationDto.from(notification);
            sseService.send(Collections.singleton(notification.getReceiverId()), "notifications.created", dto);
        }

        log.info("새 알림 생성 완료: receiverIds={}", receiverIds);
    }

    @Cacheable(value = "notifications", key = "#receiverId", unless = "#result.isEmpty()")
    @PreAuthorize("principal.userDto.id == #receiverId")
    @Override
    public List<NotificationDto> getNotifications(UUID receiverId) {
        return notificationRepository.findAllByReceiverId(receiverId).stream()
                .map(NotificationDto::from)
                .toList();
    }

    @CacheEvict(value = "notifications", key = "#receiverId")
    @PreAuthorize("principal.userDto.id == #receiverId")
    @Transactional
    @Override
    public void deleteNotification(UUID notificationId, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> UserNotFoundException.withUsername(username));

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new NotificationNotFoundException());

        if (!notification.getReceiverId().equals(user.getId())) {
            throw new NotificationForbiddenException();
        }

        notificationRepository.delete(notification);

        Cache cache = cacheManager.getCache("notifications");
        if (cache != null) {
            cache.evict(user.getId());
        }
    }
}