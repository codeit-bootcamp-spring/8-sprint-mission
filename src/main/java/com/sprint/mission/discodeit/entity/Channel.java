package com.sprint.mission.discodeit.entity;

/*
    Channel
   - 채널 엔티티

    [필드 설명]
    • name             : 채널 이름 (수정 O)
    • description      : 채널 설명 (수정 O)

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

    // 채널 이름
    private String name;

    // 채널 설명
    private String description;

    public Channel(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public void update(String name, String description) {
        this.name = name;
        this.description = description;
        updateCall();
    }

    @Override
    public String toString() {
        return "{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
