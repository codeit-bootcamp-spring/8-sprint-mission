package com.sprint.mission.discodeit.message;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import com.sprint.mission.discodeit.dto.data.MessageDto;
import com.sprint.mission.discodeit.dto.request.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.request.MessageUpdateRequest;
import com.sprint.mission.discodeit.dto.response.PageResponse;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.mapper.MessageMapper;
import com.sprint.mission.discodeit.mapper.PageResponseMapper;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.basic.BasicMessageService;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

@ExtendWith(MockitoExtension.class)
@DisplayName("MessageService 단위 테스트")
class MessageServiceTest {

  @Mock
  private MessageRepository messageRepository;
  @Mock
  private ChannelRepository channelRepository;
  @Mock
  private UserRepository userRepository;
  @Mock
  private MessageMapper messageMapper;
  @Mock
  private BinaryContentStorage binaryContentStorage;
  @Mock
  private BinaryContentRepository binaryContentRepository;
  @Mock
  private PageResponseMapper pageResponseMapper;

  @InjectMocks
  private BasicMessageService sut;

  private static final UUID MESSAGE_ID = UUID.randomUUID();
  private static final UUID CHANNEL_ID = UUID.randomUUID();
  private static final UUID AUTHOR_ID = UUID.randomUUID();

  @Nested
  @DisplayName("create")
  class Create {

    @Test
    @DisplayName("성공: 메시지 생성 후 MessageDto 반환")
    void create_success() {
      // given
      MessageCreateRequest request = new MessageCreateRequest("안녕", CHANNEL_ID, AUTHOR_ID);
      Channel channel = new Channel(ChannelType.PUBLIC, "채널", null);
      User author = new User("author", "a@b.com", "p", null);
      Message message = new Message("안녕", channel, author, List.of());
      MessageDto expectedDto = new MessageDto(MESSAGE_ID, "안녕", CHANNEL_ID, AUTHOR_ID, List.of(), Instant.now());

      given(channelRepository.findById(CHANNEL_ID)).willReturn(Optional.of(channel));
      given(userRepository.findById(AUTHOR_ID)).willReturn(Optional.of(author));
      given(messageRepository.save(any(Message.class))).willAnswer(inv -> inv.getArgument(0));
      given(messageMapper.toDto(any(Message.class))).willReturn(expectedDto);

      // when
      MessageDto result = sut.create(request, List.of());

      // then
      assertThat(result).isEqualTo(expectedDto);
      then(messageRepository).should().save(any(Message.class));
    }

    @Test
    @DisplayName("실패: 채널 없음 시 NoSuchElementException")
    void create_fail_channelNotFound() {
      // given
      MessageCreateRequest request = new MessageCreateRequest("안녕", CHANNEL_ID, AUTHOR_ID);
      given(channelRepository.findById(CHANNEL_ID)).willReturn(Optional.empty());

      // when & then
      assertThatThrownBy(() -> sut.create(request, List.of()))
          .isInstanceOf(NoSuchElementException.class)
          .hasMessageContaining("does not exist");
      then(messageRepository).should(never()).save(any(Message.class));
    }

    @Test
    @DisplayName("실패: 작성자 없음 시 NoSuchElementException")
    void create_fail_authorNotFound() {
      // given
      MessageCreateRequest request = new MessageCreateRequest("안녕", CHANNEL_ID, AUTHOR_ID);
      Channel channel = new Channel(ChannelType.PUBLIC, "채널", null);
      given(channelRepository.findById(CHANNEL_ID)).willReturn(Optional.of(channel));
      given(userRepository.findById(AUTHOR_ID)).willReturn(Optional.empty());

      // when & then
      assertThatThrownBy(() -> sut.create(request, List.of()))
          .isInstanceOf(NoSuchElementException.class)
          .hasMessageContaining("does not exist");
      then(messageRepository).should(never()).save(any(Message.class));
    }
  }

  @Nested
  @DisplayName("update")
  class Update {

    @Test
    @DisplayName("성공: 메시지 수정 후 MessageDto 반환")
    void update_success() {
      // given
      Channel channel = new Channel(ChannelType.PUBLIC, "c", null);
      User author = new User("u", "e", "p", null);
      Message message = new Message("기존내용", channel, author, List.of());
      MessageUpdateRequest request = new MessageUpdateRequest("새내용");
      MessageDto expectedDto = new MessageDto(MESSAGE_ID, "새내용", CHANNEL_ID, AUTHOR_ID, List.of(), Instant.now());

      given(messageRepository.findById(MESSAGE_ID)).willReturn(Optional.of(message));
      given(messageMapper.toDto(any(Message.class))).willReturn(expectedDto);

      // when
      MessageDto result = sut.update(MESSAGE_ID, request);

      // then
      assertThat(result).isEqualTo(expectedDto);
    }

    @Test
    @DisplayName("실패: 메시지 없음 시 NoSuchElementException")
    void update_fail_messageNotFound() {
      // given
      MessageUpdateRequest request = new MessageUpdateRequest("새내용");
      given(messageRepository.findById(MESSAGE_ID)).willReturn(Optional.empty());

      // when & then
      assertThatThrownBy(() -> sut.update(MESSAGE_ID, request))
          .isInstanceOf(NoSuchElementException.class)
          .hasMessageContaining("not found");
    }
  }

  @Nested
  @DisplayName("delete")
  class Delete {

    @Test
    @DisplayName("성공: 메시지 삭제 시 deleteById 호출")
    void delete_success() {
      // given
      given(messageRepository.existsById(MESSAGE_ID)).willReturn(true);

      // when
      sut.delete(MESSAGE_ID);

      // then
      then(messageRepository).should().deleteById(MESSAGE_ID);
    }

    @Test
    @DisplayName("실패: 메시지 없음 시 NoSuchElementException")
    void delete_fail_messageNotFound() {
      // given
      given(messageRepository.existsById(MESSAGE_ID)).willReturn(false);

      // when & then
      assertThatThrownBy(() -> sut.delete(MESSAGE_ID))
          .isInstanceOf(NoSuchElementException.class)
          .hasMessageContaining("not found");
      then(messageRepository).should(never()).deleteById(any(UUID.class));
    }
  }

  @Nested
  @DisplayName("findAllByChannelId")
  class FindAllByChannelId {

    @Test
    @DisplayName("성공: 채널별 메시지 목록(페이지) 반환")
    void findAllByChannelId_success() {
      // given
      Pageable pageable = PageRequest.of(0, 20);
      Channel channel = new Channel(ChannelType.PUBLIC, "c", null);
      User author = new User("u", "e", "p", null);
      Message message = new Message("내용", channel, author, List.of());
      MessageDto dto = new MessageDto(MESSAGE_ID, "내용", CHANNEL_ID, AUTHOR_ID, List.of(), Instant.now());
      Slice<Message> slice = new SliceImpl<>(List.of(message));
      PageResponse<MessageDto> expectedPage = new PageResponse<>(List.of(dto), null, 1, false, 1L);

      given(messageRepository.findAllByChannelIdWithAuthor(eq(CHANNEL_ID), any(Instant.class), eq(pageable)))
          .willReturn(slice);
      given(messageMapper.toDto(any(Message.class))).willReturn(dto);
      given(pageResponseMapper.fromSlice(org.mockito.ArgumentMatchers.<Slice<MessageDto>>any(), any())).willReturn(expectedPage);

      // when
      PageResponse<MessageDto> result = sut.findAllByChannelId(CHANNEL_ID, Instant.now(), pageable);

      // then
      assertThat(result).isNotNull();
      assertThat(result.content()).hasSize(1);
    }

    @Test
    @DisplayName("성공: 메시지 없으면 빈 페이지 반환")
    void findAllByChannelId_empty() {
      // given
      Pageable pageable = PageRequest.of(0, 20);
      Slice<Message> emptySlice = new SliceImpl<>(List.of());
      PageResponse<MessageDto> emptyPage = new PageResponse<>(List.of(), null, 0, false, 0L);

      given(messageRepository.findAllByChannelIdWithAuthor(eq(CHANNEL_ID), any(Instant.class), eq(pageable)))
          .willReturn(emptySlice);
      given(pageResponseMapper.fromSlice(org.mockito.ArgumentMatchers.<Slice<MessageDto>>any(), any())).willReturn(emptyPage);

      // when
      PageResponse<MessageDto> result = sut.findAllByChannelId(CHANNEL_ID, Instant.now(), pageable);

      // then
      assertThat(result.content()).isEmpty();
    }
  }
}
