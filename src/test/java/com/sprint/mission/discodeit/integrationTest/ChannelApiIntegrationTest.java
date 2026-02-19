package com.sprint.mission.discodeit.integrationTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.DTO.dto.UserDto;
import com.sprint.mission.discodeit.DTO.request.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.DTO.request.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.DTO.request.UserCreateRequest;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.service.ChannelService;
import com.sprint.mission.discodeit.service.UserService;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class ChannelApiIntegrationTest {

  @Autowired
  private MockMvc mockMvc;
  @Autowired
  private ObjectMapper objectMapper;
  @Autowired
  private ChannelService channelService;
  @Autowired
  private UserService userService;
  @Autowired
  private ChannelRepository channelRepository;

  @Test
  @WithMockUser
  @DisplayName("POST /api/channels - Public 채널 생성 통합 테스트")
  void createChannel_integration_success() throws Exception {
    // given
    PublicChannelCreateRequest request = new PublicChannelCreateRequest("test", "test description");

    mockMvc.perform(post("/api/channels/public").contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)).with(csrf()))
        .andExpect(status().isCreated()).andExpect(jsonPath("$.name").value("test"))
        .andExpect(jsonPath("$.description").value("test description"));
  }

  @Test
  @WithMockUser
  @DisplayName("POST /api/channels - Public 채널 생성 통합 테스트 실패 (유효성 검증 실패)")
  void createChannel_integration_fail_validation() throws Exception {
    // given
    PublicChannelCreateRequest request = new PublicChannelCreateRequest("t", "test description");

    mockMvc.perform(post("/api/channels/public").contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)).with(csrf()))
        .andExpect(status().isBadRequest());
  }

  @Test
  @WithMockUser
  @DisplayName("GET /api/channels - 사용자별 채널 목록 조회 통합 테스트")
  void findAllChannelsByUserId_integration_success() throws Exception {
    //given
    UserCreateRequest user1CreateRequest = new UserCreateRequest("channelUser1",
        "channelUser1@codeit.com", "password1234");

    UserCreateRequest user2CreateRequest = new UserCreateRequest("channelUser2",
        "channelUser2@codeit.com", "password1234");

    UserDto user1 = userService.create(user1CreateRequest, Optional.empty());
    UserDto user2 = userService.create(user2CreateRequest, Optional.empty());

    PublicChannelCreateRequest publicChannelRequest = new PublicChannelCreateRequest(
        "public channel", "public channel description");

    PrivateChannelCreateRequest privateChannelRequest = new PrivateChannelCreateRequest(
        List.of(user1.id(), user2.id()));

    channelService.create(publicChannelRequest);
    channelService.create(privateChannelRequest);

    // when & then
    mockMvc.perform(get("/api/channels").param("userId", user1.id().toString()).with(csrf()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(2))
        .andExpect(jsonPath("$[0].name").value("public channel"))
        .andExpect(jsonPath("$[0].description").value("public channel description"))
        .andExpect(jsonPath("$[0].type").value("PUBLIC"))
        .andExpect(jsonPath("$[1].type").value("PRIVATE"));
  }


  @Test
  @WithMockUser
  @DisplayName("DELETE /api/channels/{channelId} - 채널 삭제 통합 테스트")
  void deleteChannel_integration_success() throws Exception {
    // given
    Channel savedChannel = channelRepository.save(
        new Channel("test", "test description", ChannelType.PUBLIC));

    // when & then
    mockMvc.perform(delete("/api/channels/{channelId}", savedChannel.getId()).with(csrf()))
        .andExpect(status().isNoContent());

    assertThat(channelRepository.findById(savedChannel.getId())).isEmpty();
  }

  @Test
  @WithMockUser
  @DisplayName("DELETE /api/channels/{channelId} - 채널 삭제 통합 테스트 실패 (존재하지 않는 채널")
  void deleteChannel_integration_fail_channelNotFound() throws Exception {
    // given
    UUID channelId = UUID.randomUUID();

    mockMvc.perform(delete("/api/channels/{channelId}", channelId))
        .andExpect(status().isNotFound());
  }
}
