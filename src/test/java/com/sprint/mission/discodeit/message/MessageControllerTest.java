package com.sprint.mission.discodeit.message;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.dto.data.MessageDto;
import com.sprint.mission.discodeit.dto.request.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.request.MessageUpdateRequest;
import com.sprint.mission.discodeit.exception.GlobalExceptionHandler;
import com.sprint.mission.discodeit.service.MessageService;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(com.sprint.mission.discodeit.controller.MessageController.class)
@DisplayName("MessageController 슬라이스 테스트")
class MessageControllerTest {

  @org.springframework.boot.SpringBootConfiguration
  @org.springframework.context.annotation.Import({
      com.sprint.mission.discodeit.controller.MessageController.class,
      GlobalExceptionHandler.class
  })
  static class Config {}

  @Autowired
  private MockMvc mockMvc;
  @Autowired
  private ObjectMapper objectMapper;
  @MockBean
  private MessageService messageService;

  private static final UUID MESSAGE_ID = UUID.randomUUID();
  private static final UUID CHANNEL_ID = UUID.randomUUID();
  private static final UUID AUTHOR_ID = UUID.randomUUID();

  @Nested
  @DisplayName("POST /api/messages")
  class Create {

    @Test
    @DisplayName("성공: multipart로 메시지 생성 시 201과 생성된 메시지 JSON 반환")
    void success() throws Exception {
      MessageCreateRequest request = new MessageCreateRequest("안녕", CHANNEL_ID, AUTHOR_ID);
      MessageDto dto = new MessageDto(MESSAGE_ID, "안녕", CHANNEL_ID, AUTHOR_ID, List.of(), Instant.now());
      given(messageService.create(any(MessageCreateRequest.class), any())).willReturn(dto);

      MockMultipartFile part = new MockMultipartFile("messageCreateRequest", "",
          "application/json", objectMapper.writeValueAsBytes(request));
      mockMvc.perform(multipart("/api/messages").file(part))
          .andExpect(status().isCreated())
          .andExpect(content().contentType(MediaType.APPLICATION_JSON))
          .andExpect(jsonPath("$.content").value("안녕"))
          .andExpect(jsonPath("$.channelId").value(CHANNEL_ID.toString()))
          .andExpect(jsonPath("$.authorId").value(AUTHOR_ID.toString()));
    }

    @Test
    @DisplayName("실패: 메시지 내용 누락 시 400과 검증 오류 JSON 반환")
    void fail_validation() throws Exception {
      MessageCreateRequest invalidRequest = new MessageCreateRequest("", CHANNEL_ID, AUTHOR_ID);
      MockMultipartFile part = new MockMultipartFile("messageCreateRequest", "",
          "application/json", objectMapper.writeValueAsBytes(invalidRequest));

      mockMvc.perform(multipart("/api/messages").file(part))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.code").exists())
          .andExpect(jsonPath("$.message").exists());
    }
  }

  @Nested
  @DisplayName("PATCH /api/messages/{messageId}")
  class Update {

    @Test
    @DisplayName("성공: 메시지 수정 시 200과 수정된 메시지 JSON 반환")
    void success() throws Exception {
      MessageUpdateRequest request = new MessageUpdateRequest("새내용");
      MessageDto dto = new MessageDto(MESSAGE_ID, "새내용", CHANNEL_ID, AUTHOR_ID, List.of(), Instant.now());
      given(messageService.update(eq(MESSAGE_ID), any(MessageUpdateRequest.class))).willReturn(dto);

      mockMvc.perform(patch("/api/messages/{messageId}", MESSAGE_ID)
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.content").value("새내용"))
          .andExpect(jsonPath("$.id").value(MESSAGE_ID.toString()));
    }

    @Test
    @DisplayName("실패: 수정 내용 누락 시 400과 검증 오류 JSON 반환")
    void fail_validation() throws Exception {
      String body = "{\"newContent\":\"\"}";

      mockMvc.perform(patch("/api/messages/{messageId}", MESSAGE_ID)
              .contentType(MediaType.APPLICATION_JSON)
              .content(body))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.code").exists());
    }
  }

  @Nested
  @DisplayName("GET /api/messages")
  class FindAllByChannelId {

    @Test
    @DisplayName("성공: channelId만 전달 시 200과 메시지 목록 JSON 배열 반환")
    void success() throws Exception {
      MessageDto dto = new MessageDto(MESSAGE_ID, "내용", CHANNEL_ID, AUTHOR_ID, List.of(), Instant.now());
      given(messageService.findAllByChannelIdAsList(CHANNEL_ID)).willReturn(List.of(dto));

      mockMvc.perform(get("/api/messages").param("channelId", CHANNEL_ID.toString()))
          .andExpect(status().isOk())
          .andExpect(content().contentType(MediaType.APPLICATION_JSON))
          .andExpect(jsonPath("$").isArray())
          .andExpect(jsonPath("$[0].content").value("내용"));
    }

  }

  @Nested
  @DisplayName("DELETE /api/messages/{messageId}")
  class Delete {

    @Test
    @DisplayName("성공: 메시지 삭제 시 204 No Content")
    void success() throws Exception {
      mockMvc.perform(delete("/api/messages/{messageId}", MESSAGE_ID))
          .andExpect(status().isNoContent());
    }
  }
}
