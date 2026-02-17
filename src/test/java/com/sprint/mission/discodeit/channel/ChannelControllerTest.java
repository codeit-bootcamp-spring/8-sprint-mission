package com.sprint.mission.discodeit.channel;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.dto.data.ChannelDto;
import com.sprint.mission.discodeit.dto.request.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.request.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.dto.request.PublicChannelUpdateRequest;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.exception.GlobalExceptionHandler;
import com.sprint.mission.discodeit.service.ChannelService;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = com.sprint.mission.discodeit.controller.ChannelController.class)
@DisplayName("ChannelController 슬라이스 테스트")
class ChannelControllerTest {

  @org.springframework.boot.SpringBootConfiguration
  @org.springframework.context.annotation.Import({
      com.sprint.mission.discodeit.controller.ChannelController.class,
      GlobalExceptionHandler.class
  })
  static class Config {}

  @Autowired
  private MockMvc mockMvc;
  @Autowired
  private ObjectMapper objectMapper;
  @MockBean
  private ChannelService channelService;

  private static final UUID CHANNEL_ID = UUID.randomUUID();
  private static final UUID USER_ID = UUID.randomUUID();

  @Nested
  @DisplayName("GET /api/channels")
  class FindAllByUserId {

    @Test
    @DisplayName("성공: userId로 채널 목록 조회 시 200과 JSON 배열 반환")
    void success() throws Exception {
      ChannelDto dto = new ChannelDto(CHANNEL_ID, ChannelType.PUBLIC, "채널", "설명", List.of(), List.of(), null);
      given(channelService.findAllByUserId(USER_ID)).willReturn(List.of(dto));

      mockMvc.perform(get("/api/channels").param("userId", USER_ID.toString()))
          .andExpect(status().isOk())
          .andExpect(content().contentType(MediaType.APPLICATION_JSON))
          .andExpect(jsonPath("$").isArray())
          .andExpect(jsonPath("$[0].name").value("채널"))
          .andExpect(jsonPath("$[0].type").value("PUBLIC"));
    }

  }

  @Nested
  @DisplayName("POST /api/channels/public")
  class CreatePublic {

    @Test
    @DisplayName("성공: 유효한 요청으로 공개 채널 생성 시 200과 JSON 본문 반환")
    void success() throws Exception {
      PublicChannelCreateRequest request = new PublicChannelCreateRequest("공개채널", "설명");
      ChannelDto dto = new ChannelDto(CHANNEL_ID, ChannelType.PUBLIC, "공개채널", "설명", List.of(), List.of(), null);
      given(channelService.create(any(PublicChannelCreateRequest.class))).willReturn(dto);

      mockMvc.perform(post("/api/channels/public")
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isOk())
          .andExpect(content().contentType(MediaType.APPLICATION_JSON))
          .andExpect(jsonPath("$.name").value("공개채널"))
          .andExpect(jsonPath("$.type").value("PUBLIC"))
          .andExpect(jsonPath("$.description").value("설명"));
    }

    @Test
    @DisplayName("실패: 채널명 누락 시 400과 검증 오류 JSON 반환")
    void fail_validation() throws Exception {
      String body = "{\"name\":\"\",\"description\":\"설명\"}";

      mockMvc.perform(post("/api/channels/public")
              .contentType(MediaType.APPLICATION_JSON)
              .content(body))
          .andExpect(status().isBadRequest())
          .andExpect(content().contentType(MediaType.APPLICATION_JSON))
          .andExpect(jsonPath("$.code").exists())
          .andExpect(jsonPath("$.message").exists());
    }
  }

  @Nested
  @DisplayName("POST /api/channels/private")
  class CreatePrivate {

    @Test
    @DisplayName("성공: 참가자 ID 목록으로 비공개 채널 생성 시 200과 JSON 반환")
    void success() throws Exception {
      PrivateChannelCreateRequest request = new PrivateChannelCreateRequest(List.of(USER_ID));
      ChannelDto dto = new ChannelDto(CHANNEL_ID, ChannelType.PRIVATE, null, null, List.of(), List.of(), null);
      given(channelService.create(any(PrivateChannelCreateRequest.class))).willReturn(dto);

      mockMvc.perform(post("/api/channels/private")
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.type").value("PRIVATE"));
    }
  }

  @Nested
  @DisplayName("PATCH /api/channels/{channelId}")
  class Update {

    @Test
    @DisplayName("성공: 채널 수정 시 200과 수정된 채널 JSON 반환")
    void success() throws Exception {
      PublicChannelUpdateRequest request = new PublicChannelUpdateRequest("새이름", "새설명");
      ChannelDto dto = new ChannelDto(CHANNEL_ID, ChannelType.PUBLIC, "새이름", "새설명", List.of(), List.of(), null);
      given(channelService.update(eq(CHANNEL_ID), any(PublicChannelUpdateRequest.class))).willReturn(dto);

      mockMvc.perform(patch("/api/channels/{channelId}", CHANNEL_ID)
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.name").value("새이름"))
          .andExpect(jsonPath("$.description").value("새설명"));
    }

    @Test
    @DisplayName("실패: 요청 본문 유효성 위반 시 400과 오류 JSON 반환")
    void fail_validation() throws Exception {
      String body = "{\"newName\":\"\",\"newDescription\":\"d\"}";

      mockMvc.perform(patch("/api/channels/{channelId}", CHANNEL_ID)
              .contentType(MediaType.APPLICATION_JSON)
              .content(body))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.code").exists());
    }
  }

  @Nested
  @DisplayName("DELETE /api/channels/{channelId}")
  class Delete {

    @Test
    @DisplayName("성공: 채널 삭제 시 204 No Content")
    void success() throws Exception {
      mockMvc.perform(delete("/api/channels/{channelId}", CHANNEL_ID))
          .andExpect(status().isNoContent());
    }
  }
}
