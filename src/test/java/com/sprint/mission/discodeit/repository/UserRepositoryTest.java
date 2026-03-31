package com.sprint.mission.discodeit.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserRole;
import jakarta.persistence.EntityManager;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryTest {

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private EntityManager em;

  private User persistUser(String username, String email) {
    BinaryContent profile = new BinaryContent("test.png", 10L, "image/png");
    User user = new User(username, email, "pw", profile, UserRole.USER);

    em.persist(user);
    em.flush();
    em.clear();
    return user;
  }

  @Test
  @DisplayName("findByUsername: 성공 - 존재하면 Optional에 담겨온다")
  void findByUsername_success() {
    persistUser("jun", "jun@test.com");

    Optional<User> result = userRepository.findByUsername("jun");

    assertThat(result).isPresent();
    assertThat(result.get().getUsername()).isEqualTo("jun");
  }

  @Test
  @DisplayName("findByUsername: 실패 - 없으면 Optional.empty")
  void findByUsername_fail() {
    Optional<User> result = userRepository.findByUsername("nope");
    assertThat(result).isEmpty();
  }

  @Test
  @DisplayName("existsByUsernameOrEmail: 성공 - username 또는 email 중 하나라도 있으면 true")
  void existsByUsernameOrEmail_success() {
    persistUser("jun", "jun@test.com");

    boolean exists1 = userRepository.existsByUsernameOrEmail("jun", "x@test.com");
    boolean exists2 = userRepository.existsByUsernameOrEmail("x", "jun@test.com");

    assertThat(exists1).isTrue();
    assertThat(exists2).isTrue();
  }

  @Test
  @DisplayName("existsByUsernameOrEmail: 실패 - 둘 다 없으면 false")
  void existsByUsernameOrEmail_fail() {
    boolean exists = userRepository.existsByUsernameOrEmail("nope", "nope@test.com");
    assertThat(exists).isFalse();
  }
}
