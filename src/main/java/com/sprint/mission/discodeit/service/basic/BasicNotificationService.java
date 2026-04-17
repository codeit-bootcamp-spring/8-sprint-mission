package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.dto.NotificationDto;
import com.sprint.mission.discodeit.entity.Notification;
import com.sprint.mission.discodeit.exception.NotificationException.NotificationNotFoundException;
import com.sprint.mission.discodeit.exception.NotificationException.NotificationAccessDeniedException;
import com.sprint.mission.discodeit.mapper.NotificationMapper;
import com.sprint.mission.discodeit.repository.NotificationRepository;
import com.sprint.mission.discodeit.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

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
        if (receiverIds.isEmpty()) {
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
                        )).toList();
        notificationRepository.saveAll(notifications);
        log.info("새 알림 생성 완료했습니다. receiverIds: {}", receiverIds);
    }
}
