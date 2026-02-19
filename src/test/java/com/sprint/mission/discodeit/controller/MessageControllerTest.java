package com.sprint.mission.discodeit.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.dto.data.MessageDto;
import com.sprint.mission.discodeit.dto.data.UserDto;
import com.sprint.mission.discodeit.dto.response.PageResponse;
import com.sprint.mission.discodeit.service.MessageService;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(MessageController.class)
@MockitoBean(types = JpaMetamodelMappingContext.class)
class MessageControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private MessageService messageService;

  @Nested
  @DisplayName("메시지 생성 테스트")
  class Create {

    @Test
    @DisplayName("성공: 텍스트 내용과 첨부 파일을 포함하여 메시지를 생성한다")
    void success() throws Exception {
      // given
      UUID channelId = UUID.randomUUID();
      UUID authorId = UUID.randomUUID();
      UserDto author = new UserDto(authorId, "sender", "s@test.com", null, true);
      MessageDto response = new MessageDto(UUID.randomUUID(), Instant.now(), null, "Hello",
          channelId, author, List.of());

      given(messageService.create(any(), any())).willReturn(response);

      MockMultipartFile file = new MockMultipartFile("attachments", "test.png", "image/png",
          "img-content".getBytes());
      String requestJson = String.format(
          "{\"content\":\"Hello\", \"channelId\":\"%s\", \"authorId\":\"%s\"}", channelId,
          authorId);
      MockMultipartFile messageCreateRequest = new MockMultipartFile("messageCreateRequest", "",
          "application/json", requestJson.getBytes());

      // when & then
      mockMvc.perform(multipart("/api/messages")
              .file(file)
              .file(messageCreateRequest))
          .andExpect(status().isCreated())
          .andExpect(jsonPath("$.content").value("Hello"))
          .andExpect(jsonPath("$.channelId").value(channelId.toString()));
    }

    @Test
    @DisplayName("실패: 메시지 내용(content)이 비어있으면 400 에러를 반환한다")
    void fail_emptyContent() throws Exception {
      String requestJson =
          "{\"content\":\"\", \"channelId\":\"" + UUID.randomUUID() + "\", \"authorId\":\""
              + UUID.randomUUID() + "\"}";
      MockMultipartFile request = new MockMultipartFile("messageCreateRequest", "",
          "application/json", requestJson.getBytes());

      mockMvc.perform(multipart("/api/messages").file(request))
          .andExpect(status().isBadRequest());
    }
  }

  @Nested
  @DisplayName("메시지 수정 테스트")
  class Update {

    @Test
    @DisplayName("성공: 메시지 내용을 newContent 필드로 수정한다")
    void success() throws Exception {
      UUID messageId = UUID.randomUUID();
      MessageDto response = new MessageDto(messageId, Instant.now(), Instant.now(), "Modified",
          UUID.randomUUID(), null, List.of());
      given(messageService.update(any(), any())).willReturn(response);

      // 필드명 반영: newContent
      String requestBody = "{\"newContent\":\"Modified\"}";

      mockMvc.perform(patch("/api/messages/{messageId}", messageId)
              .contentType(MediaType.APPLICATION_JSON)
              .content(requestBody))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.content").value("Modified"));
    }

    @Test
    @DisplayName("실패: 수정 내용이 공백이면 400 에러를 반환한다")
    void fail_blankContent() throws Exception {
      String requestBody = "{\"newContent\":\" \"}";

      mockMvc.perform(patch("/api/messages/{messageId}", UUID.randomUUID())
              .contentType(MediaType.APPLICATION_JSON)
              .content(requestBody))
          .andExpect(status().isBadRequest());
    }
  }

  @Nested
  @DisplayName("메시지 목록 조회 테스트")
  class FindAll {

    @Test
    @DisplayName("성공: 채널의 메시지 목록을 페이징 조회한다")
    void success() throws Exception {
      UUID channelId = UUID.randomUUID();
      MessageDto msg = new MessageDto(UUID.randomUUID(), Instant.now(), null, "Hi", channelId, null,
          List.of());

      PageResponse<MessageDto> response = new PageResponse<>(
          List.of(msg), //content
          null,         //nextCursor
          10,           //size
          false,        //hasNext
          1L            //totalElements
      );

      given(messageService.findAllByChannelId(any(), any(), any())).willReturn(response);

      mockMvc.perform(get("/api/messages")
              .param("channelId", channelId.toString()))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.content[0].content").value("Hi"));
    }
  }

  @Nested
  @DisplayName("메시지 삭제 테스트")
  class Delete {

    @Test
    @DisplayName("성공: 메시지 삭제 시 204 No Content를 반환한다")
    void success() throws Exception {
      mockMvc.perform(delete("/api/messages/{messageId}", UUID.randomUUID()))
          .andExpect(status().isNoContent());
    }
  }
}