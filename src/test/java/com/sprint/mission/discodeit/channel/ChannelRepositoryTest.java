package com.sprint.mission.discodeit.channel;

import static org.assertj.core.api.Assertions.assertThat;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
@EnableJpaAuditing
@EntityScan(basePackages = "com.sprint.mission.discodeit.entity")
@EnableJpaRepositories(basePackages = "com.sprint.mission.discodeit.repository")
@DisplayName("ChannelRepository 슬라이스 테스트")
class ChannelRepositoryTest {

  @Autowired
  private ChannelRepository channelRepository;

  @Nested
  @DisplayName("findById (JpaRepository)")
  class FindById {

    @Test
    @DisplayName("성공: 존재하는 ID로 조회 시 Channel 반환")
    void success() {
      Channel channel = new Channel(ChannelType.PUBLIC, "공개채널", "설명");
      Channel saved = channelRepository.save(channel);

      Optional<Channel> found = channelRepository.findById(saved.getId());
      assertThat(found).isPresent();
      assertThat(found.get().getName()).isEqualTo("공개채널");
    }

    @Test
    @DisplayName("실패: 존재하지 않는 ID로 조회 시 Optional.empty")
    void fail_notFound() {
      Optional<Channel> found = channelRepository.findById(UUID.randomUUID());
      assertThat(found).isEmpty();
    }
  }

  @Nested
  @DisplayName("findAllByTypeOrIdIn (커스텀 쿼리 메소드)")
  class FindAllByTypeOrIdIn {

    @Test
    @DisplayName("성공: PUBLIC 타입 + ID 목록에 해당하는 채널 목록 반환")
    void success() {
      Channel c1 = channelRepository.save(new Channel(ChannelType.PUBLIC, "A", null));
      Channel c2 = channelRepository.save(new Channel(ChannelType.PRIVATE, "B", null));
      Channel c3 = channelRepository.save(new Channel(ChannelType.PUBLIC, "C", null));

      List<Channel> result = channelRepository.findAllByTypeOrIdIn(
          ChannelType.PUBLIC, List.of(c2.getId()));
      assertThat(result).hasSize(3);
      assertThat(result).extracting(Channel::getName).containsExactlyInAnyOrder("A", "B", "C");
    }

    @Test
    @DisplayName("실패: 조건에 맞는 채널이 없으면 빈 목록 반환")
    void fail_empty() {
      List<Channel> result = channelRepository.findAllByTypeOrIdIn(
          ChannelType.PUBLIC, List.of());
      assertThat(result).isEmpty();
    }
  }
}
