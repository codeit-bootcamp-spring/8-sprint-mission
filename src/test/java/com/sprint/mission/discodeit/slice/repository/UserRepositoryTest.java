package com.sprint.mission.discodeit.slice.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.sprint.mission.discodeit.config.JpaConfig;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@Import(JpaConfig.class)
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("UserRepository 테스트")
public class UserRepositoryTest {

  @Autowired
  private UserRepository userRepository;

  private User savedUser;

  @BeforeEach
  void setUp() {
    User user = new User("testUser", "test@naver.com", "test1234", null);
    savedUser = userRepository.save(user);
  }

  @Test
  @DisplayName("사용자 이름으로 조회 시 정상 동작")
  void findByUsername() {

    // when
    Optional<User> result = userRepository.findByUsername("testUser");

    // then
    assertThat(result).isPresent();
    assertThat(result.get().getUsername()).isEqualTo("testUser");
    assertThat(result.get().getEmail()).isEqualTo("test@naver.com");
  }

  @Test
  @DisplayName("존재하지 않는 사용자 이름 조회 시 Optional.empty() 반환")
  void findByUsername_NonExistentUsername_ReturnsEmpty() {

    // when
    Optional<User> result = userRepository.findByUsername("hahahatestUser");

    // then
    assertThat(result).isEmpty();
  }

  @Test
  @DisplayName("사용자 이름 중복 여부 확인이 정상적으로 동작")
  void existsByUsername() {

    // when
    boolean exists = userRepository.existsByUsername("testUser");
    boolean nonExists = userRepository.existsByUsername("hahahatestUser");

    // then
    assertThat(exists).isTrue();
    assertThat(nonExists).isFalse();
  }

  @Test
  @DisplayName("이메일 중복 여부 확인이 정상적으로 동작")
  void existsByEmail() {

    // when
    boolean exists = userRepository.existsByEmail("test@naver.com");
    boolean nonExists = userRepository.existsByUsername("hahahatestUser@naver.com");

    // then
    assertThat(exists).isTrue();
    assertThat(nonExists).isFalse();
  }

  @Test
  @DisplayName("Fetch Join을 사용하여 프로필과 상태를 포함한 전체 목록을 조회")
  void findAllWithProfileAndStatus() {

    // given
    User user2 = new User("testUser2", "test2@naver.com", "test1234", null);
    User user3 = new User("testUser3", "test3@naver.com", "test1234", null);
    userRepository.save(user2);
    userRepository.save(user3);

    // when
    List<User> result = userRepository.findAllWithProfile();

    // then
    assertThat(result.size()).isEqualTo(3);
    assertThat(result).extracting(User::getUsername)
        .containsExactlyInAnyOrder("testUser", "testUser2", "testUser3");
  }

  @Test
  @DisplayName("데이터가 없을 경우 빈 리스트 반환")
  void findAllWithProfileAndStatus_ShouldReturnEmptyList() {

    // given
    userRepository.deleteAll();

    // when
    List<User> result = userRepository.findAllWithProfile();

    // then
    assertThat(result).isEmpty();
    assertThat(result).isNotNull();
  }
}