package com.sprint.mission.discodeit.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.dto.request.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.dto.request.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("채널 통합 테스트")
class ChannelIntegrationTest {

  @Autowired
  private MockMvc mockMvc;
  @Autowired
  private ObjectMapper objectMapper;
  @Autowired
  private ChannelRepository channelRepository;
  @Autowired
  private UserRepository userRepository;

  @Test
  @DisplayName("성공: 공개 채널을 생성하고 목록에서 확인한다")
  void createPublicChannel_Success() throws Exception {
    // given
    PublicChannelCreateRequest request = new PublicChannelCreateRequest("자바공부방", "자바자바자바");

    // when & then
    mockMvc.perform(post("/api/channels/public")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.name").value("자바공부방"));

    assertThat(channelRepository.findAll()).anyMatch(c -> c.getName().equals("자바공부방"));
  }

  @Test
  @DisplayName("성공: 비공개 채널을 생성한다")
  void createPrivateChannel_Success() throws Exception {
    User user = userRepository.save(new User("tester", "test@test.com", "password123", null));

    PrivateChannelCreateRequest request = new PrivateChannelCreateRequest(
        List.of(user.getId())
    );

    // when & then
    mockMvc.perform(post("/api/channels/private")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.type").value("PRIVATE"));
  }

  @Test
  @DisplayName("실패: 존재하지 않는 채널 수정 시 404 에러를 반환한다")
  void updateChannel_Fail_NotFound() throws Exception {
    // given
    UUID nonExistId = UUID.randomUUID();
    String updateBody = "{\"newName\":\"새이름\"}";

    // when & then
    mockMvc.perform(patch("/api/channels/{id}", nonExistId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(updateBody))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("CHANNEL_NOT_FOUND"));
  }

  @Test
  @DisplayName("성공: 채널을 삭제한다")
  void deleteChannel_Success() throws Exception {
    //Given
    com.sprint.mission.discodeit.entity.Channel channel =
        channelRepository.save(new com.sprint.mission.discodeit.entity.Channel(
            com.sprint.mission.discodeit.entity.ChannelType.PUBLIC, "삭제할채널", "설명"));

    //When & Then
    mockMvc.perform(delete("/api/channels/{id}", channel.getId()))
        .andExpect(status().isNoContent()); // 보통 삭제 성공은 204 No Content를 사용합니다.

    assertThat(channelRepository.findById(channel.getId())).isEmpty();
  }

}