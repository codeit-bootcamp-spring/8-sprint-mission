package com.sprint.mission.discodeit.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.sprint.mission.discodeit.config.JpaConfig;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
@Import(JpaConfig.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ChannelRepositoryTest {

  @Autowired
  private ChannelRepository channelRepository;

  @Nested
  @DisplayName("findAllByTypeOrIdIn 메서드 테스트")
  class FindAllByTypeOrIdIn {

    @Test
    @DisplayName("성공: 특정 타입이거나 ID 목록에 포함된 채널들을 모두 조회한다")
    void success() {
      // given
      Channel publicChannel = channelRepository.save(
          new Channel(ChannelType.PUBLIC, "공개채널", "설명"));
      Channel privateChannel = channelRepository.save(
          new Channel(ChannelType.PRIVATE, "비밀채널", "설명"));

      // PUBLIC 타입이거나, 비밀채널의 ID를 가진 채널을 찾으라고 요청 (OR 조건)
      ChannelType typeParam = ChannelType.PUBLIC;
      List<UUID> idParams = List.of(privateChannel.getId());

      // when
      List<Channel> result = channelRepository.findAllByTypeOrIdIn(typeParam, idParams);

      // then
      assertThat(result).hasSize(2);
      assertThat(result).extracting("name")
          .containsExactlyInAnyOrder("공개채널", "비밀채널");
    }

    @Test
    @DisplayName("실패: 일치하는 타입이 없고 ID 목록도 비어있으면 빈 리스트를 반환한다")
    void fail() {
      // given
      channelRepository.save(new Channel(ChannelType.PRIVATE, "비밀채널", "설명"));

      // DB에 없는 PUBLIC 타입을 찾거나, 엉뚱한 ID를 넘김
      ChannelType typeParam = ChannelType.PUBLIC;
      List<UUID> idParams = List.of(UUID.randomUUID());

      // when
      List<Channel> result = channelRepository.findAllByTypeOrIdIn(typeParam, idParams);

      // then
      assertThat(result).isEmpty();
    }
  }
}