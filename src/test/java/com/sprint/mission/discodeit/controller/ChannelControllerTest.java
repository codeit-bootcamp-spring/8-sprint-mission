package com.sprint.mission.discodeit.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.dto.channel.ChannelCreatePublicRequest;
import com.sprint.mission.discodeit.dto.channel.ChannelDto;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.service.ChannelService;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ChannelController.class)
@ActiveProfiles("test")
class ChannelControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private ChannelService channelService;

  @MockitoBean
  private JpaMetamodelMappingContext jpaMappingContext;

  @Test
  @WithMockUser(roles = "CHANNEL_MANAGER")
  @DisplayName("POST /api/channels/public: 성공 - 201 + JSON 응답")
  void createPublic_success() throws Exception {
    UUID id = UUID.randomUUID();
    ChannelDto response = new ChannelDto(id, ChannelType.PUBLIC, "pubChannel",
        "Channel Description", List.of(), null);

    when(channelService.createPublicChannel(any())).thenReturn(response);

    ChannelCreatePublicRequest req = new ChannelCreatePublicRequest("pubChannel",
        "Channel Description");

    mockMvc.perform(
            post("/api/channels/public")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(req))
                .with(csrf()) // CSRF 토큰 추가
        )
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(id.toString()));
  }

  @Test
  @WithMockUser(roles = "CHANNEL_MANAGER")
  @DisplayName("POST /api/channels/public: 실패 - validation(빈 name) -> 400")
  void createPublic_fail_validation() throws Exception {
    ChannelCreatePublicRequest req = new ChannelCreatePublicRequest("", "d");

    mockMvc.perform(
            post("/api/channels/public")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(req))
                .with(csrf())
        )
        .andExpect(status().isBadRequest());
  }
}
