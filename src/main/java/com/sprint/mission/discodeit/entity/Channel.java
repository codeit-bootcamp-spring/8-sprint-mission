package com.sprint.mission.discodeit.entity;

/*
    Channel
   - 채널 엔티티

    [필드 설명]
    • type             : 채널 타입
    • name             : 채널 이름
    • description      : 채널 설명

    [메서드]
    • update(String name, String description)    : 채널 이름과 채널 설명 갱신하고, updateCall()로 updatedAt 수정.
 */

import lombok.Getter;

import java.io.Serial;
import java.io.Serializable;

@Getter
public class Channel extends BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    // 채널 타입
    private ChannelType type;

    // 채널 이름
    private String name;

    // 채널 설명
    private String description;

    public Channel(ChannelType type, String name, String description) {
        this.type = type;
        this.name = name;
        this.description = description;
    }

    public void update(String newName, String newDescription) {

        boolean changed = false;

        if (newName != null && !newName.equals(this.name)) {
            this.name = newName;
            changed = true;
        }

        if (newDescription != null && !newDescription.equals(this.description)) {
            this.description = newDescription;
            changed = true;
        }

        if (changed) {
            updateCall();
        }
    }
}
