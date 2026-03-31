package com.sprint.mission.discodeit.service.basic;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.mock;
import static org.mockito.BDDMockito.never;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.times;

import com.sprint.mission.discodeit.dto.channel.ChannelCreatePrivateRequest;
import com.sprint.mission.discodeit.dto.channel.ChannelCreatePublicRequest;
import com.sprint.mission.discodeit.dto.channel.ChannelDto;
import com.sprint.mission.discodeit.dto.channel.ChannelUpdateRequest;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserRole;
import com.sprint.mission.discodeit.exception.channel.ChannelNotFoundException;
import com.sprint.mission.discodeit.exception.channel.PrivateChannelUpdateException;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.ChannelMapper;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class BasicChannelServiceTest {

  @Mock
  ChannelRepository channelRepository;
  @Mock
  MessageRepository messageRepository;
  @Mock
  ReadStatusRepository readStatusRepository;
  @Mock
  UserRepository userRepository;
  @Mock
  ChannelMapper channelMapper;

  @InjectMocks
  BasicChannelService channelService;

  @Test
  @DisplayName("createPublic 성공: PUBLIC 채널 저장 후 dto 반환")
  void createPublic_success() {

    // given
    ChannelCreatePublicRequest req = new ChannelCreatePublicRequest("general", "공지 채널");

    Channel saved = new Channel(ChannelType.PUBLIC, "general", "공지 채널");
    given(channelRepository.save(any(Channel.class))).willReturn(saved);

    ChannelDto dto = mock(ChannelDto.class);
    given(channelMapper.toDto(saved)).willReturn(dto);

    // when
    ChannelDto result = channelService.createPublicChannel(req);

    // then
    assertSame(dto, result);

    ArgumentCaptor<Channel> captor = ArgumentCaptor.forClass(Channel.class);
    then(channelRepository).should().save(captor.capture());

    Channel captured = captor.getValue();
    assertEquals(ChannelType.PUBLIC, captured.getType());
    assertEquals("general", captured.getName());
    assertEquals("공지 채널", captured.getDescription());

    then(channelMapper).should().toDto(saved);
  }

  @Test
  @DisplayName("createPublic 실패: repository.save에서 예외가 나면 그대로 전파")
  void createPublic_fail_saveThrows() {
    // given
    ChannelCreatePublicRequest req = new ChannelCreatePublicRequest("general", "공지 채널");
    given(channelRepository.save(any(Channel.class))).willThrow(new RuntimeException("DB 에러"));

    // when & then
    assertThrows(RuntimeException.class, () -> channelService.createPublicChannel(req));

    then(channelRepository).should().save(any(Channel.class));
    then(channelMapper).shouldHaveNoInteractions();
  }

  @Test
  @DisplayName("createPrivate 성공: PRIVATE 채널 저장 + 참가자 ReadStatus 생성 + dto 반환")
  void createPrivate_success() {

    // given
    UUID u1 = UUID.randomUUID();
    UUID u2 = UUID.randomUUID();
    ChannelCreatePrivateRequest req = new ChannelCreatePrivateRequest(List.of(u1, u2));

    Channel savedChannel = new Channel(ChannelType.PRIVATE, null, null);
    given(channelRepository.save(any(Channel.class))).willReturn(savedChannel);

    User user1 = new User("u1", "u1@test.com", "pw", null, UserRole.USER);
    User user2 = new User("u2", "u2@test.com", "pw", null, UserRole.USER);
    given(userRepository.findById(u1)).willReturn(Optional.of(user1));
    given(userRepository.findById(u2)).willReturn(Optional.of(user2));

    ChannelDto dto = mock(ChannelDto.class);
    given(channelMapper.toDto(savedChannel)).willReturn(dto);

    // when
    ChannelDto result = channelService.createPrivateChannel(req);

    // then
    assertSame(dto, result);

    ArgumentCaptor<Channel> channelCaptor = ArgumentCaptor.forClass(Channel.class);
    then(channelRepository).should().save(channelCaptor.capture());
    assertEquals(ChannelType.PRIVATE, channelCaptor.getValue().getType());

    ArgumentCaptor<ReadStatus> rsCaptor = ArgumentCaptor.forClass(ReadStatus.class);
    then(readStatusRepository).should(times(2)).save(rsCaptor.capture());

    List<ReadStatus> savedReadStatuses = rsCaptor.getAllValues();
    assertEquals(2, savedReadStatuses.size());
    assertTrue(savedReadStatuses.stream().allMatch(rs -> rs.getChannel() == savedChannel));
    assertTrue(savedReadStatuses.stream().allMatch(rs -> rs.getLastReadAt().equals(Instant.EPOCH)));

    then(channelMapper).should().toDto(savedChannel);
  }

  @Test
  @DisplayName("createPrivate 실패: 참가자 중 user가 없으면 예외 발생")
  void createPrivate_fail_userNotFound() {
    // given
    UUID missing = UUID.randomUUID();
    ChannelCreatePrivateRequest req = new ChannelCreatePrivateRequest(List.of(missing));

    Channel savedChannel = new Channel(ChannelType.PRIVATE, null, null);
    given(channelRepository.save(any(Channel.class))).willReturn(savedChannel);

    given(userRepository.findById(missing)).willReturn(Optional.empty());

    // when & then
    assertThrows(UserNotFoundException.class, () -> channelService.createPrivateChannel(req));

    then(channelRepository).should().save(any(Channel.class));
    then(userRepository).should().findById(missing);
    then(readStatusRepository).should(never()).save(any());
    then(channelMapper).shouldHaveNoInteractions();
  }

  @Test
  @DisplayName("update 성공: PUBLIC 채널이면 이름/설명 변경 후 dto 반환 (save 호출 없음)")
  void update_success_public() {
    // given
    UUID channelId = UUID.randomUUID();
    ChannelUpdateRequest req = new ChannelUpdateRequest("newName", "newDesc");

    Channel channel = new Channel(ChannelType.PUBLIC, "oldName", "oldDesc");
    given(channelRepository.findById(channelId)).willReturn(Optional.of(channel));

    ChannelDto dto = mock(ChannelDto.class);
    given(channelMapper.toDto(channel)).willReturn(dto);

    // when
    ChannelDto result = channelService.updateChannel(channelId, req);

    // then
    assertSame(dto, result);

    then(channelRepository).should().findById(channelId);
    then(channelRepository).should(never()).save(any());

    assertEquals("newName", channel.getName());
    assertEquals("newDesc", channel.getDescription());

    then(channelMapper).should().toDto(channel);
  }

  @Test
  @DisplayName("update 실패: 채널이 없으면 예외")
  void update_fail_notFound() {
    // given
    UUID channelId = UUID.randomUUID();
    ChannelUpdateRequest req = new ChannelUpdateRequest("newName", "newDesc");
    given(channelRepository.findById(channelId)).willReturn(Optional.empty());

    // when & then
    assertThrows(ChannelNotFoundException.class,
        () -> channelService.updateChannel(channelId, req));

    then(channelRepository).should().findById(channelId);
    then(channelMapper).shouldHaveNoInteractions();
  }

  @Test
  @DisplayName("update 실패: PRIVATE 채널이면 수정 불가 예외")
  void update_fail_privateChannel() {
    // given
    UUID channelId = UUID.randomUUID();
    ChannelUpdateRequest req = new ChannelUpdateRequest("newName", "newDesc");

    Channel channel = new Channel(ChannelType.PRIVATE, null, null);
    given(channelRepository.findById(channelId)).willReturn(Optional.of(channel));

    // when & then
    assertThrows(
        PrivateChannelUpdateException.class, () -> channelService.updateChannel(channelId, req));

    then(channelRepository).should().findById(channelId);
    then(channelMapper).shouldHaveNoInteractions();
  }

  @Test
  @DisplayName("delete 성공: 채널 존재 -> 메시지 삭제 + ReadStatus 삭제 + 채널 삭제")
  void delete_success() {
    // given
    UUID channelId = UUID.randomUUID();
    given(channelRepository.existsById(channelId)).willReturn(true);

    Message m1 = mock(Message.class);
    Message m2 = mock(Message.class);
    given(messageRepository.findAllByChannel_Id(channelId)).willReturn(List.of(m1, m2));

    // when
    assertDoesNotThrow(() -> channelService.deleteChannel(channelId));

    // then
    then(channelRepository).should().existsById(channelId);
    then(messageRepository).should().findAllByChannel_Id(channelId);
    then(messageRepository).should().delete(m1);
    then(messageRepository).should().delete(m2);

    then(readStatusRepository).should().deleteAllByChannel_Id(channelId);
    then(channelRepository).should().deleteById(channelId);
  }

  @Test
  @DisplayName("delete 실패: 채널이 없으면 예외 + 아무 삭제도 호출되지 않음")
  void delete_fail_notFound() {
    // given
    UUID channelId = UUID.randomUUID();
    given(channelRepository.existsById(channelId)).willReturn(false);

    // when & then
    assertThrows(ChannelNotFoundException.class, () -> channelService.deleteChannel(channelId));

    then(channelRepository).should().existsById(channelId);

    then(messageRepository).shouldHaveNoInteractions();
    then(readStatusRepository).shouldHaveNoInteractions();
    then(channelRepository).should(never()).deleteById(any());
  }

  @Test
  @DisplayName("findAllByUserId 성공: 접근 가능한 채널 리스트를 dto 리스트로 변환")
  void findAllByUserId_success() {

    // given
    UUID userId = UUID.randomUUID();

    Channel c1 = new Channel(ChannelType.PUBLIC, "ch1", "desc1");
    Channel c2 = new Channel(ChannelType.PUBLIC, "ch2", "desc2");
    given(channelRepository.findAllAccessibleByUserId(userId)).willReturn(List.of(c1, c2));

    ChannelDto d1 = mock(ChannelDto.class);
    ChannelDto d2 = mock(ChannelDto.class);
    given(channelMapper.toDto(c1)).willReturn(d1);
    given(channelMapper.toDto(c2)).willReturn(d2);

    // when
    List<ChannelDto> result = channelService.findAllByUserId(userId);

    // then
    assertEquals(2, result.size());
    assertSame(d1, result.get(0));
    assertSame(d2, result.get(1));

    then(channelRepository).should().findAllAccessibleByUserId(userId);
    then(channelMapper).should().toDto(c1);
    then(channelMapper).should().toDto(c2);
  }

  @Test
  @DisplayName("findAllByUserId 실패: repository에서 예외가 발생하면 그대로 전파")
  void findAllByUserId_fail_repositoryThrows() {

    // given
    UUID userId = UUID.randomUUID();
    given(channelRepository.findAllAccessibleByUserId(userId)).willThrow(
        new RuntimeException("DB 에러"));

    // when & then
    assertThrows(RuntimeException.class, () -> channelService.findAllByUserId(userId));

    then(channelRepository).should().findAllAccessibleByUserId(userId);
    then(channelMapper).shouldHaveNoInteractions();
  }

}
