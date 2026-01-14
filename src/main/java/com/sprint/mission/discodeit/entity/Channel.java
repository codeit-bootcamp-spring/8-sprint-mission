package com.sprint.mission.discodeit.entity;

import lombok.Getter;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
public class Channel implements Serializable {

  private static final long serialVersionUID = 1L;
  private final UUID id;
  private final Instant createdAt;
  private Instant updatedAt;

  private ChannelType type;
  private String name;
  private String description;

  public Channel(ChannelType type, String name, String description) {
    id = UUID.randomUUID();
    createdAt = Instant.now();
    updatedAt = createdAt;

    this.type = type;
    this.name = name;
    this.description = description;
  }

  public String getFileName() {
    return id.toString().concat(".ser");
  }

  public void update(String name, String description) {
    boolean anyValueUpdated = false;
    if (name != null && !name.equals(this.name)) {
      this.name = name;
      anyValueUpdated = true;
    }
    if (description != null && !description.equals(this.description)) {
      this.description = description;
      anyValueUpdated = true;
    }
    if (anyValueUpdated) {
      this.updatedAt = Instant.now();
    }
  }
}
