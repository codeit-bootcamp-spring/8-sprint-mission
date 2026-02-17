package com.sprint.mission.discodeit.message;

import static org.assertj.core.api.Assertions.assertThat;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import java.time.Instant;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
@EnableJpaAuditing
@EntityScan(basePackages = "com.sprint.mission.discodeit.entity")
@EnableJpaRepositories(basePackages = "com.sprint.mission.discodeit.repository")
@DisplayName("MessageRepository 테스트")
class MessageRepositoryTest {

  @Autowired
  private MessageRepository messageRepository;
  @Autowired
  private ChannelRepository channelRepository;
  @Autowired
  private UserRepository userRepository;
  @Autowired
  private UserStatusRepository userStatusRepository;

  @Nested
  @DisplayName("save / findById")
  class SaveAndFind {

    @Test
    @DisplayName("저장 후 조회 시 동일 엔티티 반환")
    void saveAndFindById() {
      Channel channel = channelRepository.save(new Channel(ChannelType.PUBLIC, "채널", null));
      User author = userRepository.save(new User("author", "a@b.com", "p", null));
      userStatusRepository.save(new UserStatus(author, Instant.now()));
      Message message = new Message("내용", channel, author, List.of());
      Message saved = messageRepository.save(message);

      assertThat(saved.getId()).isNotNull();
      Optional<Message> found = messageRepository.findById(saved.getId());
      assertThat(found).isPresent();
      assertThat(found.get().getContent()).isEqualTo("내용");
    }
  }

  @Nested
  @DisplayName("findAllByChannelIdWithAuthor")
  class FindAllByChannelIdWithAuthor {

    @Test
    @DisplayName("채널 ID와 커서로 페이지 조회")
    void findAllByChannelIdWithAuthor() {
      Channel channel = channelRepository.save(new Channel(ChannelType.PUBLIC, "채널", null));
      User author = userRepository.save(new User("u", "e@e.com", "p", null));
      userStatusRepository.save(new UserStatus(author, Instant.now()));
      messageRepository.save(new Message("메시지1", channel, author, List.of()));
      messageRepository.save(new Message("메시지2", channel, author, List.of()));

      Pageable pageable = PageRequest.of(0, 10);
      Instant cursor = Instant.now().plusSeconds(60);
      Slice<Message> slice = messageRepository.findAllByChannelIdWithAuthor(
          channel.getId(), cursor, pageable);

      assertThat(slice.getContent()).hasSize(2);
    }
  }

  @Nested
  @DisplayName("deleteAllByChannelId")
  class DeleteAllByChannelId {

    @Test
    @DisplayName("채널별 메시지 일괄 삭제")
    void deleteAllByChannelId() {
      Channel channel = channelRepository.save(new Channel(ChannelType.PUBLIC, "채널", null));
      User author = userRepository.save(new User("u", "e@e.com", "p", null));
      userStatusRepository.save(new UserStatus(author, Instant.now()));
      messageRepository.save(new Message("m1", channel, author, List.of()));

      assertThat(messageRepository.findAll()).hasSize(1);
      messageRepository.deleteAllByChannelId(channel.getId());
      assertThat(messageRepository.findAll()).isEmpty();
    }
  }
}
