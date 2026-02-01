package com.sprint.mission.discodeit.entity;


import com.sprint.mission.discodeit.entity.base.BaseUpdatableEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
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
import org.hibernate.annotations.BatchSize;

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
  @JoinColumn(name = "channel_id", nullable = false)
  private Channel channel;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "author_id")
  private User author;

  // 메시지에 첨부된 BinaryContent 객체들
  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
  @JoinTable(
      name = "message_attachments",
      joinColumns = @JoinColumn(name = "message_id"),
      inverseJoinColumns = @JoinColumn(name = "attachment_id")
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
