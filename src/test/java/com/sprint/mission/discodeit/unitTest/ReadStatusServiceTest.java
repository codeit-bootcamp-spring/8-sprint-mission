package com.sprint.mission.discodeit.unitTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.sprint.mission.discodeit.DTO.dto.ReadStatusDto;
import com.sprint.mission.discodeit.DTO.request.ReadStatusCreateRequest;
import com.sprint.mission.discodeit.DTO.request.ReadStatusUpdateRequest;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.ReadStatusException.ReadStatusAlreadyExists;
import com.sprint.mission.discodeit.mapper.ReadStatusMapper;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.basic.BasicReadStatusService;
import java.time.Instant;
import java.util.List;
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
class ReadStatusServiceTest {

  @InjectMocks
  private BasicReadStatusService readStatusService;

  @Mock
  private ReadStatusRepository readStatusRepository;
  @Mock
  private UserRepository userRepository;
  @Mock
  private ChannelRepository channelRepository;
  @Mock
  private ReadStatusMapper readStatusMapper;

  private User user;
  private Channel channel;
  private ReadStatus readStatus;

  @BeforeEach
  void setUp() {
    user = new User("testUser", "test@example.com", "password123", null);
    ReflectionTestUtils.setField(user, "id", UUID.randomUUID());

    channel = new Channel("test channel", "description", ChannelType.PUBLIC);
    ReflectionTestUtils.setField(channel, "id", UUID.randomUUID());

    readStatus = new ReadStatus(user, channel, Instant.now());
    ReflectionTestUtils.setField(readStatus, "id", UUID.randomUUID());
  }

  @Test
  @DisplayName("읽음 상태 생성 성공")
  void createReadStatus_success() {
    // given
    ReadStatusCreateRequest request = new ReadStatusCreateRequest(user.getId(), channel.getId(),
        Instant.now());
    ReadStatusDto dto = new ReadStatusDto(readStatus.getId(), user.getId(), channel.getId(),
        readStatus.getLastReadAt());

    given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
    given(channelRepository.findById(channel.getId())).willReturn(Optional.of(channel));
    given(
        readStatusRepository.existsByUserIdAndChannelId(user.getId(), channel.getId())).willReturn(
        false);
    given(readStatusRepository.save(any(ReadStatus.class))).willReturn(readStatus);
    given(readStatusMapper.toDto(any(ReadStatus.class))).willReturn(dto);

    // when
    ReadStatusDto result = readStatusService.create(request);

    // then
    assertThat(result).isNotNull();
    assertThat(result.userId()).isEqualTo(user.getId());
    assertThat(result.channelId()).isEqualTo(channel.getId());
    then(readStatusRepository).should().save(any(ReadStatus.class));
  }

  @Test
  @DisplayName("읽음 상태 생성 실패 - 이미 존재함")
  void createReadStatus_fail_alreadyExists() {
    // given
    ReadStatusCreateRequest request = new ReadStatusCreateRequest(user.getId(), channel.getId(),
        Instant.now());

    given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
    given(channelRepository.findById(channel.getId())).willReturn(Optional.of(channel));
    given(
        readStatusRepository.existsByUserIdAndChannelId(user.getId(), channel.getId())).willReturn(
        true);

    // when & then
    assertThatThrownBy(() -> readStatusService.create(request))
        .isInstanceOf(ReadStatusAlreadyExists.class);
  }

  @Test
  @DisplayName("읽음 상태 업데이트 성공")
  void updateReadStatus_success() {
    // given
    Instant newTime = Instant.now().plusSeconds(60);
    ReadStatusUpdateRequest request = new ReadStatusUpdateRequest(newTime);
    ReadStatusDto dto = new ReadStatusDto(readStatus.getId(), user.getId(), channel.getId(),
        newTime);

    given(readStatusRepository.findById(readStatus.getId())).willReturn(Optional.of(readStatus));
    given(readStatusRepository.save(any(ReadStatus.class))).willReturn(readStatus);
    given(readStatusMapper.toDto(any(ReadStatus.class))).willReturn(dto);

    // when
    ReadStatusDto result = readStatusService.update(readStatus.getId(), request);

    // then
    assertThat(result.lastReadAt()).isEqualTo(newTime);
    then(readStatusRepository).should().save(any(ReadStatus.class));
  }

  @Test
  @DisplayName("사용자 ID로 모든 읽음 상태 조회 성공")
  void findAllByUserId_success() {
    // given
    given(readStatusRepository.findAllByUserIdWithChannel(user.getId())).willReturn(
        List.of(readStatus));

    // when
    readStatusService.findAllByUserId(user.getId());

    // then
    then(readStatusRepository).should().findAllByUserIdWithChannel(user.getId());
    then(readStatusMapper).should().toDto(any(ReadStatus.class));
  }
}
