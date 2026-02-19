package com.sprint.mission.discodeit.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.sprint.mission.discodeit.config.JpaConfig;
import com.sprint.mission.discodeit.entity.*;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
@Import(JpaConfig.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class MessageRepositoryTest {

  @Autowired
  private MessageRepository messageRepository;
  @Autowired
  private UserRepository userRepository;
  @Autowired
  private ChannelRepository channelRepository;
  @Autowired
  private TestEntityManager entityManager;

  @Nested
  @DisplayName("findAllByChannelIdWithAuthor (Slice 페이징) 테스트")
  class FindAllByChannelIdWithAuthor {

    @Test
    @DisplayName("성공: 특정 시간 이전의 메시지를 슬라이스 단위로 조회하고 다음 페이지 여부를 확인한다")
    void success() {
      // 1. 데이터 준비 (유저-상태-채널-메시지 2개)
      User author = userRepository.save(new User("writer", "w@test.com", "pw", null));
      entityManager.persist(new UserStatus(author, Instant.now()));
      Channel channel = channelRepository.save(new Channel(ChannelType.PUBLIC, "General", "Desc"));

      messageRepository.save(new Message("First Msg", channel, author, Collections.emptyList()));
      messageRepository.save(new Message("Second Msg", channel, author, Collections.emptyList()));

      // 2. 조회 설정 (사이즈를 1로 해서 다음 페이지가 있게 만듦)
      PageRequest pageable = PageRequest.of(0, 1, Sort.by("createdAt").descending());

      // when
      Slice<Message> result = messageRepository.findAllByChannelIdWithAuthor(
          channel.getId(), Instant.now().plusSeconds(10), pageable);

      // then
      assertThat(result.getContent()).hasSize(1);
      assertThat(result.hasNext()).isTrue(); // 데이터가 2개인데 사이즈 1로 불렀으니 true여야 함
    }

    @Test
    @DisplayName("실패: 메시지가 없는 채널 ID로 조회 시 빈 슬라이스를 반환한다")
    void fail() {
      // when
      Slice<Message> result = messageRepository.findAllByChannelIdWithAuthor(
          UUID.randomUUID(), Instant.now(), PageRequest.of(0, 10));

      // then
      assertThat(result.getContent()).isEmpty();
      assertThat(result.hasNext()).isFalse();
    }
  }

  @Nested
  @DisplayName("findLastMessageAtByChannelId 테스트")
  class FindLastMessageAtByChannelId {

    @Test
    @DisplayName("성공: 채널의 마지막 메시지 생성 시간을 조회한다")
    void success() {
      User author = userRepository.save(new User("writer2", "w2@test.com", "pw", null));
      entityManager.persist(new UserStatus(author, Instant.now()));
      Channel channel = channelRepository.save(new Channel(ChannelType.PUBLIC, "General", "Desc"));
      messageRepository.save(new Message("Latest", channel, author, Collections.emptyList()));

      Optional<Instant> lastTime = messageRepository.findLastMessageAtByChannelId(channel.getId());

      assertThat(lastTime).isPresent();
    }

    @Test
    @DisplayName("실패: 메시지가 없는 채널은 Optional.empty를 반환한다")
    void fail() {
      assertThat(messageRepository.findLastMessageAtByChannelId(UUID.randomUUID())).isEmpty();
    }
  }

  @Nested
  @DisplayName("deleteAllByChannelId 테스트")
  class DeleteAllByChannelId {

    @Test
    @DisplayName("성공: 해당 채널의 모든 메시지를 삭제한다")
    void success() {
      // given: 채널과 메시지 준비
      User author = userRepository.save(new User("deleter", "d@test.com", "pw", null));
      entityManager.persist(new UserStatus(author, Instant.now()));
      Channel channel = channelRepository.save(
          new Channel(ChannelType.PUBLIC, "DeleteTarget", "Desc"));

      messageRepository.save(new Message("Msg 1", channel, author, List.of()));
      messageRepository.save(new Message("Msg 2", channel, author, List.of()));

      // when: 삭제 실행
      messageRepository.deleteAllByChannelId(channel.getId());
      entityManager.flush(); // DB에 즉시 반영
      entityManager.clear(); // 영속성 컨텍스트 비우기

      // then: 해당 채널 ID로 조회 시 결과가 0이어야 함
      List<Message> remainMessages = messageRepository.findAll().stream()
          .filter(m -> m.getChannel().getId().equals(channel.getId()))
          .toList();
      assertThat(remainMessages).isEmpty();
    }

    @Test
    @DisplayName("성공(실패대용): 존재하지 않는 채널 ID로 삭제 요청 시 에러 없이 종료된다")
    void fail() {
      // 딜리트 메서드는 대상이 없어도 예외를 던지지 않는 경우가 많으므로
      // 아무 일도 일어나지 않는 것을 확인하는 것도 테스트입니다.
      UUID fakeId = UUID.randomUUID();

      // when & then: 예외가 발생하지 않아야 함
      org.junit.jupiter.api.Assertions.assertDoesNotThrow(() -> {
        messageRepository.deleteAllByChannelId(fakeId);
      });
    }
  }
}