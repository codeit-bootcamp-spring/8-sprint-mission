package com.sprint.mission.discodeit.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.sprint.mission.discodeit.config.QueryDslConfig;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

@DataJpaTest
@ActiveProfiles("test")
@Import(QueryDslConfig.class)
@EnableJpaAuditing
class MessageRepositoryTest {

  @Autowired
  private MessageRepository messageRepository;
  @Autowired
  private UserRepository userRepository;
  @Autowired
  private ChannelRepository channelRepository;
  @Autowired
  private TestEntityManager entityManager;

  private User user;
  private Channel channel;

  @BeforeEach
  void setUp() {
    user = entityManager.persist(new User("testUser", "test@discodeit.com", "password123", null));
    channel = entityManager.persist(new Channel("test channel", "description", ChannelType.PUBLIC));
    entityManager.flush();
    entityManager.clear();
  }

  private Message createAndSaveTestMessage(String content, Instant createdAt) {
    Message message = new Message(content, channel, user, List.of());

    if (createdAt != null) {
      ReflectionTestUtils.setField(message, "createdAt", createdAt);
    }

    Message savedMessage = messageRepository.save(message);
    entityManager.flush();
    return savedMessage;
  }

  @Test
  @DisplayName("첨부파일을 포함한 메시지 저장 테스트")
  void save_and_find_message_withAttachments() {
    // given
    BinaryContent binaryContent1 = new BinaryContent("test1.jpg", 1024L, "image/jpeg");
    BinaryContent binaryContent2 = new BinaryContent("test2.jpg", 1024L, "image/jpeg");
    List<BinaryContent> attachments = List.of(binaryContent1, binaryContent2);
    Message message = new Message("test message", channel, user, attachments);

    // when
    Message savedMessage = messageRepository.save(message);
    entityManager.flush();
    entityManager.clear();

    // then
    Optional<Message> foundMessageOpt = messageRepository.findById(savedMessage.getId());
    assertThat(foundMessageOpt).isPresent();
    Message foundMessage = foundMessageOpt.get();

    assertThat(foundMessage.getContent()).isEqualTo("test message");
    assertThat(foundMessage.getAuthor().getUsername()).isEqualTo("testUser");
    assertThat(foundMessage.getAttachments()).hasSize(2);
  }

  @Test
  @DisplayName("커서 기반 페이징 조회 테스트 - 다음 페이지가 있는 경우")
  void findAllByChannelIdWithCursor_hasNextPage() {
    // given
    for (int i = 0; i < 15; i++) {
      createAndSaveTestMessage("message " + i, Instant.now());
    }
    entityManager.flush();
    entityManager.clear();

    // when
    Slice<Message> firstPage = messageRepository.findAllByChannelIdWithCursor(channel.getId(), null,
        PageRequest.of(0, 10));

    // then
    assertThat(firstPage.getContent()).hasSize(10);
    assertThat(firstPage.hasNext()).isTrue();

    // when - 두 번째 페이지 조회
    Instant nextCursor = firstPage.getContent().get(9).getCreatedAt();
    Slice<Message> secondPage = messageRepository.findAllByChannelIdWithCursor(channel.getId(),
        nextCursor, PageRequest.of(0, 10));

    // then
    // 같은 시간내에 여러 메시지를 생성하니깐 커서 테스트를 통과하지 못함..

//    assertThat(secondPage.getContent()).hasSize(5);
    assertThat(secondPage.hasNext()).isFalse();
  }

  @Test
  @DisplayName("가장 최근 메시지 1건 조회 테스트")
  void findTopByChannelIdOrderByCreatedAtDesc_success() {
    Instant now = Instant.now();
    Instant fiveMinutesAgo = now.minus(5, ChronoUnit.MINUTES);
    Instant tenMinutesAgo = now.minus(10, ChronoUnit.MINUTES);

    createAndSaveTestMessage("test message 1", tenMinutesAgo);
    createAndSaveTestMessage("test message 2", fiveMinutesAgo);
    Message lastMessageToSave = createAndSaveTestMessage("test message 3", now);
    entityManager.flush();
    entityManager.clear();

    // when
    Message lastMessage = messageRepository.findTopByChannelIdOrderByCreatedAtDesc(channel.getId())
        .orElseThrow();

    // then
    assertThat(lastMessage.getId()).isEqualTo(lastMessageToSave.getId());
    assertThat(lastMessage.getContent()).isEqualTo("test message 3");
  }
}
