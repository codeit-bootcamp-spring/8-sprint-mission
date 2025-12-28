package com.sprint.mission.discodeit.dto;

import lombok.Getter;
import java.util.UUID;
import java.util.Set;
import com.sprint.mission.discodeit.entity.ChannelType; // 패키지 경로 확인 필요

@Getter
//  Lombok이 만드는 생성자는 기본적으로 public이지만,
// 직접 작성했다면 반드시 public을 붙여야 합니다.
public class ChannelResponse {
    private UUID id;
    private String name;
    private String description;
    private ChannelType type;
    private UUID ownerId;
    private Set<UUID> memberIds;

    // 만약 직접 생성자를 만드셨다면 아래처럼 public이 있어야 합니다.
    public ChannelResponse(UUID id, String name, String description,
                           ChannelType type, UUID ownerId, Set<UUID> memberIds) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.type = type;
        this.ownerId = ownerId;
        this.memberIds = memberIds;
    }
}