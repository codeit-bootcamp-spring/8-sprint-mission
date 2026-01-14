package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.User;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/*
  • findByUsername                  : 이름으로 유저 찾기 (쿼리 메서드)
  • findByEmail                     : 이메일로 유저 찾기 (쿼리 메서드)
  • existsByUsernameOrEmail         : 이름 or 이메일 중복 확인 (쿼리 메서드)
 */
public interface UserRepository extends JpaRepository<User, UUID> {

  Optional<User> findByUsername(String username);

  Optional<User> findByEmail(String email);

  boolean existsByUsernameOrEmail(String username, String email);
}
