package com.sprint.mission.discodeit.slice.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.sprint.mission.discodeit.config.AppConfig;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@Import(AppConfig.class)
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("ChannelRepository 테스트")
public class ChannelRepositoryTest {

  @Autowired
  private ChannelRepository channelRepository;

  private Channel publicChannel;
  private Channel subChannel;
  private Channel nonSubChannel;

  @BeforeEach
  public void setup() {
    publicChannel = new Channel(ChannelType.PUBLIC, "공개 채널", "공개 채널입니다");
    subChannel = new Channel(ChannelType.PRIVATE);
    nonSubChannel = new Channel(ChannelType.PRIVATE);

    channelRepository.save(publicChannel);
    channelRepository.save(subChannel);
    channelRepository.save(nonSubChannel);
  }

  @Test
  @DisplayName("공개 채널과 사용자가 참여한 채널 목록 반환")
  void findAllPublicOrSubscribed() {

    // given
    List<UUID> subscribedIds = List.of(subChannel.getId());

    // when
    List<Channel> result = channelRepository.findAllPublicOrSubscribed(subscribedIds);

    // then
    assertThat(result).hasSize(2);
    assertThat(result).extracting(Channel::getId)
        .containsExactlyInAnyOrder(publicChannel.getId(), subChannel.getId())
        .doesNotContain(nonSubChannel.getId());
  }

  @Test
  @DisplayName("구독 채널이 없는 경우 공개 채널만 조회")
  void findAllPublicOrSubscribed_ShouldReturnPublicChannel() {

    // when
    List<Channel> result = channelRepository.findAllPublicOrSubscribed(List.of());

    // then
    assertThat(result.size()).isEqualTo(1);
    assertThat(result.get(0).getName()).isEqualTo(publicChannel.getName());
  }
}
