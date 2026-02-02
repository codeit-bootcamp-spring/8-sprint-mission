package com.sprint.mission.discodeit.entity;

import com.sprint.mission.discodeit.entity.base.BaseUpdatableEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Entity
@Table(name = "channels")
@Getter
@NoArgsConstructor
public class Channel extends BaseUpdatableEntity {

  @Column(nullable = false, length = 100)
  private String name;

  @Column(length = 500)
  private String description;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private ChannelType type;

  // User(owner/creator)와의 Many-to-One 관계
  // 다이어그램에 있지만 schema.sql에는 owner_id 컬럼이 없음
  // 다이어그램을 우선하여 추가 (nullable = true로 설정)
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "owner_id")
  private User owner;

  // 생성자
  public Channel(String name, String description, ChannelType type, User owner) {
    this.name = name;
    this.description = description;
    this.type = type;
    this.owner = owner;
  }

  // update 메소드
  public void update(String name, String description) {
    this.name = name;
    this.description = description;
  }

  // 헬퍼 메서드
  public UUID getOwnerId() {
    return owner != null ? owner.getId() : null;
  }
}
