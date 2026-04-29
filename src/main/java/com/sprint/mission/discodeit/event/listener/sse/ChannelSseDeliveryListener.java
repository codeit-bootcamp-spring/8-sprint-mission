package com.sprint.mission.discodeit.event.listener.sse;

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

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChannelSseDeliveryListener {

    private final SseService sseService;

    @Async("channelTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(ChannelCreatedEvent event) {
        UUID channelId = event.channelDto().id();
        try {
            if (event.receiverIds() == null || event.receiverIds().isEmpty()) {
                sseService.broadcast("channels.created", event.channelDto());
                log.debug("SSE Public 채널 생성 이벤트 전송 성공: channelId: {}", channelId);
            } else {
                sseService.send(event.receiverIds(), "channels.created", event.channelDto());
                log.debug("SSE Private 채널 생성 이벤트 전송 성공: channelId: {}", channelId);
            }
        } catch (Exception e) {
            log.debug("SSE 채널 생성 이벤트 전송 실패: channelId: {}", channelId);
        }
    }

    @Async("channelTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(ChannelUpdatedEvent event) {
        UUID channelId = event.channelDto().id();
        try {
            sseService.broadcast("channels.updated", event.channelDto());
            log.debug("SSE 채널 수정 이벤트 전송 성공: channelId: {}", channelId);
        } catch (Exception e) {
            log.debug("SSE 채널 수정 이벤트 전송 실패: channelId: {}", channelId);
        }
    }

    @Async("channelTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(ChannelDeletedEvent event) {
        UUID channelId = event.channelDto().id();
        try {
            sseService.broadcast("channels.updated", event.channelDto());
            log.debug("SSE 채널 삭제 이벤트 전송 성공: channelId: {}", channelId);
        } catch (Exception e) {
            log.debug("SSE 채널 삭제 이벤트 전송 실패: channelId: {}", channelId);
        }
    }
}
