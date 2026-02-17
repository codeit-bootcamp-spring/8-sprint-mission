package com.sprint.mission.discodeit.user;

import static org.assertj.core.api.Assertions.assertThat;

import com.sprint.mission.discodeit.DiscodeitApplication;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

@DataJpaTest
@ActiveProfiles("test")
@ContextConfiguration(classes = DiscodeitApplication.class)
@EnableJpaAuditing
@EntityScan(basePackages = "com.sprint.mission.discodeit.entity")
@EnableJpaRepositories(basePackages = "com.sprint.mission.discodeit.repository")
@DisplayName("UserRepository 슬라이스 테스트")
class UserRepositoryTest {

  @Autowired
  private UserRepository userRepository;
  @Autowired
  private UserStatusRepository userStatusRepository;

  @Nested
  @DisplayName("findById (JpaRepository)")
  class FindById {

    @Test
    @DisplayName("성공: 존재하는 ID로 조회 시 User 반환")
    void success() {
      User user = new User("user1", "a@b.com", "password", null);
      User saved = userRepository.save(user);

      Optional<User> found = userRepository.findById(saved.getId());
      assertThat(found).isPresent();
      assertThat(found.get().getUsername()).isEqualTo("user1");
    }

    @Test
    @DisplayName("실패: 존재하지 않는 ID로 조회 시 Optional.empty")
    void fail_notFound() {
      Optional<User> found = userRepository.findById(UUID.randomUUID());
      assertThat(found).isEmpty();
    }
  }

  @Nested
  @DisplayName("findByUsername (커스텀 쿼리 메소드)")
  class FindByUsername {

    @Test
    @DisplayName("성공: 존재하는 사용자명으로 조회 시 User 반환")
    void success() {
      User user = new User("findMe", "f@f.com", "p", null);
      userRepository.save(user);

      Optional<User> found = userRepository.findByUsername("findMe");
      assertThat(found).isPresent();
      assertThat(found.get().getEmail()).isEqualTo("f@f.com");
    }

    @Test
    @DisplayName("실패: 존재하지 않는 사용자명으로 조회 시 Optional.empty")
    void fail_notFound() {
      Optional<User> found = userRepository.findByUsername("nonexistent");
      assertThat(found).isEmpty();
    }
  }

  @Nested
  @DisplayName("existsByEmail / existsByUsername")
  class Exists {

    @Test
    @DisplayName("성공: 이메일 존재 시 true, 없으면 false")
    void existsByEmail() {
      User user = new User("u1", "email@test.com", "p", null);
      userRepository.save(user);

      assertThat(userRepository.existsByEmail("email@test.com")).isTrue();
      assertThat(userRepository.existsByEmail("other@test.com")).isFalse();
    }

    @Test
    @DisplayName("성공: 사용자명 존재 시 true, 없으면 false")
    void existsByUsername() {
      User user = new User("uniqueName", "e@e.com", "p", null);
      userRepository.save(user);

      assertThat(userRepository.existsByUsername("uniqueName")).isTrue();
      assertThat(userRepository.existsByUsername("other")).isFalse();
    }
  }

  @Nested
  @DisplayName("findAllWithProfileAndStatus (커스텀 @Query)")
  class FindAllWithProfileAndStatus {

    @Test
    @DisplayName("성공: status가 있는 User만 목록 반환")
    void success() {
      User user = new User("u1", "u1@e.com", "p", null);
      userRepository.save(user);
      userStatusRepository.save(new UserStatus(user, java.time.Instant.now()));

      List<User> list = userRepository.findAllWithProfileAndStatus();
      assertThat(list).hasSize(1);
      assertThat(list.get(0).getUsername()).isEqualTo("u1");
    }

    @Test
    @DisplayName("실패: status가 없는 User는 목록에 포함되지 않음 (빈 목록 또는 일부만)")
    void fail_noStatusExcluded() {
      User user = new User("noStatus", "n@e.com", "p", null);
      userRepository.save(user);
      // UserStatus 미생성

      List<User> list = userRepository.findAllWithProfileAndStatus();
      assertThat(list).isEmpty();
    }
  }
}
