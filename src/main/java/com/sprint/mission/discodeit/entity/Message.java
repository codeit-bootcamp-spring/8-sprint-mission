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
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "messages")
public class Message extends BaseUpdatableEntity {

  @Column(
      name = "content",
      nullable = false
  )
  private String content;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(
      name = "channel_id",
      nullable = false
  )
  private Channel channel;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(
      name = "author_id",
      nullable = false
  )
  private User author;

  @OneToMany(
      fetch = FetchType.LAZY,
      cascade = CascadeType.ALL,
      orphanRemoval = true
  )
  @JoinTable(
      name = "message_attachments",
      joinColumns = @JoinColumn(name = "message_id"),
      inverseJoinColumns = @JoinColumn(name = "attachment_id")
  )
  private List<BinaryContent> attachments = new ArrayList<>();

  public Message(String content, Channel channel, User author, List<BinaryContent> attachments) {
    this.content = content;
    this.channel = channel;
    this.author = author;
    if (attachments != null) {
      this.attachments = attachments;
    }
  }

  public void update(String newContent) {
    if (newContent != null && !this.content.equals(newContent)) {
      this.content = newContent;
    }
  }
}
