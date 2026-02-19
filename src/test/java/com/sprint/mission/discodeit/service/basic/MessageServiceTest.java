package com.sprint.mission.discodeit.service.basic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;

import com.sprint.mission.discodeit.dto.data.MessageDto;
import com.sprint.mission.discodeit.dto.data.UserDto;
import com.sprint.mission.discodeit.dto.request.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.request.MessageUpdateRequest;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.ChannelNotFoundException;
import com.sprint.mission.discodeit.exception.MessageException;
import com.sprint.mission.discodeit.exception.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.MessageMapper;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
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

  @InjectMocks
  private BasicMessageService messageService;

  @Nested
  @DisplayName("메시지 생성 테스트")
  class CreateMessage {

    @Test
    @DisplayName("성공: 유효한 채널과 작성자로 메시지를 생성")
    void create_success() {
      //given
      UUID channelId = UUID.randomUUID();
      UUID authorId = UUID.randomUUID();
      MessageCreateRequest request = new MessageCreateRequest("안녕하세요", channelId, authorId);

      Channel channel = new Channel(ChannelType.PUBLIC, "채널", "설명");
      User author = new User("writer", "writer@email.com", "pw", null);

      UserDto authorDto = new UserDto(authorId, "writer", "writer@email.com", null, true);
      MessageDto expectedDto = new MessageDto(UUID.randomUUID(), Instant.now(), null, "안녕하세요",
          channelId, authorDto, List.of());

      given(channelRepository.findById(channelId)).willReturn(Optional.of(channel));
      given(userRepository.findById(authorId)).willReturn(Optional.of(author));
      given(messageMapper.toDto(any(Message.class))).willReturn(expectedDto);

      //when
      MessageDto result = messageService.create(request, List.of());

      //then
      assertThat(result.content()).isEqualTo("안녕하세요");
      then(messageRepository).should(times(1)).save(any(Message.class));
    }

    @Test
    @DisplayName("실패: 존재하지 않는 채널에 메시지 생성 시 예외가 발생")
    void create_fail_channelNotFound() {
      //given
      UUID channelId = UUID.randomUUID();
      UUID authorId = UUID.randomUUID();

      MessageCreateRequest request = new MessageCreateRequest("안녕하세요", channelId, authorId);
      given(channelRepository.findById(channelId)).willReturn(Optional.empty());

      //when & then
      assertThatThrownBy(() -> messageService.create(request, List.of()))
          .isInstanceOf(ChannelNotFoundException.class);
    }


    @Test
    @DisplayName("실패: 존재하지 않는 작성자로 메시지 생성 시 예외가 발생")
    void create_fail_userNotFound() {
      // given
      UUID channelId = UUID.randomUUID();
      UUID authorId = UUID.randomUUID();
      MessageCreateRequest request = new MessageCreateRequest("내용", channelId, authorId);

      //채널은 존재한다고 가정
      given(channelRepository.findById(channelId)).willReturn(Optional.of(mock(Channel.class)));
      //유저(작성자)는 존재하지 않는다고 가정
      given(userRepository.findById(authorId)).willReturn(Optional.empty());

      //when & then
      assertThatThrownBy(() -> messageService.create(request, List.of()))
          .isInstanceOf(UserNotFoundException.class);
    }
  }

  @Nested
  @DisplayName("메시지 수정 테스트")
  class UpdateMessage {

    @Test
    @DisplayName("성공: 메시지 내용을 정상적으로 수정")
    void update_success() {
      //given
      UUID messageId = UUID.randomUUID();
      MessageUpdateRequest request = new MessageUpdateRequest("수정된 내용");
      Message message = mock(Message.class); // 간단하게 Mock 객체 활용

      given(messageRepository.findById(messageId)).willReturn(Optional.of(message));

      //when
      messageService.update(messageId, request);

      //then
      then(message).should().update("수정된 내용");
    }

    @Test
    @DisplayName("실패: 존재하지 않는 메시지 수정 시 예외가 발생")
    void update_fail_notFound() {
      //given
      UUID messageId = UUID.randomUUID();
      MessageUpdateRequest request = new MessageUpdateRequest("수정");
      given(messageRepository.findById(messageId)).willReturn(Optional.empty());

      //when & then
      assertThatThrownBy(() -> messageService.update(messageId, request))
          .isInstanceOf(MessageException.class);
    }
  }

  @Nested
  @DisplayName("메시지 삭제 테스트")
  class DeleteMessage {

    @Test
    @DisplayName("성공: 메시지를 삭제")
    void delete_success() {
      //given
      UUID messageId = UUID.randomUUID();
      given(messageRepository.existsById(messageId)).willReturn(true);

      //when
      messageService.delete(messageId);

      //then
      then(messageRepository).should().deleteById(messageId);
    }

    @Test
    @DisplayName("실패: 존재하지 않는 메시지 삭제 시 예외가 발생")
    void delete_fail_notFound() {
      //given
      UUID messageId = UUID.randomUUID();
      given(messageRepository.existsById(messageId)).willReturn(false);

      //when & then
      assertThatThrownBy(() -> messageService.delete(messageId))
          .isInstanceOf(MessageException.class);
    }
  }
}