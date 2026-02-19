package com.sprint.mission.discodeit.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.sprint.mission.discodeit.config.QueryDslConfig;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
@Import(QueryDslConfig.class)
@EnableJpaAuditing
class ChannelRepositoryTest {

  @Autowired
  private TestEntityManager entityManager;

  @Autowired
  private ChannelRepository channelRepository;

  private Channel createAndSaveTestChannel(ChannelType type, String name) {
    Channel channel = new Channel(name, "Description for " + name, type);
    return channelRepository.saveAndFlush(channel);
  }

  @Test
  @DisplayName("Public 채널 저장 테스트")
  void save_PublicChannel_Success() {
    // given
    Channel publicChannel = new Channel("general", "Public discussion channel", ChannelType.PUBLIC);

    // when
    Channel savedChannel = channelRepository.save(publicChannel);
    entityManager.flush();
    entityManager.clear();

    // then
    Optional<Channel> foundChannelOpt = channelRepository.findById(savedChannel.getId());
    assertThat(foundChannelOpt).isPresent();
    Channel foundChannel = foundChannelOpt.get();

    assertThat(foundChannel.getName()).isEqualTo("general");
    assertThat(foundChannel.getType()).isEqualTo(ChannelType.PUBLIC);
    assertThat(foundChannel.getCreatedAt()).isNotNull();
  }

  @Test
  @DisplayName("타입이 PUBLIC이거나 ID 목록에 포함된 채널을 모두 조회")
  void findAllByTypeOrIdIn_ReturnsPublicAndSelectedPrivateChannels() {
    // given
    Channel publicChannel1 = createAndSaveTestChannel(ChannelType.PUBLIC, "공개채널1");
    Channel publicChannel2 = createAndSaveTestChannel(ChannelType.PUBLIC, "공개채널2");
    Channel privateChannel1 = createAndSaveTestChannel(ChannelType.PRIVATE, "비공개채널1");
    createAndSaveTestChannel(ChannelType.PRIVATE, "비공개채널2");

    entityManager.clear();

    // when
    List<UUID> selectedPrivateIds = List.of(privateChannel1.getId());
    List<Channel> foundChannels = channelRepository.findAllByTypeOrIdIn(ChannelType.PUBLIC,
        selectedPrivateIds);

    // then
    assertThat(foundChannels).hasSize(3);
    assertThat(foundChannels).extracting(Channel::getName)
        .containsExactlyInAnyOrder("공개채널1", "공개채널2", "비공개채널1");
  }

  @Test
  @DisplayName("ID 목록이 비어있으면 타입이 PUBLIC인 채널만 조회")
  void findAllByTypeOrIdIn_EmptyIdList_ReturnsOnlyPublicChannels() {
    // given
    createAndSaveTestChannel(ChannelType.PUBLIC, "공개채널1");
    createAndSaveTestChannel(ChannelType.PUBLIC, "공개채널2");
    createAndSaveTestChannel(ChannelType.PRIVATE, "비공개채널1");

    entityManager.clear();

    // when
    List<Channel> foundChannels = channelRepository.findAllByTypeOrIdIn(ChannelType.PUBLIC,
        List.of());

    // then
    assertThat(foundChannels).hasSize(2);
    assertThat(foundChannels).allMatch(c -> c.getType() == ChannelType.PUBLIC);
  }
}
