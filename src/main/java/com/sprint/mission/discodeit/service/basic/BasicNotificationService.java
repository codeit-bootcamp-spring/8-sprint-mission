package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.dto.NotificationDto;
import com.sprint.mission.discodeit.entity.Notification;
import com.sprint.mission.discodeit.event.Sse.NotificationCreatedEvent;
import com.sprint.mission.discodeit.exception.NotificationException.NotificationAccessDeniedException;
import com.sprint.mission.discodeit.exception.NotificationException.NotificationNotFoundException;
import com.sprint.mission.discodeit.mapper.NotificationMapper;
import com.sprint.mission.discodeit.repository.NotificationRepository;
import com.sprint.mission.discodeit.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BasicNotificationService implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;
    private final CacheManager cacheManager;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Cacheable(value = "notifications", key = "#receiverId")
    @PreAuthorize("principal.userDto.id == #receiverId")
    public List<NotificationDto> findAllByReceiver(UUID receiverId) {
        return notificationRepository.findAllByReceiverIdOrderByCreatedAtDesc(receiverId)
                .stream()
                .map(notificationMapper::toDto)
                .toList();
    }


    @Override
    @CacheEvict(value = "notifications", key = "#receiverId")
    @PreAuthorize("principal.userDto.id == #receiverId")
    @Transactional
    public void confirmAndDelete(UUID notificationId, UUID receiverId) {
        log.info("[BasicNotificationService]  알림 확인 후 삭제 로직 시작 - ID: {}", notificationId);

        // 알림이 없는 경우 404 ErrorResponse 
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new NotificationNotFoundException(notificationId));

        // 인가되지 않은 요청 403 ErrorResponse
        if (!notification.getReceiverId().equals(receiverId)) {
            log.warn("[BasicNotificationService] 권한 없는 알림 삭제 시도 - 알림 ID: {}, 요청자 ID: {}", notificationId, receiverId);
            throw new NotificationAccessDeniedException(notificationId);
        }

        notificationRepository.delete(notification);
        log.info("[BasicNotificationService] 알림 삭제 완료 - ID: {}", notificationId);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void create(Set<UUID> receiverIds, String title, String content) {
        if (receiverIds == null || receiverIds.isEmpty()) {
            log.warn("알림 생성 요청이 비어있습니다. receiverIds: {}", receiverIds);
            return;
        }
        log.info("새 알림 생성 시작합니다. receiverIds: {}", receiverIds);
        List<Notification> notifications = receiverIds.stream()
                .map(receiverId ->
                        new Notification(
                                receiverId,
                                title,
                                content
                        )
                ).toList();
        List<Notification> savedNotifications = notificationRepository.saveAll(notifications);
        evictNotificationCache(receiverIds);

        List<NotificationDto> notificationDtos = savedNotifications.stream()
                .map(notificationMapper::toDto)
                .toList();

        eventPublisher.publishEvent(new NotificationCreatedEvent(notificationDtos, Instant.now()));

        log.info("새 알림 생성 완료했습니다. receiverIds: {}", receiverIds);
    }

    // 기존 캐시를 삭제하여 다음번에 findAllByReceiver로 조회했을 경우 무조건 캐시 미스가 나게 해야 함.
    private void evictNotificationCache(Set<UUID> receiverIds) {
        Cache cache = cacheManager.getCache("notifications");
        if (cache != null) {
            for (UUID receiverId : receiverIds) {
                cache.evict(receiverId);
                log.debug("알림 캐시를 제거했습니다. receiverId: {}", receiverId);
            }
        } else {
            log.warn("알림 캐시가 존재하지 않습니다.");
        }
    }
}
