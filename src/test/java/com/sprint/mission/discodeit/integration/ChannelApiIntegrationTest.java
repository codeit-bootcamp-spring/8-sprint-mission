package com.sprint.mission.discodeit.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ChannelApiIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Test
  @DisplayName("통합: 채널 생성 성공 -> 201")
  void createChannel_success() throws Exception {
    mockMvc.perform(
            post("/api/channels/public")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"name":"pub1","description":"description"}
                    """)
        )
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.name").value("pub1"))
        .andExpect(jsonPath("$.type").value("PUBLIC"));
  }

  @Test
  @DisplayName("통합: 채널 삭제 성공 -> 2xx")
  void deleteChannel_success() throws Exception {
    UUID channelId = createPublicChannelAndGetId("pub_delete");

    mockMvc.perform(delete("/api/channels/{id}", channelId))
        .andExpect(status().is2xxSuccessful());
  }

  private UUID createPublicChannelAndGetId(String name) throws Exception {
    MvcResult result = mockMvc.perform(
            post("/api/channels/public")
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
