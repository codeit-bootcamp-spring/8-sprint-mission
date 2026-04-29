package com.sprint.mission.discodeit.service.Sse;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Collection;
import java.util.UUID;

@Service
public interface SseService {

    SseEmitter connect(UUID receiver, UUID lastEventId);

    void send(Collection<UUID> receiverIds, String eventName, Object data);

    void broadcast(String eventName, Object data);

    void cleanUp();
}
