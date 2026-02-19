package com.sprint.mission.discodeit.unitTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import com.sprint.mission.discodeit.DTO.dto.MessageDto;
import com.sprint.mission.discodeit.DTO.request.MessageCreateRequest;
import com.sprint.mission.discodeit.DTO.request.MessageUpdateRequest;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.MessageException.MessageNotFoundException;
import com.sprint.mission.discodeit.exception.UserException.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.MessageMapper;
import com.sprint.mission.discodeit.mapper.PageResponseMapper;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.basic.BasicMessageService;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
public class MessageServiceTest {

  @InjectMocks
  private BasicMessageService messageService;

  @Mock
  private MessageRepository messageRepository;
  @Mock
  private UserRepository userRepository;
  @Mock
  private ChannelRepository channelRepository;
  @Mock
  private BinaryContentRepository binaryContentRepository;
  @Mock
  private BinaryContentStorage binaryContentStorage;
  @Mock
  private MessageMapper messageMapper;
  @Mock
  private PageResponseMapper pageResponseMapper;

  private MessageCreateRequest createRequest;
  private MessageUpdateRequest updateRequest;
  private Message message;
  private MessageDto messageDto;
  private User user;
  private Channel channel;

  @BeforeEach
  void setup() {
    UUID userId = UUID.randomUUID();
    UUID channelId = UUID.randomUUID();

    user = new User("testUser", "test@example.com", "password", null);
    ReflectionTestUtils.setField(user, "id", userId);

    channel = new Channel("General", "Public Channel", ChannelType.PUBLIC);
    ReflectionTestUtils.setField(channel, "id", channelId);

    createRequest = new MessageCreateRequest(userId, channelId, "Test Message");
    updateRequest = new MessageUpdateRequest("Updated Message");

    message = new Message("Test Message", channel, user, Collections.emptyList());
    ReflectionTestUtils.setField(message, "id", UUID.randomUUID());

    messageDto = new MessageDto(message.getId(), message.getCreatedAt(), message.getUpdatedAt(),
        message.getContent(), message.getChannel().getId(), null, Collections.emptyList());
  }

  @Test
  @DisplayName("메시지 생성 테스트 - 성공")
  void create_message_success() {
    // given
    UUID userId = createRequest.authorId();
    UUID channelId = createRequest.channelId();
    String content = createRequest.content();

    given(userRepository.findById(userId)).willReturn(Optional.of(user));
    given(channelRepository.findById(channelId)).willReturn(Optional.of(channel));
    given(messageRepository.save(any(Message.class))).willReturn(message);
    given(messageMapper.toDto(any(Message.class))).willReturn(messageDto);

    //when
    MessageDto result = messageService.create(createRequest, Collections.emptyList());

    //then
    assertThat(result).isNotNull();
    assertThat(result.content()).isEqualTo(content);

    then(messageRepository).should().save(any(Message.class));
  }

  @Test
  @DisplayName("메시지 생성 테스트 - 실패(존재하지 않는 유저)")
  void create_message_fail_not_found_user() {
    //given
    UUID userId = createRequest.authorId();
    given(userRepository.findById(userId)).willReturn(Optional.empty());

    //when
    assertThatThrownBy(() -> messageService.create(createRequest, Collections.emptyList()))
        .isInstanceOf(UserNotFoundException.class);

    //then
    then(messageRepository).should(never()).save(any());
  }

  @Test
  @DisplayName("메시지 수정 테스트 - 성공")
  void update_message_success() {
    // given
    UUID messageId = message.getId();
    String newContent = updateRequest.newContent();
    given(messageRepository.findById(messageId)).willReturn(Optional.of(message));
    given(messageRepository.save(any(Message.class))).willReturn(message);
    given(messageMapper.toDto(any(Message.class))).willAnswer(invocation -> {
      Message m = invocation.getArgument(0);
      return new MessageDto(m.getId(), m.getCreatedAt(), m.getUpdatedAt(),
          m.getContent(), m.getChannel().getId(), null, Collections.emptyList());
    });

    // when
    MessageDto result = messageService.update(messageId, updateRequest);

    // then
    assertThat(result).isNotNull();
    assertThat(result.content()).isEqualTo(newContent);

    then(messageRepository).should().save(any(Message.class));
  }

  @Test
  @DisplayName("메시지 수정 테스트 - 실패 (존재하지 않는 메시지)")
  void update_message_fail_not_found() {
    // given
    UUID messageId = UUID.randomUUID();
    given(messageRepository.findById(messageId)).willReturn(Optional.empty());

    // when
    assertThatThrownBy(() -> messageService.update(messageId, updateRequest))
        .isInstanceOf(
            MessageNotFoundException.class);

    // then
    then(messageRepository).should(never()).save(any());
  }

  @Test
  @DisplayName("메시지 삭제 테스트 - 성공")
  void delete_message_success() {
    // given
    UUID messageId = message.getId();
    given(messageRepository.findById(messageId)).willReturn(Optional.of(message));

    // when
    messageService.delete(messageId);

    // then
    then(messageRepository).should().delete(message);
  }

  @Test
  @DisplayName("메시지 삭제 테스트 - 실패(존재하지 않는 메시지")
  void delete_message_fail_not_found() {
    // given
    UUID messageId = UUID.randomUUID();
    given(messageRepository.findById(messageId)).willReturn(Optional.empty());

    //when
    assertThatThrownBy(() -> messageService.delete(messageId))
        .isInstanceOf(MessageNotFoundException.class);

    //then
    then(messageRepository).should(never()).delete(any());
  }
}
