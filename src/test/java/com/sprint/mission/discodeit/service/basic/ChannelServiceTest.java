package com.sprint.mission.discodeit.service.basic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

import com.sprint.mission.discodeit.dto.data.ChannelDto;
import com.sprint.mission.discodeit.dto.request.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.request.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.dto.request.PublicChannelUpdateRequest;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.exception.ChannelNotFoundException;
import com.sprint.mission.discodeit.exception.PrivateChannelUpdatedException;
import com.sprint.mission.discodeit.mapper.ChannelMapper;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
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
  private BasicChannelService channelService;

  @Nested
  @DisplayName("채널 생성 테스트")
  class CreateChannel {

    @Test
    @DisplayName("성공: 공개 채널을 생성")
    void create_public_success() {
      // given
      PublicChannelCreateRequest request = new PublicChannelCreateRequest("공개방", "설명");
      Channel channel = new Channel(ChannelType.PUBLIC, "공개방", "설명");
      ChannelDto expectedDto = new ChannelDto(
          UUID.randomUUID(),
          ChannelType.PUBLIC,
          "공개방",
          "설명",
          List.of(), // participants (빈 리스트)
          null       // lastMessageAt
      );

      given(channelMapper.toDto(any(Channel.class))).willReturn(expectedDto);

      // when
      ChannelDto result = channelService.create(request);

      // then
      assertThat(result.name()).isEqualTo("공개방");
      then(channelRepository).should(times(1)).save(any(Channel.class));
    }

    @Test
    @DisplayName("성공: 비공개 채널을 생성하고 참여자를 등록")
    void create_private_success() {
      // given
      List<UUID> participantIds = List.of(UUID.randomUUID(), UUID.randomUUID());
      PrivateChannelCreateRequest request = new PrivateChannelCreateRequest(participantIds);

      given(userRepository.findAllById(participantIds)).willReturn(List.of()); // 편의상 빈 리스트

      // when
      channelService.create(request);

      // then
      then(channelRepository).should(times(1)).save(any(Channel.class));
      then(readStatusRepository).should(times(1)).saveAll(any());
    }
  }

  @Nested
  @DisplayName("채널 수정 테스트")
  class UpdateChannel {

    @Test
    @DisplayName("실패: 비공개 채널을 수정하려고 하면 예외가 발생")
    void update_fail_privateChannel() {
      // given
      UUID channelId = UUID.randomUUID();
      Channel privateChannel = new Channel(ChannelType.PRIVATE, null, null);
      PublicChannelUpdateRequest request = new PublicChannelUpdateRequest("새이름", "새설명");

      given(channelRepository.findById(channelId)).willReturn(Optional.of(privateChannel));

      // when & then
      assertThatThrownBy(() -> channelService.update(channelId, request))
          .isInstanceOf(PrivateChannelUpdatedException.class);
    }

    @Test
    @DisplayName("실패: 존재하지 않는 채널 수정 시 예외가 발생")
    void update_fail_notFound() {
      // given
      UUID channelId = UUID.randomUUID();
      PublicChannelUpdateRequest request = new PublicChannelUpdateRequest("새이름", "새설명");

      given(channelRepository.findById(channelId)).willReturn(Optional.empty());

      // when & then
      assertThatThrownBy(() -> channelService.update(channelId, request))
          .isInstanceOf(ChannelNotFoundException.class);
    }
  }

  @Nested
  @DisplayName("채널 조회 테스트")
  class FindChannel {

    @Test
    @DisplayName("성공: 사용자가 참여 중이거나 공개된 채널 목록을 조회")
    void findAllByUserId_success() {
      // given
      UUID userId = UUID.randomUUID();
      given(readStatusRepository.findAllByUserId(userId)).willReturn(List.of());
      given(channelRepository.findAllByTypeOrIdIn(any(), any())).willReturn(List.of());

      // when
      List<ChannelDto> result = channelService.findAllByUserId(userId);

      // then
      assertThat(result).isNotNull();
      then(channelRepository).should().findAllByTypeOrIdIn(any(), any());
    }
  }

  @Nested
  @DisplayName("채널 삭제 테스트")
  class DeleteChannel {

    @Test
    @DisplayName("성공: 채널과 관련된 메시지, 읽기 상태를 모두 삭제")
    void delete_success() {
      // given
      UUID channelId = UUID.randomUUID();
      given(channelRepository.existsById(channelId)).willReturn(true);

      // when
      channelService.delete(channelId);

      // then
      then(messageRepository).should().deleteAllByChannelId(channelId);
      then(readStatusRepository).should().deleteAllByChannelId(channelId);
      then(channelRepository).should().deleteById(channelId);
    }
  }
}