package com.sprint.mission.discodeit.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import com.sprint.mission.discodeit.entity.ChannelType;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class ChannelDto {
    private UUID id;
    private ChannelType type;
    private String name;
    private String description;
    private List<UserDto> participants;
    private Instant lastMessageAt;
}
