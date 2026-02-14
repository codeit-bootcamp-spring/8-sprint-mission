package com.sprint.mission.discodeit.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.dto.message.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.message.MessageDto;
import com.sprint.mission.discodeit.service.MessageService;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(MessageController.class)
@ActiveProfiles("test")
class MessageControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private MessageService messageService;

  @MockitoBean
  private JpaMetamodelMappingContext jpaMappingContext;

  @Test
  @DisplayName("POST /api/messages: 성공 - 201 + JSON 응답")
  void create_success() throws Exception {
    UUID msgId = UUID.randomUUID();
    UUID channelId = UUID.randomUUID();

    MessageDto response = new MessageDto(
        msgId,                // id
        Instant.now(),       // createdAt
        Instant.now(),       // updatedAt
        "hello",             // content
        channelId,           // channelId
        null,                // author
        List.of()            // attachments
    );

    when(messageService.createMessage(any())).thenReturn(response);

    MessageCreateRequest req = new MessageCreateRequest(
        channelId,
        UUID.randomUUID(),
        "hello",
        List.of()
    );

    MockMultipartFile requestPart = new MockMultipartFile(
        "messageCreateRequest",
        "",
        "application/json",
        objectMapper.writeValueAsBytes(req)
    );

    mockMvc.perform(
            multipart("/api/messages")
                .file(requestPart)
                .contentType(MediaType.MULTIPART_FORM_DATA)
        )
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(msgId.toString()))
        .andExpect(jsonPath("$.content").value("hello"))
        .andExpect(jsonPath("$.channelId").value(channelId.toString()));
  }

  @Test
  @DisplayName("POST /api/messages: 실패 - validation(content blank) -> 400")
  void create_fail_validation() throws Exception {
    UUID channelId = UUID.randomUUID();

    MessageCreateRequest req = new MessageCreateRequest(
        channelId,
        UUID.randomUUID(),
        "",
        List.of()
    );

    MockMultipartFile requestPart = new MockMultipartFile(
        "messageCreateRequest",
        "",
        "application/json",
        objectMapper.writeValueAsBytes(req)
    );

    mockMvc.perform(
            multipart("/api/messages")
                .file(requestPart)
                .contentType(MediaType.MULTIPART_FORM_DATA)
        )
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
  }
}