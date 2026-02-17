package com.sprint.mission.discodeit.channel;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.controller.ChannelController;
import com.sprint.mission.discodeit.dto.data.ChannelDto;
import com.sprint.mission.discodeit.dto.request.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.request.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.dto.request.PublicChannelUpdateRequest;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.service.ChannelService;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
@DisplayName("ChannelController 단위 테스트")
class ChannelControllerTest {

  private MockMvc mockMvc;
  private final ObjectMapper objectMapper = new ObjectMapper();

  @Mock
  private ChannelService channelService;
  @InjectMocks
  private ChannelController channelController;

  private static final UUID CHANNEL_ID = UUID.randomUUID();
  private static final UUID USER_ID = UUID.randomUUID();

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.standaloneSetup(channelController).build();
  }

  @Nested
  @DisplayName("GET /api/channels")
  class FindAllByUserId {

    @Test
    @DisplayName("성공: userId로 채널 목록 반환")
    void success() throws Exception {
      ChannelDto dto = new ChannelDto(CHANNEL_ID, ChannelType.PUBLIC, "채널", "설명", List.of(), List.of(), null);
      given(channelService.findAllByUserId(USER_ID)).willReturn(List.of(dto));

      mockMvc.perform(get("/api/channels").param("userId", USER_ID.toString()))
          .andExpect(status().isOk())
          .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }
  }

  @Nested
  @DisplayName("POST /api/channels/public")
  class CreatePublic {

    @Test
    @DisplayName("성공: 공개 채널 생성")
    void success() throws Exception {
      PublicChannelCreateRequest request = new PublicChannelCreateRequest("공개채널", "설명");
      ChannelDto dto = new ChannelDto(CHANNEL_ID, ChannelType.PUBLIC, "공개채널", "설명", List.of(), List.of(), null);
      given(channelService.create(any(PublicChannelCreateRequest.class))).willReturn(dto);

      mockMvc.perform(post("/api/channels/public")
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isOk())
          .andExpect(content().json(objectMapper.writeValueAsString(dto)));
    }
  }

  @Nested
  @DisplayName("POST /api/channels/private")
  class CreatePrivate {

    @Test
    @DisplayName("성공: 비공개 채널 생성")
    void success() throws Exception {
      PrivateChannelCreateRequest request = new PrivateChannelCreateRequest(List.of(USER_ID));
      ChannelDto dto = new ChannelDto(CHANNEL_ID, ChannelType.PRIVATE, null, null, List.of(), List.of(), null);
      given(channelService.create(any(PrivateChannelCreateRequest.class))).willReturn(dto);

      mockMvc.perform(post("/api/channels/private")
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isOk())
          .andExpect(content().json(objectMapper.writeValueAsString(dto)));
    }
  }

  @Nested
  @DisplayName("PATCH /api/channels/{channelId}")
  class Update {

    @Test
    @DisplayName("성공: 채널 수정")
    void success() throws Exception {
      PublicChannelUpdateRequest request = new PublicChannelUpdateRequest("새이름", "새설명");
      ChannelDto dto = new ChannelDto(CHANNEL_ID, ChannelType.PUBLIC, "새이름", "새설명", List.of(), List.of(), null);
      given(channelService.update(eq(CHANNEL_ID), any(PublicChannelUpdateRequest.class))).willReturn(dto);

      mockMvc.perform(patch("/api/channels/{channelId}", CHANNEL_ID)
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isOk())
          .andExpect(content().json(objectMapper.writeValueAsString(dto)));
    }
  }

  @Nested
  @DisplayName("DELETE /api/channels/{channelId}")
  class Delete {

    @Test
    @DisplayName("성공: 채널 삭제 시 204 반환")
    void success() throws Exception {
      mockMvc.perform(delete("/api/channels/{channelId}", CHANNEL_ID))
          .andExpect(status().isNoContent());
    }
  }
}
