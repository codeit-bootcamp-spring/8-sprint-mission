package com.sprint.mission.discodeit.entity;


import lombok.Getter;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

/*
    Message
   - 채팅 엔티티

    [필드 설명]
    • userId           : 유저의 Id (채널 && 유저가 있어야 Message가 작성 가능하기에 유저의 Id 값 존재 해야함)
    • channelId        : 채널 Id (채널 && 유저가 있어야 Message가 작성 가능하기에 채널의 Id 값 존재 해야함)
    • contents         : 메시지 내용

    [메서드]
    • update(String contents)    : 메시지 내용을 갱신하고, updateCall()로 updatedAt 수정.
 */

@Getter
public class Message extends BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    // user 아이디 -> 유저가 있어야 메시지를 작성할 수 있다.
    private final UUID userId;

    // 어느 채널인지 확인 여부
    private final UUID channelId;

    // 메시지 내용
    private String contents;

    public Message(UUID userId, UUID channelId, String contents) {
        super();
        this.userId = userId;
        this.channelId = channelId;
        this.contents = contents;
    }

    public void update(String contents) {
        this.contents = contents;
        updateCall();
    }

    @Override
    public String toString() {
        return "{" +
                "userId=" + userId +
                ", channelId=" + channelId +
                ", contents='" + contents + '\'' +
                '}';
    }
}
