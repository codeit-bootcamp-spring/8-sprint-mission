package com.sprint.mission.discodeit.entity;

import com.sprint.mission.discodeit.entity.base.BaseUpdatableEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "messages")
@Getter
@NoArgsConstructor
public class Message extends BaseUpdatableEntity {

  @Column(columnDefinition = "TEXT", nullable = false)
  private String content;

  // Channel과의 Many-to-One 관계
  // schema.sql: ON DELETE CASCADE
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "channel_id", nullable = false)
  private Channel channel;

  // User(author)와의 Many-to-One 관계
  // schema.sql: ON DELETE SET NULL
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "author_id")
  private User author;

  // BinaryContent와의 Many-to-Many 관계 (attachments)
  // schema.sql의 message_attachments 조인 테이블 사용
  // ON DELETE CASCADE이므로 cascade = CascadeType.REMOVE
  @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
  @JoinTable(
      name = "message_attachments",
      joinColumns = @JoinColumn(name = "message_id"),
      inverseJoinColumns = @JoinColumn(name = "attachment_id")
  )
  private List<BinaryContent> attachments = new ArrayList<>();

  // 생성자
  public Message(String content, Channel channel, User author) {
    this.content = content;
    this.channel = channel;
    this.author = author;
  }

  // update 메소드
  public void update(String content) {
    this.content = content;
  }

  // update 메소드 (attachments 포함)
  public void update(String content, List<BinaryContent> attachments) {
    this.content = content;
    this.attachments.clear();
    if (attachments != null) {
      this.attachments.addAll(attachments);
    }
  }

  // 헬퍼 메서드들
  public UUID getChannelId() {
    return channel != null ? channel.getId() : null;
  }

  public UUID getAuthorId() {
    return author != null ? author.getId() : null;
  }

  public List<UUID> getAttachmentIds() {
    return attachments.stream()
        .map(BinaryContent::getId)
        .collect(java.util.stream.Collectors.toList());
  }
}
