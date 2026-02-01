package com.sprint.mission.discodeit.entity;

import com.sprint.mission.discodeit.entity.base.BaseUpdatableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.io.Serializable;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "users", schema = "discodeit_user")
public class User extends BaseUpdatableEntity implements Serializable {

  private static final long serialVersionUID = 1L;

  @Column(
      name = "username",
      length = 50,
      nullable = false,
      unique = true
  )
  private String username;

  @Column(
      name = "email",
      length = 100,
      nullable = false,
      unique = true
  )
  private String email;

  @Column(
      name = "password",
      length = 60,
      nullable = false
  )
  private String password;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(
      name = "profile_id",
      unique = true
  )
  private BinaryContent profile;

  // 양방향 일대일 관계
  @OneToOne(mappedBy = "user", fetch = FetchType.LAZY)
  private UserStatus userStatus;

  public User(String username, String email, String password, BinaryContent profile) {
    this.username = username;
    this.email = email;
    this.password = password;
    this.profile = profile;
  }

  public void update(String newUsername, String newEmail, String newPassword,
      BinaryContent newProfile) {
    if (newUsername != null && !newUsername.equals(this.username)) {
      this.username = newUsername;
    }
    if (newEmail != null && !newEmail.equals(this.email)) {
      this.email = newEmail;
    }
    if (newPassword != null && !newPassword.equals(this.password)) {
      this.password = newPassword;
    }
    if (newProfile != null && !newProfile.equals(this.profile)) {
      this.profile = newProfile;
    }
  }
}
