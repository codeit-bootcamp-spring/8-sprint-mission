package com.sprint.mission.discodeit.integrationTest;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.DTO.dto.ChannelDto;
import com.sprint.mission.discodeit.DTO.dto.MessageDto;
import com.sprint.mission.discodeit.DTO.dto.UserDto;
import com.sprint.mission.discodeit.DTO.request.MessageCreateRequest;
import com.sprint.mission.discodeit.DTO.request.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.DTO.request.UserCreateRequest;
import com.sprint.mission.discodeit.service.ChannelService;
import com.sprint.mission.discodeit.service.MessageService;
import com.sprint.mission.discodeit.service.UserService;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class MessageApiIntegrationTest {

  @Autowired
  private MockMvc mockMvc;
  @Autowired
  private ObjectMapper objectMapper;
  @Autowired
  private MessageService messageService;
  @Autowired
  private ChannelService channelService;
  @Autowired
  private UserService userService;

  @Test
  @WithMockUser
  @DisplayName("POST /api/messages - 메시지 생성 통합 테스트 성공")
  void createMessage_integration_success() throws Exception {
    // given
    UserDto user = userService.create(
        new UserCreateRequest("test", "test@codeit.com", "password123"), Optional.empty());
    ChannelDto channel = channelService.create(
        new PublicChannelCreateRequest("public channel", "public channel description"));

    MessageCreateRequest request = new MessageCreateRequest(user.id(), channel.id(),
        "test message");

    MockMultipartFile requestJson = new MockMultipartFile("messageCreateRequest", "",
        MediaType.APPLICATION_JSON_VALUE, objectMapper.writeValueAsBytes(request));

    // when & then
    mockMvc.perform(multipart("/api/messages").file(requestJson).with(csrf()))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.content").value("test message"));
  }

  @Test
  @WithMockUser
  @DisplayName("POST /api/messages - 메시지 생성 통합 테스트 실패 (유효성 검사 실패")
  void createMessage_integration_fail_validation() throws Exception {
    // given
    MessageCreateRequest request = new MessageCreateRequest(
        UUID.randomUUID(),
        null,
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
        .andExpect(jsonPath("$.details.channelId").exists());
  }

  @Test
  @WithMockUser
  @DisplayName("DELETE /api/messages/{messageId} - 메시지 삭제 통합 테스트 성공")
  void deleteMessage_integration_success() throws Exception {
    // given
    PublicChannelCreateRequest channelCreateRequest = new PublicChannelCreateRequest(
        "public channel",
        "public channel description"
    );

    ChannelDto channel = channelService.create(channelCreateRequest);

    UserCreateRequest userCreateRequest = new UserCreateRequest(
        "test",
        "test@codeit.com",
        "password123"
    );

    UserDto user = userService.create(userCreateRequest, Optional.empty());

    MessageCreateRequest messageCreateRequest = new MessageCreateRequest(
        user.id(),
        channel.id(),
        "test message"
    );

    MessageDto message = messageService.create(messageCreateRequest, Collections.emptyList());
    UUID messageId = message.id();

    mockMvc.perform(delete("/api/messages/{messageId}", messageId)
            .param("channelId", channel.id().toString())
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isNoContent());

  }

  @Test
  @WithMockUser
  @DisplayName("DELETE /api/message/{messageId} - 메시지 삭제 통합 테스트 실패 (존재하지 않는 메시지")
  void deleteMessage_integration_fail_messageNotFound() throws Exception {
    UUID messageId = UUID.randomUUID();

    mockMvc.perform(delete("/api/messages/{messageId}", messageId))
        .andExpect(status().isNotFound());
  }
}
