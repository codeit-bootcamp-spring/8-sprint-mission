package com.sprint.mission.discodeit.user;

import static org.assertj.core.api.Assertions.assertThat;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@DataJpaTest
@ActiveProfiles("test")
@EnableJpaAuditing
@EntityScan(basePackages = "com.sprint.mission.discodeit.entity")
@EnableJpaRepositories(basePackages = "com.sprint.mission.discodeit.repository")
@DisplayName("UserRepository 테스트")
class UserRepositoryTest {

  @Autowired
  private UserRepository userRepository;

  @Nested
  @DisplayName("save / findById")
  class SaveAndFind {

    @Test
    @DisplayName("저장 후 조회 시 동일 엔티티 반환")
    void saveAndFindById() {
      User user = new User("user1", "a@b.com", "password", null);
      User saved = userRepository.save(user);

      assertThat(saved.getId()).isNotNull();
      Optional<User> found = userRepository.findById(saved.getId());
      assertThat(found).isPresent();
      assertThat(found.get().getUsername()).isEqualTo("user1");
      assertThat(found.get().getEmail()).isEqualTo("a@b.com");
    }
  }

  @Nested
  @DisplayName("existsByEmail / existsByUsername")
  class Exists {

    @Test
    @DisplayName("이메일 존재 여부")
    void existsByEmail() {
      User user = new User("u1", "email@test.com", "p", null);
      userRepository.save(user);

      assertThat(userRepository.existsByEmail("email@test.com")).isTrue();
      assertThat(userRepository.existsByEmail("other@test.com")).isFalse();
    }

    @Test
    @DisplayName("사용자명 존재 여부")
    void existsByUsername() {
      User user = new User("uniqueName", "e@e.com", "p", null);
      userRepository.save(user);

      assertThat(userRepository.existsByUsername("uniqueName")).isTrue();
      assertThat(userRepository.existsByUsername("other")).isFalse();
    }
  }

  @Nested
  @DisplayName("findByUsername")
  class FindByUsername {

    @Test
    @DisplayName("사용자명으로 조회")
    void findByUsername() {
      User user = new User("findMe", "f@f.com", "p", null);
      userRepository.save(user);

      Optional<User> found = userRepository.findByUsername("findMe");
      assertThat(found).isPresent();
      assertThat(found.get().getEmail()).isEqualTo("f@f.com");
    }
  }
}
