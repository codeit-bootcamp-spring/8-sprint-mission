package com.sprint.mission.discodeit.entity;


import com.sprint.mission.discodeit.entity.base.BaseUpdatableEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/*
    Message
   - 채팅 엔티티

    [필드 설명]
    • content          : 메시지 내용
    • channel          : 채널 객체
    • author           : 유저 객체
 */

@Entity
@Table(name = "messages")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Message extends BaseUpdatableEntity {

  @Column(name = "content", columnDefinition = "TEXT")
  private String content;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(
      name = "channel_id", nullable = false,
      foreignKey = @ForeignKey(foreignKeyDefinition = "FOREIGN KEY (channel_id) REFERENCES channels(id) ON DELETE CASCADE"))
  // 채널 삭제 시 메시지도 삭제
  private Channel channel;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(
      name = "author_id",
      foreignKey = @ForeignKey(foreignKeyDefinition = "FOREIGN KEY (author_id) REFERENCES users(id) ON DELETE SET NULL"))
  // 유저 삭제 시 작성자 정보 -> NULL 처리 (메시지는 유지)
  private User author;

  // 메시지에 첨부된 BinaryContent 객체들
  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
  @JoinTable(
      name = "message_attachments",
      joinColumns = @JoinColumn(
          name = "message_id",
          foreignKey = @ForeignKey(
              name = "fk_attachments_message",
              foreignKeyDefinition = "FOREIGN KEY (message_id) REFERENCES messages(id) ON DELETE CASCADE"
          )),
      inverseJoinColumns = @JoinColumn(
          name = "attachment_id",
          foreignKey = @ForeignKey(
              name = "fk_attachments_binary",
              foreignKeyDefinition = "FOREIGN KEY (attachment_id) REFERENCES binary_contents(id) ON DELETE CASCADE"
          ))
  )
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
