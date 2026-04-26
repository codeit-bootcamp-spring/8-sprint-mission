package com.sprint.mission.discodeit.service.basic;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.mock;
import static org.mockito.BDDMockito.never;
import static org.mockito.BDDMockito.then;

import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentDto;
import com.sprint.mission.discodeit.dto.message.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.message.MessageDto;
import com.sprint.mission.discodeit.dto.message.MessageUpdateRequest;
import com.sprint.mission.discodeit.dto.response.PageResponse;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.binarycontent.BinaryContentNotFoundException;
import com.sprint.mission.discodeit.exception.channel.ChannelNotFoundException;
import com.sprint.mission.discodeit.exception.message.MessageNotFoundException;
import com.sprint.mission.discodeit.mapper.MessageMapper;
import com.sprint.mission.discodeit.mapper.PageResponseMapper;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.BinaryContentService;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.data.domain.Sort;

@ExtendWith(MockitoExtension.class)
class BasicMessageServiceTest {

  @Mock
  MessageRepository messageRepository;
  @Mock
  ChannelRepository channelRepository;
  @Mock
  UserRepository userRepository;
  @Mock
  MessageMapper messageMapper;

  @Mock
  BinaryContentService binaryContentService;
  @Mock
  BinaryContentRepository binaryContentRepository;

  @Mock
  ApplicationEventPublisher eventPublisher;

  @Spy
  PageResponseMapper pageResponseMapper = new PageResponseMapper();

  @InjectMocks
  BasicMessageService messageService;

  @Test
  @DisplayName("create 성공: 채널/유저 존재 + 첨부파일 null이면 저장 후 dto 반환")
  void create_success_noAttachments() {

    // given
    UUID channelId = UUID.randomUUID();
    UUID authorId = UUID.randomUUID();

    MessageCreateRequest req = new MessageCreateRequest(channelId, authorId, "helloMan", null);

    Channel channel = mock(Channel.class);
    User author = mock(User.class);

    given(channelRepository.findById(channelId)).willReturn(Optional.of(channel));
    given(userRepository.findById(authorId)).willReturn(Optional.of(author));

    Message saved = mock(Message.class);
    given(messageRepository.save(any(Message.class))).willReturn(saved);

    MessageDto dto = mock(MessageDto.class);
    given(messageMapper.toDto(saved)).willReturn(dto);

    // when
    MessageDto result = messageService.createMessage(req);

    // then
    assertSame(dto, result);

    then(channelRepository).should().findById(channelId);
    then(userRepository).should().findById(authorId);

    // 저장되는 Message의 핵심 필드(author/channel/content) 검증
    ArgumentCaptor<Message> captor = ArgumentCaptor.forClass(Message.class);
    then(messageRepository).should().save(captor.capture());

    Message captured = captor.getValue();
    assertSame(author, captured.getAuthor());
    assertSame(channel, captured.getChannel());
    assertEquals("helloMan", captured.getContent());
    assertNotNull(captured.getAttachments());
    assertEquals(0, captured.getAttachments().size());

    then(messageMapper).should().toDto(saved);

    // attachments null -> binary 계층 호출 없음
    then(binaryContentService).shouldHaveNoInteractions();
    then(binaryContentRepository).shouldHaveNoInteractions();
  }

  @Test
  @DisplayName("create 실패: 채널이 없으면 예외(NoSuchElementException)")
  void create_fail_channelNotFound() {
    // given
    UUID channelId = UUID.randomUUID();
    UUID authorId = UUID.randomUUID();
    MessageCreateRequest req = new MessageCreateRequest(channelId, authorId, "helloMan", null);

    given(channelRepository.findById(channelId)).willReturn(Optional.empty());

    // when & then
    assertThrows(ChannelNotFoundException.class, () -> messageService.createMessage(req));

    then(channelRepository).should().findById(channelId);
    then(userRepository).shouldHaveNoInteractions();
    then(messageRepository).should(never()).save(any());
    then(messageMapper).shouldHaveNoInteractions();
  }

  @Test
  @DisplayName("create 실패: 첨부파일 create 후 DB 재조회가 안되면 IllegalStateException")
  void create_fail_attachmentNotFoundAfterCreate() {
    // given
    UUID channelId = UUID.randomUUID();
    UUID authorId = UUID.randomUUID();

    Channel channel = mock(Channel.class);
    User author = mock(User.class);

    given(channelRepository.findById(channelId)).willReturn(Optional.of(channel));
    given(userRepository.findById(authorId)).willReturn(Optional.of(author));

    BinaryContentCreateRequest attReq =
        new BinaryContentCreateRequest("new.txt", 2L, "text/plain", "hi".getBytes());

    MessageCreateRequest req =
        new MessageCreateRequest(channelId, authorId, "helloMan", List.of(attReq));

    UUID binaryId = UUID.randomUUID();

    given(binaryContentService.create(attReq))
        .willReturn(new BinaryContentDto(binaryId, "new.txt", 2L, "text/plain"));

    given(binaryContentRepository.findById(binaryId)).willReturn(Optional.empty());

    // when & then
    assertThrows(BinaryContentNotFoundException.class, () -> messageService.createMessage(req));

    then(binaryContentService).should().create(attReq);
    then(binaryContentRepository).should().findById(binaryId);

    then(messageRepository).should(never()).save(any());
    then(messageMapper).shouldHaveNoInteractions();
  }

  @Test
  @DisplayName("findAllByChannelId 성공: pageable=null이면 기본 정렬(createdAt desc) + size=50 적용되어 PageResponse 반환")
  void findAllByChannelId_success_defaultPageable() {

    // given
    UUID channelId = UUID.randomUUID();

    Message m1 = mock(Message.class);
    Message m2 = mock(Message.class);

    Slice<Message> slice = new SliceImpl<>(
        List.of(m1, m2),
        PageRequest.of(0, 50, Sort.by(Sort.Direction.DESC, "createdAt")),
        true
    );

    given(messageRepository.findAllByChannel_Id(eq(channelId), any(Pageable.class)))
        .willReturn(slice);

    MessageDto d1 = mock(MessageDto.class);
    MessageDto d2 = mock(MessageDto.class);
    given(messageMapper.toDto(m1)).willReturn(d1);
    given(messageMapper.toDto(m2)).willReturn(d2);

    // when
    PageResponse<MessageDto> result = messageService.findAllByChannelId(channelId, null);

    // then
    assertEquals(2, result.content().size());
    assertSame(d1, result.content().get(0));
    assertSame(d2, result.content().get(1));
    assertEquals(0, result.number());
    assertEquals(50, result.size());
    assertTrue(result.hasNext());
    assertNull(result.totalElements()); // fromSlice는 totalElements=null

    ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
    then(messageRepository).should().findAllByChannel_Id(eq(channelId), pageableCaptor.capture());

    Pageable used = pageableCaptor.getValue();
    assertEquals(0, used.getPageNumber());
    assertEquals(50, used.getPageSize());
    assertEquals(Sort.by(Sort.Direction.DESC, "createdAt"), used.getSort());

    then(messageMapper).should().toDto(m1);
    then(messageMapper).should().toDto(m2);
  }

  @Test
  @DisplayName("findAllByChannelId 실패: repository 예외는 그대로 전파")
  void findAllByChannelId_fail_repositoryThrows() {
    // given
    UUID channelId = UUID.randomUUID();
    given(messageRepository.findAllByChannel_Id(eq(channelId), any(Pageable.class)))
        .willThrow(new RuntimeException("DB 에러"));

    // when & then
    assertThrows(RuntimeException.class, () -> messageService.findAllByChannelId(channelId, null));

    then(messageRepository).should().findAllByChannel_Id(eq(channelId), any(Pageable.class));
    then(messageMapper).shouldHaveNoInteractions();
  }

  @Test
  @DisplayName("update 성공: message 존재하면 content 변경 후 dto 반환 (save 호출 없음)")
  void update_success() {
    // given
    UUID messageId = UUID.randomUUID();
    MessageUpdateRequest req = new MessageUpdateRequest("new content");

    // 실제 Message 객체를 쓰면 update로 content가 바뀌는지 검증 가능
    User author = mock(User.class);
    Channel channel = mock(Channel.class);
    Message message = new Message(author, channel, "old content", List.of());

    given(messageRepository.findById(messageId)).willReturn(Optional.of(message));

    MessageDto dto = mock(MessageDto.class);
    given(messageMapper.toDto(message)).willReturn(dto);

    // when
    MessageDto result = messageService.updateMessage(messageId, req);

    // then
    assertSame(dto, result);
    assertEquals("new content", message.getContent());

    then(messageRepository).should().findById(messageId);
    then(messageRepository).should(never()).save(any());
    then(messageMapper).should().toDto(message);
  }

  @Test
  @DisplayName("update 실패: message가 없으면 예외(NoSuchElementException)")
  void update_fail_notFound() {
    // given
    UUID messageId = UUID.randomUUID();
    MessageUpdateRequest req = new MessageUpdateRequest("new content");

    given(messageRepository.findById(messageId)).willReturn(Optional.empty());

    // when & then
    assertThrows(MessageNotFoundException.class,
        () -> messageService.updateMessage(messageId, req));

    then(messageRepository).should().findById(messageId);
    then(messageMapper).shouldHaveNoInteractions();
  }

  @Test
  @DisplayName("delete 성공: 존재하면 deleteById 호출")
  void delete_success() {
    // given
    UUID messageId = UUID.randomUUID();
    given(messageRepository.existsById(messageId)).willReturn(true);

    // when
    assertDoesNotThrow(() -> messageService.deleteMessage(messageId));

    // then
    then(messageRepository).should().existsById(messageId);
    then(messageRepository).should().deleteById(messageId);
  }

  @Test
  @DisplayName("delete 실패: 존재하지 않으면 예외 + deleteById 호출 안 됨")
  void delete_fail_notFound() {
    // given
    UUID messageId = UUID.randomUUID();
    given(messageRepository.existsById(messageId)).willReturn(false);

    // when & then
    assertThrows(MessageNotFoundException.class, () -> messageService.deleteMessage(messageId));

    then(messageRepository).should().existsById(messageId);
    then(messageRepository).should(never()).deleteById(any());
  }
}
