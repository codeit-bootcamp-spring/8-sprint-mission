package com.sprint.mission.discodeit.controllerTest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.sprint.mission.discodeit.DTO.dto.MessageDto;
import com.sprint.mission.discodeit.DTO.request.MessageCreateRequest;
import com.sprint.mission.discodeit.exception.MessageException;
import java.time.Instant;
import java.util.Collections;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;

public class MessageControllerTest extends ControllerTestSupport {

  @Test
  @WithMockUser
  @DisplayName("POST /api/messages - 메시지 생성 성공")
  void createMessage_success() throws Exception {
    //given
    MessageCreateRequest request = new MessageCreateRequest(
        UUID.randomUUID(),
        UUID.randomUUID(),
        "test message"
    );
    MessageDto messageDto = new MessageDto(
        UUID.randomUUID(),
        Instant.now(),
        Instant.now(),
        "test message",
        request.channelId(),
        null,
        Collections.emptyList()
    );

    given(messageService.create(any(MessageCreateRequest.class), any())).willReturn(messageDto);

    MockMultipartFile requestJson = new MockMultipartFile(
        "messageCreateRequest",
        "",
        MediaType.APPLICATION_JSON_VALUE,
        objectMapper.writeValueAsBytes(request)
    );

    // when & then
    mockMvc.perform(multipart("/api/messages")
            .file(requestJson)
            .with(csrf()))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.content").value("test message"));
  }

  @Test
  @WithMockUser
  @DisplayName("POST /api/messages - 메시지 생성 실패 (유효성 검사)")
  void createMessage_fail_validation() throws Exception {
    //given
    MessageCreateRequest request = new MessageCreateRequest(
        null,
        UUID.randomUUID(),
        "test message"
    );

    MockMultipartFile requestJson = new MockMultipartFile(
        "messageCreateRequest",
        "",
        MediaType.APPLICATION_JSON_VALUE,
        objectMapper.writeValueAsBytes(request)
    );

    // when & then
    mockMvc.perform(multipart("/api/messages")
            .file(requestJson)
            .with(csrf()))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.details.authorId").exists());
  }

  @Test
  @WithMockUser
  @DisplayName("DELETE /api/messages/{messageId} - 메시지 삭제 성공")
  void deleteMessage_success() throws Exception {
    //given
    UUID messageId = UUID.randomUUID();

    // when & then
    mockMvc.perform(delete("/api/messages/{messageId}", messageId)
            .with(csrf()))
        .andExpect(status().isNoContent());
  }

  @Test
  @WithMockUser
  @DisplayName("DELETE /api/messages/{messageId} - 메시지 삭제 실패 (존재하지 않는 메시지)")
  void deleteMessage_fail_not_found() throws Exception {
    //given
    UUID messageId = UUID.randomUUID();
    willThrow(new MessageException.MessageNotFoundException(messageId))
        .given(messageService).delete(eq(messageId));

    // when & then
    mockMvc.perform(delete("/api/messages/{messageId}", messageId)
            .with(csrf()))
        .andExpect(status().isNotFound());
  }
}
