package com.sprint.mission.discodeit.event;

import java.util.UUID;

public record UserLoginOutEvent(
        UUID userId,
        boolean isLogin
) {
}
