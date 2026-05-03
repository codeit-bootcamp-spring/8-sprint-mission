package com.sprint.mission.discodeit.event;

import com.sprint.mission.discodeit.entity.Message;
import lombok.Getter;

@Getter
public class MessageCreatedEvent {

  private final Message message;
  private final String authorName;

  public MessageCreatedEvent(Message message) {
    this.message = message;
    this.authorName = message.getAuthor().getUsername();
  }
}
