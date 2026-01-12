package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository {

  // 유저 생성
  User save(User user);

  // 유저 조회 (단건)
  Optional<User> findById(UUID id);

  // 유저 조회 (다건)
  List<User> findAll();

  // 존재 여부 확인
  boolean existsById(UUID id);

  // 유저 삭제
  void delete(UUID id);

  Optional<User> findByUsername(String username);

  Optional<User> findByEmail(String email);

  // newUsername 또는 newEmail 이 이미 사용 중인지 확인한다.
  boolean existsByUsernameOrEmail(String username, String email);
}
