package com.sprint.mission.discodeit.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.sprint.mission.discodeit.config.QueryDslConfig;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.hibernate.Hibernate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
@Import(QueryDslConfig.class)
@EnableJpaAuditing
class UserRepositoryTest {

  @Autowired
  private UserRepository userRepository;
  @Autowired
  private TestEntityManager entityManager;
  @Autowired
  private UserStatusRepository userStatusRepository;

  private User createTestUser(String username, String email) {
    BinaryContent profile = new BinaryContent("test.jpg", 1024L, "image/jpeg");
    User user = new User(username, email, "password1234", profile);
    UserStatus status = new UserStatus(user, Instant.now());

    return user;
  }

  @Test
  @DisplayName("사용자의 이름으로 조회")
  void findByUsername_ExistUsername_ReturnsUser() {
    // given
    String username = "test";
    String email = "test@codeit.com";
    User user = createTestUser(username, email);
    userRepository.save(user);

    entityManager.flush();
    entityManager.clear();

    // when
    Optional<User> foundUser = userRepository.findByUsername(username);

    // then
    assertThat(foundUser).isPresent();
    assertThat(foundUser.get().getUsername()).isEqualTo(username);
  }

  @Test
  @DisplayName("존재하지 않는 사용자의 이름으로 검색하면 빈 Optional을 반환")
  void findByUsername_NonExistUsername_ReturnsEmptyOptional() {
    // given
    String nonExistUsername = "nonExistUser";

    // when
    Optional<User> foundUser = userRepository.findByUsername(nonExistUsername);

    // then
    assertThat(foundUser).isEmpty();
  }

  @Test
  @DisplayName("이메일로 사용자 존재 여부를 확인할 수 있다")
  void existsByEmail_ExistEmail_ReturnsTrue() {
    // given
    String email = "test@codeit.com";
    User user = createTestUser("test", email);
    userRepository.save(user);

    // when
    boolean exists = userRepository.existsByEmail(email);

    // then
    assertThat(exists).isTrue();
  }

  @Test
  @DisplayName("존재하지 않는 이메일로 확인하면 false를 반환")
  void existsByEmail_NonExistEmail_ReturnsFalse() {
    // given
    String nonExistEmail = "nonExistEmail@codeit.com";

    // when
    boolean exists = userRepository.existsByEmail(nonExistEmail);

    // then
    assertThat(exists).isFalse();
  }

  @Test
  @DisplayName("모든 사용자를 조회할 때 userStatus와 함께 조회한다.")
  void findAll_withUserStatus() {
    // given
    User user1 = new User("user1", "user1@codeit.com", "password123", null);
    User user2 = new User("user2", "user2@codeit.com", "password123", null);
    userRepository.save(user1);
    userRepository.save(user2);

    userStatusRepository.save(new UserStatus(user1, Instant.now()));
    userStatusRepository.save(new UserStatus(user2, Instant.now()));

    entityManager.flush();
    entityManager.clear();

    // when
    List<User> users = userRepository.findAll();

    // then
    assertThat(users).hasSize(2);
    for (User user : users) {
      assertThat(Hibernate.isInitialized(user.getUserStatus())).isTrue();
      assertThat(user.getUserStatus().getUser().getId()).isEqualTo(user.getId());
    }
  }

  @Test
  @DisplayName("유저가 저장될 때 프로필도 함께 저장된다.")
  void save_user_with_profile() {
    // given
    BinaryContent profile = new BinaryContent("test.jpg", 1024L, "image/jpeg");
    User user = new User("test", "test@codeit.com", "password123", profile);

    // when
    userRepository.save(user);
    entityManager.flush();
    entityManager.clear();

    // then
    User foundUser = userRepository.findByUsername("test").orElseThrow();
    assertThat(foundUser.getProfile()).isNotNull();
    assertThat(foundUser.getProfile().getFileName()).isEqualTo("test.jpg");
    assertThat(foundUser.getProfile().getCreatedAt()).isNotNull();
  }
}
