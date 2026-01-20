package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.User;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository {
    User save(User user);
    Optional<User> findById(UUID id);
    Optional<User> findByEmail(String email);  //  추가: 이메일로 유저를 조회하는 메서드
    List<User> findAll();
    void delete(UUID id);
    boolean existsByName(String name);
    boolean existsByEmail(String email);
}