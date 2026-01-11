package com.sprint.mission.discodeit.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChannelCreateRequest {
    private String name;
    private String description;
    private UUID ownerId;
    
    @JsonProperty("participantIds") // API 스펙에 맞춰 participantIds로 받음
    private List<UUID> participantIds; // PRIVATE 채널용 참여자 리스트
    
    // 내부적으로 memberIds로 접근 (기존 코드 호환성)
    public List<UUID> getMemberIds() {
        return participantIds;
    }
}