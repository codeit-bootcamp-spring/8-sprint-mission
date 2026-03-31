package com.sprint.mission.discodeit.integration;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class MessageApiIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Test
  @DisplayName("통합: 메시지 생성 성공 -> 201")
  void createMessage_success() throws Exception {
    UUID userId = createUserAndGetId("m1", "m1@test.com");
    UUID channelId = createPublicChannelAndGetId("msg_channel");

    byte[] reqJson = ("""
        {"channelId":"%s","authorId":"%s","content":"hello","attachmentIds":[]}
        """.formatted(channelId, userId)).getBytes(StandardCharsets.UTF_8);

    MockMultipartFile requestPart = new MockMultipartFile(
        "messageCreateRequest", "", "application/json", reqJson
    );

    mockMvc.perform(
            multipart("/api/messages")
                .file(requestPart)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                // 메시지를 작성하는 사용자 정보를 주입
                .with(user("m1").roles("USER"))
                .with(csrf())
        )
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.content").value("hello"))
        .andExpect(jsonPath("$.channelId").value(channelId.toString()));
  }

  @Test
  @DisplayName("통합: 메시지 목록 조회 성공 -> 200")
  void findAllMessages_success() throws Exception {
    UUID channelId = createPublicChannelAndGetId("list_channel");

    mockMvc.perform(
            get("/api/messages")
                .param("channelId", channelId.toString())
                // 조회 시에도 인증된 사용자여야 합니다.
                .with(user("m1").roles("USER"))
        )
        .andExpect(status().isOk());
  }

  private UUID createUserAndGetId(String username, String email) throws Exception {
    MockMultipartFile requestPart = new MockMultipartFile(
        "userCreateRequest", "", "application/json",
        ("""
            {"username":"%s","email":"%s","password":"pw"}
            """.formatted(username, email)).getBytes(StandardCharsets.UTF_8)
    );

    MvcResult result = mockMvc.perform(
            multipart("/api/users")
                .file(requestPart)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .with(csrf()) // 회원가입도 CSRF 필요
        )
        .andExpect(status().isCreated())
        .andReturn();

    JsonNode body = objectMapper.readTree(result.getResponse().getContentAsByteArray());
    return UUID.fromString(body.get("id").asText());
  }

  private UUID createPublicChannelAndGetId(String name) throws Exception {
    MvcResult result = mockMvc.perform(
            post("/api/channels/public")
                // 퍼블릭 채널 생성은 CHANNEL_MANAGER 이상의 권한이 있어야 함
                .with(user("admin").roles("CHANNEL_MANAGER"))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"name":"%s","description":"d"}
                    """.formatted(name))
        )
        .andExpect(status().isCreated())
        .andReturn();

    JsonNode body = objectMapper.readTree(result.getResponse().getContentAsByteArray());
    return UUID.fromString(body.get("id").asText());
  }
}
