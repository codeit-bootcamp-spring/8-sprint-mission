package com.sprint.mission.discodeit.entity;


import lombok.Getter;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/*
    Message
   - 채팅 엔티티

    [필드 설명]
    • authorId           : 유저의 Id (채널 && 유저가 있어야 Message가 작성 가능하기에 유저의 Id 값 존재 해야함)
    • channelId          : 채널 Id (채널 && 유저가 있어야 Message가 작성 가능하기에 채널의 Id 값 존재 해야함)
    • newContent            : 메시지 내용

    [메서드]
    • update(String newContent)    : 메시지 내용을 갱신하고, updateCall()로 updatedAt 수정.
 */

@Getter
public class Message extends BaseEntity implements Serializable {

  @Serial
  private static final long serialVersionUID = 1L;

  // 작성자 Id
  private final UUID authorId;

  // 어느 채널인지 확인 여부
  private final UUID channelId;

  // 메시지 내용
  private String content;

  // 메시지에 첨부된 BinaryContent 의 userId 들.
  private List<UUID> attachmentIds;

  public Message(String content, UUID channelId, UUID authorId, List<UUID> attachmentIds) {
    this.content = content;
    this.channelId = channelId;
    this.authorId = authorId;
    this.attachmentIds = attachmentIds == null ? new ArrayList<>() : new ArrayList<>(attachmentIds);
  }

  public Message(String content, UUID channelId, UUID authorId) {
    this(content, channelId, authorId, List.of());
  }

  public void update(String contents) {
    this.content = contents;
    updateCall();
  }
}
