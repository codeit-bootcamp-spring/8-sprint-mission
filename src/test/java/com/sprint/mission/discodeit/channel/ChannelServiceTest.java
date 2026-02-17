package com.sprint.mission.discodeit.channel;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import com.sprint.mission.discodeit.dto.data.ChannelDto;
import com.sprint.mission.discodeit.dto.request.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.request.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.dto.request.PublicChannelUpdateRequest;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.mapper.ChannelMapper;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.basic.BasicChannelService;
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

@ExtendWith(MockitoExtension.class)
@DisplayName("ChannelService 단위 테스트")
class ChannelServiceTest {

  @Mock
  private ChannelRepository channelRepository;
  @Mock
  private ReadStatusRepository readStatusRepository;
  @Mock
  private MessageRepository messageRepository;
  @Mock
  private UserRepository userRepository;
  @Mock
  private ChannelMapper channelMapper;

  @InjectMocks
  private BasicChannelService sut;

  private static final UUID CHANNEL_ID = UUID.randomUUID();
  private static final UUID USER_ID = UUID.randomUUID();

  @Nested
  @DisplayName("create(PUBLIC)")
  class CreatePublic {

    @Test
    @DisplayName("성공: 공개 채널 생성 후 ChannelDto 반환")
    void createPublic_success() {
      // given
      PublicChannelCreateRequest request = new PublicChannelCreateRequest("공개채널", "설명");
      Channel channel = new Channel(ChannelType.PUBLIC, "공개채널", "설명");
      ChannelDto expectedDto = new ChannelDto(CHANNEL_ID, ChannelType.PUBLIC, "공개채널", "설명", List.of(), List.of(), null);

      given(channelRepository.save(any(Channel.class))).willAnswer(inv -> {
        Channel c = inv.getArgument(0);
        return c;
      });
      given(channelMapper.toDto(any(Channel.class))).willReturn(expectedDto);

      // when
      ChannelDto result = sut.create(request);

      // then
      assertThat(result).isEqualTo(expectedDto);
      then(channelRepository).should().save(any(Channel.class));
    }
  }

  @Nested
  @DisplayName("create(PRIVATE)")
  class CreatePrivate {

    @Test
    @DisplayName("성공: 비공개 채널 생성 후 ChannelDto 반환")
    void createPrivate_success() {
      // given
      List<UUID> participantIds = List.of(USER_ID);
      PrivateChannelCreateRequest request = new PrivateChannelCreateRequest(participantIds);
      User user = new User("u", "u@e.com", "p", null);
      Channel channel = new Channel(ChannelType.PRIVATE, null, null);

      given(channelRepository.save(any(Channel.class))).willAnswer(inv -> inv.getArgument(0));
      given(userRepository.findAllById(participantIds)).willReturn(List.of(user));
      given(readStatusRepository.saveAll(anyList())).willReturn(List.of());
      given(channelMapper.toDto(any(Channel.class))).willReturn(
          new ChannelDto(CHANNEL_ID, ChannelType.PRIVATE, null, null, List.of(), List.of(), null));

      // when
      ChannelDto result = sut.create(request);

      // then
      assertThat(result).isNotNull();
      then(channelRepository).should().save(any(Channel.class));
      then(readStatusRepository).should().saveAll(anyList());
    }
  }

  @Nested
  @DisplayName("update")
  class Update {

    @Test
    @DisplayName("성공: 공개 채널 수정 후 ChannelDto 반환")
    void update_success() {
      // given
      Channel channel = new Channel(ChannelType.PUBLIC, "기존명", "기존설명");
      PublicChannelUpdateRequest request = new PublicChannelUpdateRequest("새이름", "새설명");
      ChannelDto expectedDto = new ChannelDto(CHANNEL_ID, ChannelType.PUBLIC, "새이름", "새설명", List.of(), List.of(), null);

      given(channelRepository.findById(CHANNEL_ID)).willReturn(Optional.of(channel));
      given(channelMapper.toDto(any(Channel.class))).willReturn(expectedDto);

      // when
      ChannelDto result = sut.update(CHANNEL_ID, request);

      // then
      assertThat(result).isEqualTo(expectedDto);
    }

    @Test
    @DisplayName("실패: 채널 없음 시 NoSuchElementException")
    void update_fail_channelNotFound() {
      // given
      PublicChannelUpdateRequest request = new PublicChannelUpdateRequest("새이름", "새설명");
      given(channelRepository.findById(CHANNEL_ID)).willReturn(Optional.empty());

      // when & then
      assertThatThrownBy(() -> sut.update(CHANNEL_ID, request))
          .isInstanceOf(NoSuchElementException.class)
          .hasMessageContaining("not found");
    }

    @Test
    @DisplayName("실패: 비공개 채널 수정 시 IllegalArgumentException")
    void update_fail_privateChannel() {
      // given
      Channel privateChannel = new Channel(ChannelType.PRIVATE, null, null);
      PublicChannelUpdateRequest request = new PublicChannelUpdateRequest("새이름", "새설명");
      given(channelRepository.findById(CHANNEL_ID)).willReturn(Optional.of(privateChannel));

      // when & then
      assertThatThrownBy(() -> sut.update(CHANNEL_ID, request))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("Private channel");
    }
  }

  @Nested
  @DisplayName("delete")
  class Delete {

    @Test
    @DisplayName("성공: 채널 삭제 시 관련 삭제 메서드 호출")
    void delete_success() {
      // given
      given(channelRepository.existsById(CHANNEL_ID)).willReturn(true);

      // when
      sut.delete(CHANNEL_ID);

      // then
      then(messageRepository).should().deleteAllByChannelId(CHANNEL_ID);
      then(readStatusRepository).should().deleteAllByChannelId(CHANNEL_ID);
      then(channelRepository).should().deleteById(CHANNEL_ID);
    }

    @Test
    @DisplayName("실패: 채널 없음 시 NoSuchElementException")
    void delete_fail_channelNotFound() {
      // given
      given(channelRepository.existsById(CHANNEL_ID)).willReturn(false);

      // when & then
      assertThatThrownBy(() -> sut.delete(CHANNEL_ID))
          .isInstanceOf(NoSuchElementException.class)
          .hasMessageContaining("not found");
      then(channelRepository).should(never()).deleteById(any(UUID.class));
    }
  }

  @Nested
  @DisplayName("findAllByUserId")
  class FindAllByUserId {

    @Test
    @DisplayName("성공: 사용자별 채널 목록 반환")
    void findAllByUserId_success() {
      // given
      ReadStatus rs = new ReadStatus(new User("u", "e", "p", null), new Channel(ChannelType.PUBLIC, "c", "d"), java.time.Instant.now());
      Channel channel = new Channel(ChannelType.PUBLIC, "채널", "설명");
      ChannelDto dto = new ChannelDto(CHANNEL_ID, ChannelType.PUBLIC, "채널", "설명", List.of(), List.of(), null);

      given(readStatusRepository.findAllByUserId(USER_ID)).willReturn(List.of(rs));
      given(channelRepository.findAllByTypeOrIdIn(eq(ChannelType.PUBLIC), anyList())).willReturn(List.of(channel));
      given(channelMapper.toDto(any(Channel.class))).willReturn(dto);

      // when
      List<ChannelDto> result = sut.findAllByUserId(USER_ID);

      // then
      assertThat(result).hasSize(1).first().isEqualTo(dto);
    }

    @Test
    @DisplayName("성공: 구독 채널 없으면 빈 목록 반환")
    void findAllByUserId_empty() {
      // given
      given(readStatusRepository.findAllByUserId(USER_ID)).willReturn(List.of());
      given(channelRepository.findAllByTypeOrIdIn(eq(ChannelType.PUBLIC), eq(List.of()))).willReturn(List.of());

      // when
      List<ChannelDto> result = sut.findAllByUserId(USER_ID);

      // then
      assertThat(result).isEmpty();
    }
  }
}
