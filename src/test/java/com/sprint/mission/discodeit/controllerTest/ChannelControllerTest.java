package com.sprint.mission.discodeit.controllerTest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.sprint.mission.discodeit.DTO.dto.ChannelDto;
import com.sprint.mission.discodeit.DTO.request.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.exception.ChannelExcption.ChannelNotFoundException;
import java.util.Collections;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;

public class ChannelControllerTest extends ControllerTestSupport {

  @Test
  @WithMockUser
  @DisplayName("POST /api/channels/public - Public 채널 생성 성공")
  void createPublicChannel_success() throws Exception {
    // given
    PublicChannelCreateRequest request = new PublicChannelCreateRequest("General",
        "public channel");
    ChannelDto channelDto = new ChannelDto(UUID.randomUUID(), ChannelType.PUBLIC, "General",
        "public channel",
        Collections.emptyList(), null);

    given(channelService.create(any(PublicChannelCreateRequest.class))).willReturn(channelDto);

    // when & then
    mockMvc.perform(post("/api/channels/public")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request))
            .with(csrf()))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.name").value("General"))
        .andExpect(jsonPath("$.type").value("PUBLIC"));
  }

  @Test
  @WithMockUser
  @DisplayName("POST /api/channels/public - Public 채널 생성 실패 (유효성 검사)")
  void createPublicChannel_fail_validation() throws Exception {
    // given
    PublicChannelCreateRequest request = new PublicChannelCreateRequest("G", "public channel");

    // when & then
    mockMvc.perform(post("/api/channels/public")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request))
            .with(csrf()))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.details.name").exists());
  }

  @Test
  @WithMockUser
  @DisplayName("DELETE /api/channels/{channelId} - 채널 삭제 성공")
  void deleteChannel_success() throws Exception {
    // given
    UUID channelId = UUID.randomUUID();

    //when & then
    mockMvc.perform(delete("/api/channels/{channelId}", channelId)
            .with(csrf()))
        .andExpect(status().isNoContent());
  }

  @Test
  @WithMockUser
  @DisplayName("DELETE /api/channels/{channelId} - 채널 삭제 실패 (존재하지 않는 채널)")
  void deleteChannel_fail_not_found() throws Exception {
    // given
    UUID channelId = UUID.randomUUID();
    willThrow(new ChannelNotFoundException(channelId))
        .given(channelService).delete(eq(channelId));

    // when & then
    mockMvc.perform(delete("/api/channels/{channelId}", channelId)
            .with(csrf()))
        .andExpect(status().isNotFound());
  }
}
