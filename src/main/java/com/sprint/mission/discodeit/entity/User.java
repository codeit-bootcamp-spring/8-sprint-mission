package com.sprint.mission.discodeit.entity;

/*
    User
   - 유저 엔티티

    [필드 설명]
    • name           : 유저의 이름
    • email          : 유저의 이메일 주소
    • password       : 유저의 패스워드 (실제는 Hashing 해줘야 하지만, 학습용이라 평문 문자열로 설정하였다.)
    • profile        : 프로필 이미지 객체
 */

import com.sprint.mission.discodeit.entity.base.BaseUpdatableEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseUpdatableEntity {

  @Column(name = "username", nullable = false, unique = true, length = 50)
  private String username;

  @Column(name = "email", nullable = false, unique = true, length = 100)
  private String email;

  @Column(name = "password", nullable = false, length = 60)
  private String password;

  @Enumerated(EnumType.STRING)
  @Column(name = "role", nullable = false, length = 20)
  private UserRole role;

  // 프로필 이미지 1:1 단방향
  @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
  @JoinColumn(name = "profile_id", unique = true)
  private BinaryContent profile;

  // UserStatus와의 1:1 관계 반영
  @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
  private UserStatus status;

  public void attachStatus(UserStatus status) {
    this.status = status;
  }

  public User(String username, String email, String password, BinaryContent profile,
      UserRole role) {
    this.username = username;
    this.email = email;
    this.password = password;
    this.profile = profile;
    this.role = role;
  }

  public void update(String username, String email, String password, BinaryContent profile) {

    if (username != null) {
      this.username = username;
    }

    if (email != null) {
      this.email = email;
    }

    if (password != null) {
      this.password = password;
    }

    if (profile != this.profile) {
      this.profile = profile;
    }
  }

  // 권한 수정
  public void updateRole(UserRole newRole) {
    if (newRole != null) {
      this.role = newRole;
    }
  }
}
