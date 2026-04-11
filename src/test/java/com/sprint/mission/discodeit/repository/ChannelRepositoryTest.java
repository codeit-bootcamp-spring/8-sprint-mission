package com.sprint.mission.discodeit.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserRole;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
class ChannelRepositoryTest {

  @Autowired
  private ChannelRepository channelRepository;

  @Autowired
  private EntityManager em;

  private User persistUser(String username) {
    User u = new User(username, username + "@test.com", "testPass", null, UserRole.USER);
    em.persist(u);
    em.flush();
    return u;
  }

  @Test
  @DisplayName("findAllAccessibleByUserId: 성공 - PUBLIC + (ReadStatus 있는 PRIVATE)만 조회")
  void findAllAccessibleByUserId_success() {
    User user = persistUser("jun");

    Channel pub = new Channel(ChannelType.PUBLIC, "pub", "d");
    Channel priAllowed = new Channel(ChannelType.PRIVATE, "pri1", "d");
    Channel priDenied = new Channel(ChannelType.PRIVATE, "pri2", "d");

    em.persist(pub);
    em.persist(priAllowed);
    em.persist(priDenied);

    em.persist(new ReadStatus(user, priAllowed, Instant.now()));

    em.flush();
    em.clear();

    List<Channel> result = channelRepository.findAllAccessibleByUserId(user.getId());

    assertThat(result).extracting(Channel::getName)
        .contains("pub", "pri1")
        .doesNotContain("pri2");
  }

  @Test
  @DisplayName("findAllAccessibleByUserId: 실패(제한 케이스) - ReadStatus 없으면 PRIVATE는 안 나옴")
  void findAllAccessibleByUserId_fail_privateNotIncluded() {
    User user = persistUser("jun");

    Channel pub = new Channel(ChannelType.PUBLIC, "pub", "d");
    Channel pri = new Channel(ChannelType.PRIVATE, "pri", "d");

    em.persist(pub);
    em.persist(pri);

    em.flush();
    em.clear();

    List<Channel> result = channelRepository.findAllAccessibleByUserId(user.getId());

    assertThat(result).extracting(Channel::getName)
        .contains("pub")
        .doesNotContain("pri");
  }
}
