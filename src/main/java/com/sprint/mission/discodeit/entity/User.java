package com.sprint.mission.discodeit.entity;

/*
    User
   - 유저 엔티티

    [필드 설명]
    • name           : 유저의 이름
    • email          : 유저의 이메일 주소
    • password       : 유저의 패스워드 (실제는 Hashing 해줘야 하지만, 학습용이라 평문 문자열로 설정하였다.)
    • profileId      : 프로필 이미지(BinaryContent)의 userId

    [메서드]
    • update(User user)   : User 객체의 값을 현재 객체에 반영 및 updateCall()로 updatedAt 수정.

 */

import lombok.Getter;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

@Getter
public class User extends BaseEntity implements Serializable {

  @Serial
  private static final long serialVersionUID = 1L;

  private String name;
  private String email;
  private String password;
  private UUID profileId;

  // 프로필 이미지 들어왔을 때
  public User(String name, String email, String password, UUID profileId) {
    this.name = name;
    this.email = email;
    this.password = password;
    this.profileId = profileId;
  }

  // 프로필 이미지 안 들어왔을 때
  public User(String name, String email, String password) {
    this(name, email, password, null);
  }

  public void update(String newName, String newEmail, String newPassword, UUID newProfileId) {

    boolean changeCheck = false;

    if (newName != null && !newName.equals(this.name)) {
      this.name = newName;
      changeCheck = true;
    }

    if (newEmail != null && !newEmail.equals(this.email)) {
      this.email = newEmail;
      changeCheck = true;
    }

    if (newPassword != null && !newPassword.equals(this.password)) {
      this.password = newPassword;
      changeCheck = true;
    }

    if (newProfileId != null && !newProfileId.equals(this.profileId)) {
      this.profileId = newProfileId;
      changeCheck = true;
    }

    if (changeCheck) {
      updateCall();
    }
  }
}
