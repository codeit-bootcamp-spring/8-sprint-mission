package com.sprint.mission.discodeit.entity;

import com.sprint.mission.discodeit.entity.base.BaseUpdatableEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "users")
public class User extends BaseUpdatableEntity {

  @Size(max = 50, message = "유저 이름은 50자를 초과할 수 없습니다.")
  @Column(
      name = "username",
      length = 50,
      nullable = false,
      unique = true
  )
  private String username;

  @Email(message = "올바른 이메일 형식이 아닙니다.")
  @Size(max = 100, message = "이메일은 100자를 초과할 수 없습니다.")
  @Column(
      name = "email",
      length = 100,
      nullable = false,
      unique = true
  )
  private String email;

  @Size(max = 60, message = "비밀번호는 60자를 초과할 수 없습니다.")
  @Column(
      name = "password",
      length = 60,
      nullable = false
  )
  private String password;

  @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
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
