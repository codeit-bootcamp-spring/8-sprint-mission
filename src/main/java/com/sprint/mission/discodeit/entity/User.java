package com.sprint.mission.discodeit.entity;

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
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class User extends BaseUpdatableEntity {

  @NotBlank
  @Size(min = 2, max = 50)
  @Column(name = "username", length = 50, nullable = false, unique = true)
  private String username;

  @NotBlank
  @Email
  @Size(max = 100)
  @Column(name = "email", length = 100, nullable = false, unique = true)
  private String email;

  @NotBlank
  @Size(max = 60)
  @Column(name = "password", length = 60, nullable = false)
  private String password;

  @OneToOne(fetch = FetchType.LAZY, orphanRemoval = true, cascade = CascadeType.ALL)
  @JoinColumn(name = "profile_id")
  private BinaryContent profile;

  @Enumerated(EnumType.STRING)
  @Column(name = "role", nullable = false)
  private Role role;

  public User(String username, String email, String password, BinaryContent profile) {
    this.username = username;
    this.email = email;
    this.password = password;
    this.profile = profile;
    this.role = Role.USER;
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

    if (newProfile != null) {
      this.profile = newProfile;
    }
  }

  public void updateRole(Role role) {
    this.role = role;
  }
}
