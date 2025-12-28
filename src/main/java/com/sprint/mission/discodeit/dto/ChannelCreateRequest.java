package com.sprint.mission.discodeit.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ChannelCreateRequest {
    private String name;
    private String description;
    private UUID ownerId;
    private List<UUID> memberIds; // PRIVATE 채널용 멤버 리스트
}