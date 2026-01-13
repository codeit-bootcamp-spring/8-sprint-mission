package com.sprint.mission.discodeit.entity;


import com.sprint.mission.discodeit.entity.base.BaseUpdatableEntity;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;

/*
    Message
   - 채팅 엔티티

    [필드 설명]
    • author           : 유저 객체
    • channel          : 채널 객체
    • content          : 메시지 내용
 */

@Getter
public class Message extends BaseUpdatableEntity {

  // 메시지 내용
  private String content;

  private Channel channel;

  private User author;

  // 메시지에 첨부된 BinaryContent 객체들
  List<BinaryContent> attachments;

  public Message(User author, Channel channel, String content, List<BinaryContent> attachments) {
    this.author = author;
    this.channel = channel;
    this.content = content;
    this.attachments = attachments == null ? new ArrayList<>() : new ArrayList<>(attachments);
  }

  // 첨부파일 없는 경우
  public Message(User author, Channel channel, String content) {
    this(author, channel, content, new ArrayList<>());
  }

  public void update(String content) {
    if (content != null) {
      this.content = content;
    }
  }
}
