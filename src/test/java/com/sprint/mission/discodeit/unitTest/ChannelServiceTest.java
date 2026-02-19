package com.sprint.mission.discodeit.unitTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import com.sprint.mission.discodeit.DTO.dto.ChannelDto;
import com.sprint.mission.discodeit.DTO.request.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.DTO.request.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.DTO.request.PublicChannelUpdateRequest;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.exception.ChannelExcption.ChannelNotFoundException;
import com.sprint.mission.discodeit.exception.ChannelExcption.PrivateChannelModificationNotAllowedException;
import com.sprint.mission.discodeit.mapper.ChannelMapper;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.basic.BasicChannelService;
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
class ChannelServiceTest {

  @InjectMocks
  private BasicChannelService channelService;

  @Mock
  private ChannelRepository channelRepository;
  @Mock
  private UserRepository userRepository;
  @Mock
  private MessageRepository messageRepository;
  @Mock
  private ReadStatusRepository readStatusRepository;
  @Mock
  private ChannelMapper channelMapper;

  private PublicChannelCreateRequest publicCreateRequest;
  private PrivateChannelCreateRequest privateCreateRequest;
  private PublicChannelUpdateRequest updateRequest;
  private Channel publicChannel;
  private Channel privateChannel;
  private ChannelDto publicChannelDto;
  private ChannelDto privateChannelDto;

  @BeforeEach
  void setUp() {
    publicCreateRequest = new PublicChannelCreateRequest("General", "Public Channel");
    privateCreateRequest = new PrivateChannelCreateRequest(Collections.emptyList());
    updateRequest = new PublicChannelUpdateRequest("Updated Channel", "Updated Description");
    publicChannel = new Channel("General", "Public Channel", ChannelType.PUBLIC);
    privateChannel = new Channel(null, null, ChannelType.PRIVATE);

    ReflectionTestUtils.setField(publicChannel, "id", UUID.randomUUID());
    ReflectionTestUtils.setField(privateChannel, "id", UUID.randomUUID());

    publicChannelDto = new ChannelDto(publicChannel.getId(), ChannelType.PUBLIC, "General",
        "Public Channel", Collections.emptyList(), null);
    privateChannelDto = new ChannelDto(privateChannel.getId(), ChannelType.PRIVATE, null, null,
        Collections.emptyList(), null);
  }

  @Test
  @DisplayName("Public 채널 생성 성공")
  void create_public_channel_success() {
    // given
    given(channelRepository.save(any(Channel.class))).willReturn(publicChannel);
    given(messageRepository.findTopByChannelIdOrderByCreatedAtDesc(publicChannel.getId()))
        .willReturn(Optional.empty());

    //지정한 객체인지 확인하기 위해 eq 사용
    given(channelMapper.toDto(eq(publicChannel), any(), any()))
        .willReturn(publicChannelDto);

    // when
    ChannelDto result = channelService.create(publicCreateRequest);

    // then
    assertThat(result).isNotNull();
    assertThat(result.name()).isEqualTo(publicCreateRequest.name());
    assertThat(result.description()).isEqualTo(publicCreateRequest.description());
    assertThat(result.type()).isEqualTo(ChannelType.PUBLIC);

    then(channelRepository).should().save(any(Channel.class));
  }

  @Test
  @DisplayName("Private 채널 생성 테스트 - 성공")
  void create_private_channel_success() {
    // given
    given(channelRepository.save(any(Channel.class))).willReturn(privateChannel);
    given(messageRepository.findTopByChannelIdOrderByCreatedAtDesc(privateChannel.getId()))
        .willReturn(Optional.empty());

    //지정한 객체인지 확인하기 위해 eq 사용
    given(channelMapper.toDto(eq(privateChannel), any(), any()))
        .willReturn(privateChannelDto);

    // when
    ChannelDto result = channelService.create(privateCreateRequest);

    // then
    assertThat(result).isNotNull();
    assertThat(result.type()).isEqualTo(ChannelType.PRIVATE);

    then(channelRepository).should().save(any(Channel.class));
  }

  @Test
  @DisplayName("채널 정보 수정 테스트 - 성공")
  void update_channel_success() {
    // given
    UUID channelId = publicChannel.getId();
    String newName = updateRequest.newName();
    String newDescription = updateRequest.newDescription();

    given(channelRepository.findById(channelId)).willReturn(Optional.ofNullable(publicChannel));
    given(channelRepository.save(any(Channel.class))).willReturn(publicChannel);
    given(messageRepository.findTopByChannelIdOrderByCreatedAtDesc(channelId))
        .willReturn(Optional.empty());
    given(channelMapper.toDto(eq(publicChannel), any(), any())).willAnswer(invocationOnMock -> {
      Channel channel = invocationOnMock.getArgument(0);
      return new ChannelDto(channel.getId(), channel.getType(), channel.getName(),
          channel.getDescription(), Collections.emptyList(), null);
    });

    //when
    ChannelDto result = channelService.update(channelId, updateRequest);

    //then
    assertThat(result).isNotNull();
    assertThat(result.name()).isEqualTo(newName);
    assertThat(result.description()).isEqualTo(newDescription);

    then(channelRepository).should().save(any(Channel.class));
  }

  @Test
  @DisplayName("채널 정보 수정 테스트 - 실패(존재하지 않는 채널)")
  void update_channel_fail_not_found() {
    // given
    UUID channelId = publicChannel.getId();
    given(channelRepository.findById(channelId)).willReturn(Optional.empty());

    // when
    assertThatThrownBy(() -> channelService.update(channelId, updateRequest))
        .isInstanceOf(ChannelNotFoundException.class);

    //then
    then(channelRepository).should(never()).save(any());
  }

  @Test
  @DisplayName("채널 정보 수정 테스트 - 실패(Private 채널은 수정할 수 없음)")
  void update_channel_fail_private() {
    // given
    UUID channelId = privateChannel.getId();
    given(channelRepository.findById(channelId)).willReturn(Optional.ofNullable(privateChannel));
    // when
    assertThatThrownBy(() -> channelService.update(channelId, updateRequest))
        .isInstanceOf(PrivateChannelModificationNotAllowedException.class);
    //then
    then(channelRepository).should(never()).save(any());
  }

  @Test
  @DisplayName("채널 삭제 테스트 - 성공")
  void delete_channel_success() {
    // given
    UUID channelId = publicChannel.getId();
    given(channelRepository.findById(channelId)).willReturn(Optional.ofNullable(publicChannel));

    // when
    channelService.delete(channelId);

    // then
    then(channelRepository).should().delete(publicChannel);
  }

  @Test
  @DisplayName("채널 삭제 테스트 - 실패(존재하지 않는 채널)")
  void delete_channel_fail_not_found() {
    // given
    UUID channelId = publicChannel.getId();
    given(channelRepository.findById(channelId)).willReturn(Optional.empty());

    // when
    assertThatThrownBy(() -> channelService.delete(channelId))
        .isInstanceOf(ChannelNotFoundException.class);

    // then
    then(channelRepository).should(never()).delete(any());
  }

  @Test
  @DisplayName("채널 조회 테스트 - 성공")
  void find_channel_success() {
    // given
    UUID userId = UUID.randomUUID();

    Channel publicChannel = new Channel("General", "Public Channel", ChannelType.PUBLIC);
    //구독한 채널
    Channel subPrivateChannel = new Channel(null, null, ChannelType.PRIVATE);
    //구독하지 않은 채널
    Channel nonSubPrivateChannel = new Channel(null, null, ChannelType.PRIVATE);

    ReflectionTestUtils.setField(publicChannel, "id", UUID.randomUUID());
    ReflectionTestUtils.setField(subPrivateChannel, "id", UUID.randomUUID());
    ReflectionTestUtils.setField(nonSubPrivateChannel, "id", UUID.randomUUID());

  }
}
