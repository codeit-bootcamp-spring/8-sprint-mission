package com.sprint.mission.discodeit.slice.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.controller.MessageController;
import com.sprint.mission.discodeit.dto.MessageDto;
import com.sprint.mission.discodeit.dto.UserDto;
import com.sprint.mission.discodeit.dto.request.MessageCreateRequest;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.Role;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.message.MessageNotFoundException;
import com.sprint.mission.discodeit.service.MessageService;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(MessageController.class)
@DisplayName("MessageController 테스트")
public class MessageControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private MessageService messageService;

  UUID channelId;
  UUID authorId;
  UUID messageId;
  Channel testChannel;
  User testAuthor;
  UserDto testAuthorDto;
  Message testMessage;
  MessageDto testMessageDto;

  @BeforeEach
  void setUp() {
    channelId = UUID.randomUUID();
    authorId = UUID.randomUUID();
    messageId = UUID.randomUUID();

    testChannel = new Channel(ChannelType.PUBLIC, "test channel", "test description");
    testAuthor = new User("testUser", "test@naver.com", "test1234", null);
    testAuthorDto = new UserDto(authorId, "testUser", "test@naver.com", null, true, Role.USER);
    testMessage = new Message("안녕", testChannel, testAuthor, List.of());
    testMessageDto = new MessageDto(messageId, Instant.now(), Instant.now(), "안녕", channelId,
        testAuthorDto, List.of());
  }

  @Test
  @DisplayName("메시지 생성 요청이 성공적으로 처리되어야 한다.")
  void createMessage_ValidRequest_Success() throws Exception {

    // given
    MessageCreateRequest messageReq = new MessageCreateRequest("안녕", channelId, authorId);
    String messageJson = objectMapper.writeValueAsString(messageReq);

    MockMultipartFile messagePart = new MockMultipartFile(
        "messageCreateRequest",
        "",
        "application/json",
        messageJson.getBytes(StandardCharsets.UTF_8));

    given(messageService.create(any(), any())).willReturn(testMessageDto);

    // when & then
    mockMvc.perform(multipart("/api/messages")
            .file(messagePart))
        .andDo(print())
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.content").value(testMessage.getContent()))
        .andExpect(jsonPath("$.id").exists());

    then(messageService).should().create(eq(messageReq), any());
  }

  @Test
  @DisplayName("채널 Id가 누락된 경우 메시지 생성 요청 시 400 에러가 발생해야 한다.")
  void createMessage_InvalidChannelId_BadRequest() throws Exception {

    // given
    MessageCreateRequest invalidMessageReq = new MessageCreateRequest("안녕", null, authorId);
    String messageJson = objectMapper.writeValueAsString(invalidMessageReq);

    MockMultipartFile messagePart = new MockMultipartFile(
        "messageCreateRequest",
        "",
        "application/json",
        messageJson.getBytes(StandardCharsets.UTF_8));

    // when & then
    mockMvc.perform(multipart("/api/messages")
            .file(messagePart))
        .andExpect(status().isBadRequest());

    then(messageService).shouldHaveNoInteractions();
  }

  @Test
  @DisplayName("메시지 삭제 요청이 성공적으로 처리되어야 한다.")
  void deleteMessage_ValidRequest_Success() throws Exception {

    // when & then
    mockMvc.perform(delete("/api/messages/{messageId}", messageId))
        .andDo(print())
        .andExpect(status().isOk());

    then(messageService).should().delete(eq(messageId));
  }

  @Test
  @DisplayName("존재하지 않는 메시지 Id로 메시지 삭제 요청 시 404 에러가 발생해야 한다.")
  void deleteMessage_MessageNotFound_NotFound() throws Exception {

    // given
    UUID invalidMessageId = UUID.randomUUID();
    willThrow(new MessageNotFoundException(invalidMessageId))
        .given(messageService).delete(eq(invalidMessageId));

    // when & then
    mockMvc.perform(delete("/api/messages/{messageId}", invalidMessageId))
        .andExpect(status().isNotFound());

    then(messageService).should().delete(eq(invalidMessageId));
  }
}
