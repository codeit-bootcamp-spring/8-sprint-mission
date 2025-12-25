package com.sprint.mission.discodeit.dto;

import com.sprint.mission.discodeit.entity.ChannelType; // Enum 임포트
import lombok.Builder;
import lombok.Getter;

import java.util.Set;
import java.util.UUID;

@Getter
@Builder
public class ChannelResponse {
    private UUID id;
    private String name;
    private String description; //  에러 해결을 위해 반드시 추가 필요!
    private ChannelType type;   //  String 대신 Enum 사용 권장
    private UUID ownerId;
    private Set<UUID> memberIds;
}