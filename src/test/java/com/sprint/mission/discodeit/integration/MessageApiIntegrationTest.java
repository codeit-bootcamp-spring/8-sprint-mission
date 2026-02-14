package com.sprint.mission.discodeit.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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
        )
        .andExpect(status().isCreated())
        .andReturn();

    JsonNode body = objectMapper.readTree(result.getResponse().getContentAsByteArray());
    return UUID.fromString(body.get("id").asText());
  }

  private UUID createPublicChannelAndGetId(String name) throws Exception {
    MvcResult result = mockMvc.perform(
            org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                .post("/api/channels/public")
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
