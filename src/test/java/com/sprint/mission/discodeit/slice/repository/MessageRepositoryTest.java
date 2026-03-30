package com.sprint.mission.discodeit.slice.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.sprint.mission.discodeit.config.JpaConfig;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

@Import(JpaConfig.class)
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("MessageRepository 테스트")
public class MessageRepositoryTest {

  @Autowired
  private MessageRepository messageRepository;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private ChannelRepository channelRepository;

  private User author;
  private Channel channel;
  private Message firstMessage;
  private Message secondMessage;
  private Message thirdMessage;
  Instant now = Instant.now();

  @BeforeEach
  void setUp() {
    author = new User("testUser", "test@naver.com", "test1234", null);
    channel = new Channel(ChannelType.PUBLIC, "test Channel", "test description");
    firstMessage = new Message("first", channel, author, null);
    secondMessage = new Message("second", channel, author, null);
    thirdMessage = new Message("third", channel, author, null);

    userRepository.save(author);
    channelRepository.save(channel);

    ReflectionTestUtils.setField(firstMessage, "createdAt", now.minusSeconds(10));
    ReflectionTestUtils.setField(secondMessage, "createdAt", now.minusSeconds(5));
    ReflectionTestUtils.setField(thirdMessage, "createdAt", now);

    messageRepository.saveAllAndFlush(List.of(firstMessage, secondMessage, thirdMessage));
  }

  @Test
  @DisplayName("커서가 null일 때 최신 메시지부터 페이징 조회")
  void findAllByCursor_whenCursorNull() {

    // given
    Pageable pageable = PageRequest.of(0, 2);

    // when
    Slice<Message> result = messageRepository.findAllByChannelIdWithAuthor(channel.getId(),
        Instant.now().plusSeconds(1),
        pageable);

    // then
    assertThat(result.getContent()).hasSize(2);
    assertThat(result.hasNext()).isTrue();
  }

  @Test
  @DisplayName("커서가 존재할 경우 커서 기준 전 메시지 페이징 조회")
  void findAllByCursor_withCursor() {

    // given
    Pageable pageable = PageRequest.of(0, 10);
    Instant cursor = thirdMessage.getCreatedAt().plusSeconds(1);

    // when
    Slice<Message> result = messageRepository.findAllByChannelIdWithAuthor(channel.getId(),
        cursor,
        pageable);

    // then
    assertThat(result.getContent()).hasSize(3);
    assertThat(result.getContent()).extracting(Message::getContent)
        .containsExactlyInAnyOrder(thirdMessage.getContent(), secondMessage.getContent(),
            firstMessage.getContent());
    assertThat(result.hasNext()).isFalse();
  }

  @Test
  @DisplayName("존재하지 않는 채널로 조회하면 빈 결과를 반환")
  void findAllByCursor_InvalidChannel() {

    // when
    Slice<Message> result = messageRepository.findAllByChannelIdWithAuthor(
        UUID.randomUUID(), Instant.now(), PageRequest.of(0, 10));

    // then
    assertThat(result).isEmpty();
    assertThat(result.hasNext()).isFalse();
  }

  @Test
  @DisplayName("채널에 해당하는 메시지 전체 삭제")
  void deleteAllByChannelId() {

    // when
    messageRepository.deleteAllByChannelId(channel.getId());

    // then
    Optional<Message> message = messageRepository.findTopByChannelIdOrderByCreatedAtDesc(
        channel.getId());
    assertThat(message).isEmpty();
  }

  @Test
  @DisplayName("채널에 가장 최근에 생성된 메시지 하나 조회")
  void findTopByChannelIdOrderByCreatedAtDesc() {

    // given
    Message lastMessage = messageRepository.save(new Message("last", channel, author, null));

    // when
    Optional<Message> result = messageRepository.findTopByChannelIdOrderByCreatedAtDesc(
        channel.getId());

    // then
    assertThat(result).isPresent();
    assertThat(result.get().getContent()).isEqualTo(lastMessage.getContent());
  }

  @Test
  @DisplayName("채널에 메시지가 없을 경우 Optinal.empty 반환")
  void findTopByChannelIdOrderByCreatedAtDesc_ShouldReturnsEmpty() {

    // given
    messageRepository.deleteAllByChannelId(channel.getId());

    // when
    Optional<Message> result = messageRepository.findTopByChannelIdOrderByCreatedAtDesc(
        channel.getId());

    // then
    assertThat(result).isEmpty();
  }
}
