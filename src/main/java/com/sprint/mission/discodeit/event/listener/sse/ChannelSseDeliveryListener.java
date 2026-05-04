package com.sprint.mission.discodeit.event.listener.sse;

import com.sprint.mission.discodeit.dto.dto.ChannelDto;
import com.sprint.mission.discodeit.dto.dto.UserDto;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.event.Sse.Channel.ChannelCreatedEvent;
import com.sprint.mission.discodeit.event.Sse.Channel.ChannelDeletedEvent;
import com.sprint.mission.discodeit.event.Sse.Channel.ChannelUpdatedEvent;
import com.sprint.mission.discodeit.service.Sse.SseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
//@Component
@RequiredArgsConstructor
public class ChannelSseDeliveryListener {

    private final SseService sseService;

    @Async("asyncTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(ChannelCreatedEvent event) {
        ChannelDto channelDto = event.getData();
        handleChannelSse("channels.created", channelDto);
    }

    @Async("asyncTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(ChannelUpdatedEvent event) {
        ChannelDto channelDto = event.getTo();
        handleChannelSse("channels.updated", channelDto);
    }

    @Async("asyncTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(ChannelDeletedEvent event) {
        ChannelDto channelDto = event.getData();
        handleChannelSse("channels.deleted", channelDto);
    }

    private void handleChannelSse(String eventName, ChannelDto channelDto) {
        UUID channelId = channelDto.id();
        try {
            if (channelDto.type().equals(ChannelType.PUBLIC)) {
                sseService.broadcast(eventName, channelDto);
                log.debug("SSE Public 채널 이벤트 전송 성공. eventName: {} channelId: {}", eventName, channelId);
            } else {
                Set<UUID> receiverIds = channelDto.participants().stream()
                        .map(UserDto::id)
                        .collect(Collectors.toSet());
                sseService.send(receiverIds, eventName, channelDto);
                log.debug("SSE Private 채널 이벤트 전송 성공. eventName: {} channelId: {}", eventName, channelId);
            }
        } catch (Exception e) {
            log.error("SSE 채널 이벤트 전송 실패. eventName: {} channelId: {}", eventName, channelId);
        }
    }
}
