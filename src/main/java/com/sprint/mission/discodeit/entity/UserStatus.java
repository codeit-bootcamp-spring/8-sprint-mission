package com.sprint.mission.discodeit.entity;

import lombok.Getter;

import java.io.Serial;
import java.io.Serializable;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

/*
    사용자 별로 마지막으로 접속한 시간을 나타낸다. -> 사용자의 온라인 상태를 확인하기 위해 활용

    [필드 설명]
    • userId               : 유저의 id
    • lastConnAt           : 마지막 접속 시간

    [메서드]
    • isOnline: 마지막 접속 시간이 현재 기준으로 5분 이내이면 접속 중인 유저로 간주하는 메서드

 */
@Getter
public class UserStatus extends BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final int ONLINE_VERIFICATION_MINUTES = 5;

    private UUID userId;

    private Instant lastConnAt;

    public UserStatus(UUID userId, Instant lastConnAt) {
        this.userId = userId;
        this.lastConnAt = lastConnAt;
    }

    public void update(Instant newLastConnAt) {
        if (newLastConnAt != null && !newLastConnAt.equals(this.lastConnAt)) {
            updateCall();
            this.lastConnAt = newLastConnAt;
        }
    }

    public boolean isOnline() {

        if (lastConnAt == null) {
            return false;
        }

        Instant now = Instant.now();
        return lastConnAt.isAfter(now.minus(Duration.ofMinutes(ONLINE_VERIFICATION_MINUTES)));
    }
}
