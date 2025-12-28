package com.sprint.mission.discodeit.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ChannelUpdateRequest {
    private UUID id;       // 수정 대상 객체의 id
    private String name;   // 수정할 이름
    private String descrption;

    public String getDescription() {
        return "";
    }
}