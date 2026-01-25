package com.sprint.mission.discodeit.entity;

import com.sprint.mission.discodeit.entity.base.BaseUpdatableEntity;
import jakarta.persistence.*;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor
public class User extends BaseUpdatableEntity {

  @Column(nullable = false, unique = true, length = 50)
  private String username;

  @Column(nullable = false, unique = true, length = 100)
  private String email;

  @Column(nullable = false, length = 60)
  private String password;

  // BinaryContent(profile)와의 One-to-One 관계 (0..1, optional)
  // schema.sql: ON DELETE SET NULL
  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "profile_id", unique = true)
  private BinaryContent profile;

  // UserStatus와의 One-to-One 양방향 관계
  // schema.sql: ON DELETE CASCADE
  // User가 삭제되면 UserStatus도 삭제되어야 하므로 cascade = ALL, orphanRemoval = true
  @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
  private UserStatus status;

  // 생성자
  public User(String username, String email, String password) {
    this.setId(UUID.randomUUID());
    this.username = username;
    this.email = email;
    this.password = password;
  }

  // update 메소드
  public void update(String username, String email, String password) {
    this.username = username;
    this.email = email;
    this.password = password;
  }

  // profileId 업데이트 메소드 (BinaryContent를 직접 설정)
  public void updateProfile(BinaryContent profile) {
    this.profile = profile;
  }
}
