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
import lombok.Getter;

@Getter
public class User extends BaseUpdatableEntity {

  private String username;
  private String email;
  private String password;

  // UUID profileId -> 객체 참조로 변경
  private BinaryContent profile;

  // UserStatus와의 1:1 관계 반영
  private UserStatus status;

  public User(String username, String email, String password, BinaryContent profile) {
    this.username = username;
    this.email = email;
    this.password = password;
    this.profile = profile;
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
}
