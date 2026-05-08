package com.sprint.mission.discodeit.event;

import lombok.Getter;

import java.time.Instant;

@Getter
public abstract class DeletedEvent<T> {

    private final T data;
    private final Instant deleteAt;

    protected DeletedEvent(final T data, final Instant deleteAt) {
        this.data = data;
        this.deleteAt = deleteAt;
    }
}
