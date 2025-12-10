package com.sprint.mission.discodeit.config;

import com.sprint.mission.discodeit.entity.*;

public record DemoData(
        User user,
        Channel channel,
        Message message
) {}