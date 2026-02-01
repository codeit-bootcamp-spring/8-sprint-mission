package com.sprint.mission.discodeit.entity;

import com.sprint.mission.discodeit.entity.base.BaseUpdatableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.io.Serializable;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "channels", schema = "discodeit_user")
public class Channel extends BaseUpdatableEntity implements Serializable {

  private static final long serialVersionUID = 1L;

  @Column(
      name = "name",
      length = 100
  )
  private String name;

  @Column(
      name = "description",
      length = 500
  )
  private String description;

  @Enumerated(EnumType.STRING)
  @Column(
      name = "type",
      length = 10,
      nullable = false
  )
  private ChannelType type;

  public Channel(String name, String description, ChannelType type) {
    this.name = name;
    this.description = description;
    this.type = type;
  }

  public void update(String newName, String newDescription) {
    if (newName != null && !newName.equals(this.name)) {
      this.name = newName;
    }
    if (newDescription != null && !newDescription.equals(this.description)) {
      this.description = newDescription;
    }
  }
}
