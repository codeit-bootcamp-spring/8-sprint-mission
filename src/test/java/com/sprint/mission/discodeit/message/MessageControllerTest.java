package com.sprint.mission.discodeit.message;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.controller.MessageController;
import com.sprint.mission.discodeit.dto.data.MessageDto;
import com.sprint.mission.discodeit.dto.request.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.request.MessageUpdateRequest;
import com.sprint.mission.discodeit.service.MessageService;
import java.time.Instant;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
@DisplayName("MessageController 단위 테스트")
class MessageControllerTest {

  private MockMvc mockMvc;
  private final ObjectMapper objectMapper = new ObjectMapper();

  @Mock
  private MessageService messageService;
  @InjectMocks
  private MessageController messageController;

  private static final UUID MESSAGE_ID = UUID.randomUUID();
  private static final UUID CHANNEL_ID = UUID.randomUUID();
  private static final UUID AUTHOR_ID = UUID.randomUUID();

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.standaloneSetup(messageController)
        .setCustomArgumentResolvers(new org.springframework.data.web.PageableHandlerMethodArgumentResolver())
        .build();
  }

  @Nested
  @DisplayName("POST /api/messages")
  class Create {

    @Test
    @DisplayName("성공: 메시지 생성 시 201 반환")
    void success() throws Exception {
      MessageCreateRequest request = new MessageCreateRequest("안녕", CHANNEL_ID, AUTHOR_ID);
      MessageDto dto = new MessageDto(MESSAGE_ID, "안녕", CHANNEL_ID, AUTHOR_ID, List.of(), Instant.now());
      given(messageService.create(any(MessageCreateRequest.class), any())).willReturn(dto);

      MockMultipartFile part = new MockMultipartFile("messageCreateRequest", "",
          "application/json", objectMapper.writeValueAsBytes(request));
      mockMvc.perform(multipart("/api/messages").file(part))
          .andExpect(status().isCreated());
    }
  }

  @Nested
  @DisplayName("PATCH /api/messages/{messageId}")
  class Update {

    @Test
    @DisplayName("성공: 메시지 수정 시 200 반환")
    void success() throws Exception {
      MessageUpdateRequest request = new MessageUpdateRequest("새내용");
      MessageDto dto = new MessageDto(MESSAGE_ID, "새내용", CHANNEL_ID, AUTHOR_ID, List.of(), Instant.now());
      given(messageService.update(eq(MESSAGE_ID), any(MessageUpdateRequest.class))).willReturn(dto);

      mockMvc.perform(patch("/api/messages/{messageId}", MESSAGE_ID)
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isOk());
    }
  }

  @Nested
  @DisplayName("DELETE /api/messages/{messageId}")
  class Delete {

    @Test
    @DisplayName("성공: 메시지 삭제 시 204 반환")
    void success() throws Exception {
      mockMvc.perform(delete("/api/messages/{messageId}", MESSAGE_ID))
          .andExpect(status().isNoContent());
    }
  }

  @Nested
  @DisplayName("GET /api/messages")
  class FindAllByChannelId {

    @Test
    @DisplayName("성공: channelId로 목록 조회 시 200 반환")
    void success() throws Exception {
      given(messageService.findAllByChannelIdAsList(CHANNEL_ID)).willReturn(List.of());

      mockMvc.perform(get("/api/messages").param("channelId", CHANNEL_ID.toString()))
          .andExpect(status().isOk());
    }
  }
}
