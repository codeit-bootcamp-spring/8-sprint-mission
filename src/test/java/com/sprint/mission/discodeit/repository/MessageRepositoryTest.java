package com.sprint.mission.discodeit.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserRole;
import jakarta.persistence.EntityManager;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
class MessageRepositoryTest {

  @Autowired
  private MessageRepository messageRepository;

  @Autowired
  private EntityManager em;

  private User persistUser(String username) {
    User u = new User(username, username + "@test.com", "pw", null, UserRole.USER);
    em.persist(u);
    em.flush();
    return u;
  }

  @Test
  @DisplayName("findFirstByChannel_IdOrderByCreatedAtDesc: 성공 - 최신 메시지 1건 조회")
  void findFirstByChannelId_latest_success() throws Exception {
    User user = persistUser("jun");
    Channel channel = new Channel(ChannelType.PUBLIC, "c", "d");
    em.persist(channel);

    em.persist(new Message(user, channel, "m1"));
    em.flush();

    Thread.sleep(5); // createdAt 차이를 강제로 만들어 정렬 안정화(테스트에서만)

    em.persist(new Message(user, channel, "m2"));
    em.flush();
    em.clear();

    Optional<Message> result =
        messageRepository.findFirstByChannel_IdOrderByCreatedAtDesc(channel.getId());

    assertThat(result).isPresent();
    assertThat(result.get().getContent()).isEqualTo("m2");
  }

  @Test
  @DisplayName("findFirstByChannel_IdOrderByCreatedAtDesc: 실패 - 메시지가 없으면 empty")
  void findFirstByChannelId_latest_fail() {
    Channel channel = new Channel(ChannelType.PUBLIC, "c", "d");
    em.persist(channel);
    em.flush();
    em.clear();

    Optional<Message> result =
        messageRepository.findFirstByChannel_IdOrderByCreatedAtDesc(channel.getId());

    assertThat(result).isEmpty();
  }

  @Test
  @DisplayName("findAllByChannel_Id(Pageable): 성공 - Slice 페이징/정렬 동작")
  void findAllByChannelId_slice_success() {
    User user = persistUser("jun");
    Channel channel = new Channel(ChannelType.PUBLIC, "c", "d");
    em.persist(channel);

    em.persist(new Message(user, channel, "m1"));
    em.persist(new Message(user, channel, "m2"));
    em.persist(new Message(user, channel, "m3"));
    em.flush();
    em.clear();

    PageRequest pageable = PageRequest.of(
        0, 2, Sort.by(Sort.Direction.DESC, "createdAt")
    );

    Slice<Message> slice = messageRepository.findAllByChannel_Id(channel.getId(), pageable);

    assertThat(slice.getContent()).hasSize(2);
    assertThat(slice.hasNext()).isTrue();
  }

  @Test
  @DisplayName("findAllByChannel_Id(Pageable): 실패(빈 결과) - 메시지 없으면 빈 Slice")
  void findAllByChannelId_slice_fail_empty() {
    Channel channel = new Channel(ChannelType.PUBLIC, "c", "d");
    em.persist(channel);
    em.flush();
    em.clear();

    PageRequest pageable = PageRequest.of(0, 2, Sort.by("createdAt").descending());

    Slice<Message> slice = messageRepository.findAllByChannel_Id(channel.getId(), pageable);

    assertThat(slice.getContent()).isEmpty();
    assertThat(slice.hasNext()).isFalse();
  }
}
