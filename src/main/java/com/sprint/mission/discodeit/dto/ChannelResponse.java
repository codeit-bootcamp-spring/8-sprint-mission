package com.sprint.mission.discodeit.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.Set;
import java.util.UUID;

@Getter
@Builder
public class ChannelResponse {
    private UUID id;
    private String name;
    private String type;
    private UUID ownerId;    // 이 필드가 추가되어야 합니다.
    private Set<UUID> memberIds;
}