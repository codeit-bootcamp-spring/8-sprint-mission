package com.sprint.mission.discodeit.message;

import static org.assertj.core.api.Assertions.assertThat;

import com.sprint.mission.discodeit.DiscodeitApplication;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
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
@DisplayName("MessageRepository 슬라이스 테스트")
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
  @DisplayName("findById (JpaRepository)")
  class FindById {

    @Test
    @DisplayName("성공: 존재하는 ID로 조회 시 Message 반환")
    void success() {
      Channel channel = channelRepository.save(new Channel(ChannelType.PUBLIC, "채널", null));
      User author = userRepository.save(new User("author", "a@b.com", "p", null));
      userStatusRepository.save(new UserStatus(author, Instant.now()));
      Message message = messageRepository.save(new Message("내용", channel, author, List.of()));

      Optional<Message> found = messageRepository.findById(message.getId());
      assertThat(found).isPresent();
      assertThat(found.get().getContent()).isEqualTo("내용");
    }

    @Test
    @DisplayName("실패: 존재하지 않는 ID로 조회 시 Optional.empty")
    void fail_notFound() {
      Optional<Message> found = messageRepository.findById(UUID.randomUUID());
      assertThat(found).isEmpty();
    }
  }

  @Nested
  @DisplayName("findAllByChannelIdWithAuthor (커스텀 쿼리 + 페이징/정렬)")
  class FindAllByChannelIdWithAuthor {

    @Test
    @DisplayName("성공: 채널 ID·커서·Pageable로 슬라이스 조회")
    void success() {
      Channel channel = channelRepository.save(new Channel(ChannelType.PUBLIC, "채널", null));
      User author = userRepository.save(new User("u", "e@e.com", "p", null));
      userStatusRepository.save(new UserStatus(author, Instant.now()));
      messageRepository.save(new Message("메시지1", channel, author, List.of()));
      messageRepository.save(new Message("메시지2", channel, author, List.of()));

      Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
      Instant cursor = Instant.now().plusSeconds(60);
      Slice<Message> slice = messageRepository.findAllByChannelIdWithAuthor(
          channel.getId(), cursor, pageable);

      assertThat(slice.getContent()).hasSize(2);
      assertThat(slice.hasNext()).isFalse();
    }

    @Test
    @DisplayName("실패: 해당 채널에 메시지가 없으면 빈 Slice")
    void fail_empty() {
      Channel channel = channelRepository.save(new Channel(ChannelType.PUBLIC, "빈채널", null));
      Pageable pageable = PageRequest.of(0, 10);
      Slice<Message> slice = messageRepository.findAllByChannelIdWithAuthor(
          channel.getId(), Instant.now().plusSeconds(60), pageable);

      assertThat(slice.getContent()).isEmpty();
    }
  }

  @Nested
  @DisplayName("findLastMessageAtByChannelId (커스텀 쿼리)")
  class FindLastMessageAtByChannelId {

    @Test
    @DisplayName("성공: 채널의 마지막 메시지 createdAt 반환")
    void success() {
      Channel channel = channelRepository.save(new Channel(ChannelType.PUBLIC, "채널", null));
      User author = userRepository.save(new User("u", "e@e.com", "p", null));
      userStatusRepository.save(new UserStatus(author, Instant.now()));
      messageRepository.save(new Message("m1", channel, author, List.of()));

      Optional<Instant> lastAt = messageRepository.findLastMessageAtByChannelId(channel.getId());
      assertThat(lastAt).isPresent();
    }

    @Test
    @DisplayName("실패: 채널에 메시지가 없으면 Optional.empty")
    void fail_noMessage() {
      Channel channel = channelRepository.save(new Channel(ChannelType.PUBLIC, "빈채널", null));

      Optional<Instant> lastAt = messageRepository.findLastMessageAtByChannelId(channel.getId());
      assertThat(lastAt).isEmpty();
    }
  }

  @Nested
  @DisplayName("deleteAllByChannelId (커스텀 메소드)")
  class DeleteAllByChannelId {

    @Test
    @DisplayName("성공: 해당 채널 메시지 일괄 삭제")
    void success() {
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
