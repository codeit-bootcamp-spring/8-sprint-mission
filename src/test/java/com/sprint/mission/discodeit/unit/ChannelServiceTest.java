package com.sprint.mission.discodeit.unit;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

import com.sprint.mission.discodeit.dto.ChannelDto;
import com.sprint.mission.discodeit.dto.UserDto;
import com.sprint.mission.discodeit.dto.request.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.request.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.dto.request.PublicChannelUpdateRequest;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.Role;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.channel.ChannelNotFoundException;
import com.sprint.mission.discodeit.exception.channel.PrivateChannelUpdateException;
import com.sprint.mission.discodeit.mapper.ChannelMapper;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.basic.BasicChannelService;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ChannelServiceTest {

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
  private BasicChannelService basicChannelService;

  UUID channelId;
  String name;
  String description;

  @BeforeEach
  void setUp() {
    channelId = UUID.randomUUID();
    name = "Test Channel";
    description = "Test Description";
  }

  @Nested
  @DisplayName("채널 생성 관련 테스트")
  class channelCreateTests {

    @Test
    @DisplayName("공개 채널 생성 성공인 경우")
    void createPublicChannel_ShouldReturnChannelDto() {

      // given
      PublicChannelCreateRequest channelReq = new PublicChannelCreateRequest(name, description);
      ChannelDto publicDto = new ChannelDto(channelId, ChannelType.PUBLIC, name, description,
          List.of(), Instant.now());

      given(channelMapper.toDto(any(Channel.class))).willReturn(publicDto);

      // when
      ChannelDto result = basicChannelService.create(channelReq);

      // then
      assertThat(result).isEqualTo(publicDto);

      then(channelRepository).should().save(any(Channel.class));
    }

    @Test
    @DisplayName("비공개 채널 생성 성공인 경우")
    void createPrivateChannel_ShouldReturnChannelDto() {

      // given
      List<UUID> participants = List.of(UUID.randomUUID(), UUID.randomUUID());
      PrivateChannelCreateRequest channelReq = new PrivateChannelCreateRequest(participants);

      User user1 = mock(User.class);
      User user2 = mock(User.class);
      given(userRepository.findAllById(eq(participants))).willReturn(List.of(user1, user2));

      List<UserDto> participantsDto = List.of(
          new UserDto(participants.get(0), "user1", "user1@naver.com", null, Role.USER),
          new UserDto(participants.get(1), "user2", "user2@naver.com", null, Role.USER)
      );

      ChannelDto privateDto = new ChannelDto(channelId, ChannelType.PRIVATE, null, null,
          participantsDto, Instant.now());
      given(channelMapper.toDto(any(Channel.class))).willReturn(privateDto);

      // when
      ChannelDto result = basicChannelService.create(channelReq);

      // then
      assertThat(result).isEqualTo(privateDto);

      then(channelRepository).should().save(any(Channel.class));
      then(userRepository).should().findAllById(eq(participants));
      then(readStatusRepository).should().saveAll(anyList());
    }
  }

  @Nested
  @DisplayName("채널 수정 관련 테스트")
  class channelUpdateTests {

    @Test
    @DisplayName("공개 채널 수정 성공인 경우")
    void updatePublicChannel_ShouldReturnChannelDto() {

      // given
      PublicChannelUpdateRequest channelReq = new PublicChannelUpdateRequest("new channel name",
          "new description");
      Channel mockChannel = new Channel(ChannelType.PUBLIC, name, description);
      given(channelRepository.findById(eq(channelId))).willReturn(Optional.of(mockChannel));

      ChannelDto mockChannelDto = new ChannelDto(channelId, ChannelType.PUBLIC,
          channelReq.newName(), channelReq.newDescription(), List.of(), Instant.now());
      given(channelMapper.toDto(any(Channel.class))).willReturn(mockChannelDto);

      // when
      ChannelDto result = basicChannelService.update(channelId, channelReq);

      // then
      assertThat(result).isEqualTo(mockChannelDto);

      then(channelRepository).should().findById(eq(channelId));
    }

    @Test
    @DisplayName("채널 수정 실패 - 비공개 채널을 수정하는 경우")
    void updatePrivateChannel_ShouldThrowException() {
      // given
      PublicChannelUpdateRequest updateReq = new PublicChannelUpdateRequest("newName", "newDesc");

      Channel privateChannel = new Channel(ChannelType.PRIVATE);
      given(channelRepository.findById(eq(channelId))).willReturn(Optional.of(privateChannel));

      // when & then
      assertThatThrownBy(() -> basicChannelService.update(channelId, updateReq))
          .isInstanceOf(PrivateChannelUpdateException.class);
    }
  }

  @Nested
  @DisplayName("채널 삭제 관련 테스트")
  class channelDeleteTests {

    @Test
    @DisplayName("채널 삭제 성공인 경우")
    void delete_ShouldSuccess() {

      // given
      given(channelRepository.existsById(eq(channelId))).willReturn(true);

      // when
      basicChannelService.delete(channelId);

      // then
      then(channelRepository).should().existsById(eq(channelId));
      then(messageRepository).should().deleteAllByChannelId(eq(channelId));
      then(readStatusRepository).should().deleteAllByChannelId(eq(channelId));
      then(channelRepository).should().deleteById(eq(channelId));
    }

    @Test
    @DisplayName("채널 삭제 실패 - 채널을 찾지 못 했을 경우")
    void delete_WhenNotFoundChannel_shouldThrowException() {

      // given
      given(channelRepository.existsById(eq(channelId))).willReturn(false);

      // when & then
      assertThatThrownBy(() -> basicChannelService.delete(channelId))
          .isInstanceOf(ChannelNotFoundException.class);
    }
  }

  @Nested
  @DisplayName("채널 조회 관련 테스트")
  class channelFindTests {

    @Test
    @DisplayName("공개 채널 조회 성공인 경우")
    void findById_ShouldReturnChannelDto() {

      // given
      Channel mockChannel = new Channel(ChannelType.PUBLIC, "test", "test channel");
      given(channelRepository.findById(eq(channelId))).willReturn(Optional.of(mockChannel));

      ChannelDto mockChannelDto = new ChannelDto(channelId, ChannelType.PUBLIC,
          mockChannel.getName(), mockChannel.getDescription(), List.of(), Instant.now());
      given(channelMapper.toDto(any(Channel.class))).willReturn(mockChannelDto);

      // when
      ChannelDto result = basicChannelService.find(channelId);

      // then
      assertThat(result).isEqualTo(mockChannelDto);

      then(channelRepository).should().findById(eq(channelId));
    }

    @Test
    @DisplayName("채널 조회 실패 - 채널을 찾지 못 했을 경우")
    void findById_WhenNotFoundChannel_ShouldThrowException() {

      // given
      given(channelRepository.findById(eq(channelId))).willReturn(Optional.empty());

      // when & then
      assertThatThrownBy(() -> basicChannelService.find(channelId))
          .isInstanceOf(ChannelNotFoundException.class);

      then(channelRepository).should().findById(eq(channelId));
    }

    @Test
    @DisplayName("특정 사용자가 볼 수 있는 채널 목록 조회 성공인 경우")
    void findAllByUserId_ShouldReturnChannelList() {

      // given
      UUID userId = UUID.randomUUID();
      UUID subscribedChannelId = UUID.randomUUID();
      UUID publicChannelId = UUID.randomUUID();

      Channel subscribedChannel = new Channel(ChannelType.PRIVATE, "Private Channel",
          "Description");
      Channel mockSubscribedChannel = mock(Channel.class);
      given(mockSubscribedChannel.getId()).willReturn(subscribedChannelId);

      ReadStatus readStatus = mock(ReadStatus.class);
      given(readStatus.getChannel()).willReturn(mockSubscribedChannel);

      given(readStatusRepository.findAllByUserIdWithFetchJoin(userId))
          .willReturn(List.of(readStatus));

      Channel publicChannel = new Channel(ChannelType.PUBLIC, "Public Channel",
          "Public Description");
      List<Channel> visibleChannels = List.of(mockSubscribedChannel, publicChannel);

      given(channelRepository.findAllPublicOrSubscribed(anyList())).willReturn(visibleChannels);

      ChannelDto dto1 = new ChannelDto(subscribedChannelId, ChannelType.PRIVATE, "Private Channel",
          null, List.of(), Instant.now());
      ChannelDto dto2 = new ChannelDto(publicChannelId, ChannelType.PUBLIC, "Public Channel",
          "Public Description", List.of(), Instant.now());

      given(channelMapper.toDto(mockSubscribedChannel)).willReturn(dto1);
      given(channelMapper.toDto(publicChannel)).willReturn(dto2);

      // when
      List<ChannelDto> result = basicChannelService.findAllByUserId(userId);

      // then
      assertThat(result).isNotNull();
      assertThat(result.size()).isEqualTo(2);
      assertThat(result.get(0)).isEqualTo(dto1);
      assertThat(result.get(1)).isEqualTo(dto2);

      then(readStatusRepository).should().findAllByUserIdWithFetchJoin(userId);
      then(channelRepository).should().findAllPublicOrSubscribed(anyList());
    }
  }
}
