package com.sprint.mission.discodeit.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.sprint.mission.discodeit.config.JpaConfig;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
@Import(JpaConfig.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserRepositoryTest {

  @Autowired
  private UserRepository userRepository;
  @Autowired
  private TestEntityManager entityManager;

  @Nested
  @DisplayName("findByUsername 메서드 테스트")
  class FindByUsername {

    @Test
    @DisplayName("성공: 존재하는 username으로 조회 시 User 반환")
    void success() {
      //given
      userRepository.save(new User("tester", "test@test.com", "pw", null));

      //when
      var result = userRepository.findByUsername("tester");

      //then
      assertThat(result).isPresent();
    }

    @Test
    @DisplayName("실패: 존재하지 않는 username으로 조회 시 빈 Optional 반환")
    void fail() {
      // given (데이터가 없는 상태)

      // when
      var result = userRepository.findByUsername("none");

      // then
      assertThat(result).isEmpty();
    }
  }

  @Nested
  @DisplayName("existsByEmail 메서드 테스트")
  class ExistsByEmail {

    @Test
    @DisplayName("성공: 이미 존재하는 이메일이면 true 반환")
    void success() {
      // given
      userRepository.save(new User("u1", "exists@test.com", "pw", null));

      // when
      boolean exists = userRepository.existsByEmail("exists@test.com");

      // then
      assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("실패: 존재하지 않는 이메일이면 false 반환")
    void fail() {
      // when
      boolean exists = userRepository.existsByEmail("none@test.com");

      // then
      assertThat(exists).isFalse();
    }
  }

  @Nested
  @DisplayName("existsByUsername 메서드 테스트")
  class ExistsByUsername {

    @Test
    @DisplayName("성공: 이미 존재하는 username이면 true 반환")
    void success() {
      // given
      userRepository.save(new User("userA", "a@test.com", "pw", null));

      // when
      boolean exists = userRepository.existsByUsername("userA");

      // then
      assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("실패: 존재하지 않는 username이면 false 반환")
    void fail() {
      // when
      boolean exists = userRepository.existsByUsername("none");

      // then
      assertThat(exists).isFalse();
    }
  }

  @Nested
  @DisplayName("findAllWithProfileAndStatus (커스텀 쿼리) 테스트")
  class FindAllWithProfileAndStatus {

    @Test
    @DisplayName("성공: 프로필과 상태가 모두 있는 유저 조회")
    void success() {
      // given
      User user = userRepository.save(new User("tester", "test@test.com", "pw", null));
      UserStatus status = new UserStatus(user, java.time.Instant.now());
      entityManager.persist(status);

      entityManager.flush();
      entityManager.clear();

      // when
      List<User> result = userRepository.findAllWithProfileAndStatus();

      // then
      assertThat(result).isNotEmpty();
      assertThat(result.get(0).getUsername()).isEqualTo("tester");
    }

    @Test
    @DisplayName("실패: 상태(Status)가 없는 유저는 JOIN 조건에 의해 조회되지 않음")
    void fail() {
      // given
      userRepository.save(new User("no-status", "no@test.com", "pw", null));

      // when
      List<User> result = userRepository.findAllWithProfileAndStatus();

      // then
      assertThat(result).isEmpty();
    }
  }
}