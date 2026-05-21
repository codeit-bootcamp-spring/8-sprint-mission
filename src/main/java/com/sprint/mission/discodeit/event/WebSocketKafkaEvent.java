package com.sprint.mission.discodeit.event;

public record WebSocketKafkaEvent(
    String destination,
    Object payload
) {

}
