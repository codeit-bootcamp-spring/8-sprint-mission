package com.sprint.mission.discodeit.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.dto.request.MessageCreateRequest;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("Message 통합 테스트")
class MessageIntegrationTest {

  @Autowired
  private MockMvc mockMvc;
  @Autowired
  private ObjectMapper objectMapper;
  @Autowired
  private MessageRepository messageRepository;
  @Autowired
  private UserRepository userRepository;
  @Autowired
  private ChannelRepository channelRepository;
  @Autowired
  private UserStatusRepository userStatusRepository;

  @Test
  @DisplayName("성공: 메시지를 전송하고 DB 저장을 확인한다")
  void sendMessage_Success() throws Exception {
    //Given
    User author = userRepository.save(new User("messenger", "msg@test.com", "password", null));
    userStatusRepository.save(new UserStatus(author, Instant.now()));

    //Given
    Channel channel = channelRepository.save(new Channel(ChannelType.PUBLIC, "테스트채널", "설명"));

    MessageCreateRequest request = new MessageCreateRequest("안녕하세요!", channel.getId(),
        author.getId());

    MockMultipartFile requestPart = new MockMultipartFile(
        "messageCreateRequest",
        "",
        MediaType.APPLICATION_JSON_VALUE,
        objectMapper.writeValueAsString(request).getBytes(StandardCharsets.UTF_8)
    );

    //When & Then
    mockMvc.perform(multipart("/api/messages")
            .file(requestPart))
        .andExpect(status().isCreated());

    assertThat(messageRepository.findAll()).anyMatch(m -> m.getContent().equals("안녕하세요!"));
  }

  @Test
  @DisplayName("실패: 존재하지 않는 채널에 메시지 전송 시 400 에러를 반환한다")
  void sendMessage_Fail_ChannelNotFound() throws Exception {
    //Given
    User author = userRepository.save(new User("tester", "test2@test.com", "password", null));
    userStatusRepository.save(new UserStatus(author, Instant.now()));

    UUID fakeChannelId = UUID.randomUUID();

    MessageCreateRequest request = new MessageCreateRequest("실패할 메시지", fakeChannelId,
        author.getId());
    MockMultipartFile requestPart = new MockMultipartFile(
        "messageCreateRequest", "", MediaType.APPLICATION_JSON_VALUE,
        objectMapper.writeValueAsString(request).getBytes(StandardCharsets.UTF_8));

    //When & Then
    mockMvc.perform(multipart("/api/messages")
            .file(requestPart))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("CHANNEL_NOT_FOUND"));
  }

  @Test
  @DisplayName("성공: 채널별 메시지 목록을 조회한다")
  void findMessages_Success() throws Exception {
    //Given
    User author = userRepository.save(new User("tester", "t@t.com", "pw", null));
    userStatusRepository.save(new UserStatus(author, Instant.now()));
    Channel channel = channelRepository.save(new Channel(ChannelType.PUBLIC, "채널", "설명"));

    messageRepository.save(new Message("내용1", channel, author, new ArrayList<>()));
    messageRepository.save(new Message("내용2", channel, author, new ArrayList<>()));

    //When & Then
    mockMvc.perform(get("/api/messages").param("channelId", channel.getId().toString()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content.length()").value(2))
        .andExpect(jsonPath("$.content[0].content").value("내용2")) // 최신 메시지가 0번
        .andExpect(jsonPath("$.content[1].content").value("내용1")); // 예전 메시지가 1번
  }

  @Test
  @DisplayName("성공: 메시지를 삭제한다")
  void deleteMessage_Success() throws Exception {
    //Given
    User author = userRepository.save(new User("tester", "t2@t.com", "pw", null));
    userStatusRepository.save(new UserStatus(author, Instant.now()));
    Channel channel = channelRepository.save(new Channel(ChannelType.PUBLIC, "채널", "설명"));
    com.sprint.mission.discodeit.entity.Message message =
        messageRepository.save(
            new Message("삭제할거야", channel, author, new ArrayList<>()));

    //When & Then
    mockMvc.perform(delete("/api/messages/{id}", message.getId()))
        .andExpect(status().isNoContent());

    assertThat(messageRepository.findById(message.getId())).isEmpty();
  }
}