package com.sprint.mission.discodeit.integration;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.dto.request.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.request.MessageUpdateRequest;
import com.sprint.mission.discodeit.dto.request.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.request.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.dto.request.PublicChannelUpdateRequest;
import com.sprint.mission.discodeit.dto.request.UserCreateRequest;
import com.sprint.mission.discodeit.dto.request.UserUpdateRequest;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

/**
 * Spring Boot 전체 컨텍스트 + H2 인메모리 DB를 사용하는 API 통합 테스트.
 * 각 테스트는 @Transactional로 독립 실행 후 롤백됩니다.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("API 통합 테스트")
class ApiIntegrationTest {

  @Autowired
  private MockMvc mockMvc;
  @Autowired
  private ObjectMapper objectMapper;

  // ---------- User API ----------

  @Nested
  @DisplayName("사용자 API")
  class UserApi {

    @Test
    @DisplayName("생성 성공: JSON으로 사용자 생성 시 201과 생성된 사용자 반환")
    void create_success() throws Exception {
      UserCreateRequest request = new UserCreateRequest("integrationUser", "integ@test.com", "pass123");

      mockMvc.perform(post("/api/users")
              .contentType(APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isCreated())
          .andExpect(content().contentType(APPLICATION_JSON))
          .andExpect(jsonPath("$.username").value("integrationUser"))
          .andExpect(jsonPath("$.email").value("integ@test.com"))
          .andExpect(jsonPath("$.id").exists());
    }

    @Test
    @DisplayName("생성 실패: 이메일 형식 오류 시 400")
    void create_fail_validation() throws Exception {
      UserCreateRequest request = new UserCreateRequest("user", "invalid-email", "pass");

      mockMvc.perform(post("/api/users")
              .contentType(APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("수정 성공: PATCH로 사용자 정보 수정 시 200")
    void update_success() throws Exception {
      UserCreateRequest create = new UserCreateRequest("toUpdate", "update@test.com", "pass");
      ResultActions createRes = mockMvc.perform(post("/api/users")
          .contentType(APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(create)));
      String body = createRes.andReturn().getResponse().getContentAsString();
      UUID userId = UUID.fromString(objectMapper.readTree(body).get("id").asText());

      UserUpdateRequest update = new UserUpdateRequest("updatedName", "updated@test.com", null);
      mockMvc.perform(patch("/api/users/" + userId)
              .contentType(APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(update)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.username").value("updatedName"))
          .andExpect(jsonPath("$.email").value("updated@test.com"));
    }

    @Test
    @DisplayName("수정 실패: 존재하지 않는 사용자 ID로 수정 시 404")
    void update_fail_notFound() throws Exception {
      UUID unknownId = UUID.randomUUID();
      UserUpdateRequest update = new UserUpdateRequest("x", "x@x.com", null);

      mockMvc.perform(patch("/api/users/" + unknownId)
              .contentType(APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(update)))
          .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("삭제 성공: 사용자 삭제 시 204")
    void delete_success() throws Exception {
      UserCreateRequest create = new UserCreateRequest("toDelete", "del@test.com", "pass");
      ResultActions createRes = mockMvc.perform(post("/api/users")
          .contentType(APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(create)));
      UUID userId = UUID.fromString(objectMapper.readTree(createRes.andReturn().getResponse().getContentAsString()).get("id").asText());

      mockMvc.perform(delete("/api/users/" + userId))
          .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("삭제 실패: 존재하지 않는 사용자 ID로 삭제 시 404")
    void delete_fail_notFound() throws Exception {
      mockMvc.perform(delete("/api/users/" + UUID.randomUUID()))
          .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("목록 조회 성공: 사용자 없을 때 빈 배열")
    void list_empty() throws Exception {
      mockMvc.perform(get("/api/users"))
          .andExpect(status().isOk())
          .andExpect(content().contentType(APPLICATION_JSON))
          .andExpect(jsonPath("$").isArray())
          .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("목록 조회 성공: 사용자 생성 후 목록에 포함")
    void list_withData() throws Exception {
      UserCreateRequest create = new UserCreateRequest("listUser", "list@test.com", "pass");
      mockMvc.perform(post("/api/users")
          .contentType(APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(create)))
          .andExpect(status().isCreated());

      mockMvc.perform(get("/api/users"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$").isArray())
          .andExpect(jsonPath("$[?(@.username=='listUser')]").exists());
    }
  }

  // ---------- Channel API ----------

  @Nested
  @DisplayName("채널 API")
  class ChannelApi {

    @Test
    @DisplayName("생성 성공: 공개 채널 생성 시 200과 채널 정보 반환")
    void createPublic_success() throws Exception {
      PublicChannelCreateRequest request = new PublicChannelCreateRequest("Public Channel", "설명");

      mockMvc.perform(post("/api/channels/public")
              .contentType(APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isOk())
          .andExpect(content().contentType(APPLICATION_JSON))
          .andExpect(jsonPath("$.name").value("Public Channel"))
          .andExpect(jsonPath("$.id").exists());
    }

    @Test
    @DisplayName("생성 성공: 비공개 채널 생성 시 200")
    void createPrivate_success() throws Exception {
      UserCreateRequest userReq = new UserCreateRequest("channelUser", "ch@test.com", "pass");
      ResultActions userRes = mockMvc.perform(post("/api/users")
          .contentType(APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(userReq)));
      UUID userId = UUID.fromString(objectMapper.readTree(userRes.andReturn().getResponse().getContentAsString()).get("id").asText());

      PrivateChannelCreateRequest channelReq = new PrivateChannelCreateRequest(List.of(userId));
      mockMvc.perform(post("/api/channels/private")
              .contentType(APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(channelReq)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.id").exists());
    }

    @Test
    @DisplayName("수정 성공: 공개 채널 이름/설명 수정 시 200")
    void update_success() throws Exception {
      PublicChannelCreateRequest create = new PublicChannelCreateRequest("Original", "desc");
      ResultActions createRes = mockMvc.perform(post("/api/channels/public")
          .contentType(APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(create)));
      UUID channelId = UUID.fromString(objectMapper.readTree(createRes.andReturn().getResponse().getContentAsString()).get("id").asText());

      PublicChannelUpdateRequest update = new PublicChannelUpdateRequest("Updated Name", "Updated desc");
      mockMvc.perform(patch("/api/channels/" + channelId)
              .contentType(APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(update)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.name").value("Updated Name"))
          .andExpect(jsonPath("$.description").value("Updated desc"));
    }

    @Test
    @DisplayName("수정 실패: 존재하지 않는 채널 ID 시 404")
    void update_fail_notFound() throws Exception {
      PublicChannelUpdateRequest update = new PublicChannelUpdateRequest("x", "x");
      mockMvc.perform(patch("/api/channels/" + UUID.randomUUID())
              .contentType(APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(update)))
          .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("삭제 성공: 채널 삭제 시 204")
    void delete_success() throws Exception {
      PublicChannelCreateRequest create = new PublicChannelCreateRequest("ToDelete", null);
      ResultActions createRes = mockMvc.perform(post("/api/channels/public")
          .contentType(APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(create)));
      UUID channelId = UUID.fromString(objectMapper.readTree(createRes.andReturn().getResponse().getContentAsString()).get("id").asText());

      mockMvc.perform(delete("/api/channels/" + channelId))
          .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("삭제 실패: 존재하지 않는 채널 ID 시 404")
    void delete_fail_notFound() throws Exception {
      mockMvc.perform(delete("/api/channels/" + UUID.randomUUID()))
          .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("목록 조회: userId로 조회 시 참여 채널만 반환 (빈 목록)")
    void list_empty() throws Exception {
      UserCreateRequest userReq = new UserCreateRequest("noChannelUser", "noch@test.com", "pass");
      ResultActions userRes = mockMvc.perform(post("/api/users")
          .contentType(APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(userReq)));
      UUID userId = UUID.fromString(objectMapper.readTree(userRes.andReturn().getResponse().getContentAsString()).get("id").asText());

      mockMvc.perform(get("/api/channels").param("userId", userId.toString()))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$").isArray())
          .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("목록 조회: 비공개 채널 참여자로 조회 시 해당 채널 포함")
    void list_withData() throws Exception {
      UserCreateRequest userReq = new UserCreateRequest("subUser", "sub@test.com", "pass");
      ResultActions userRes = mockMvc.perform(post("/api/users")
          .contentType(APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(userReq)));
      UUID userId = UUID.fromString(objectMapper.readTree(userRes.andReturn().getResponse().getContentAsString()).get("id").asText());

      PrivateChannelCreateRequest channelReq = new PrivateChannelCreateRequest(List.of(userId));
      mockMvc.perform(post("/api/channels/private")
          .contentType(APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(channelReq)))
          .andExpect(status().isOk());

      mockMvc.perform(get("/api/channels").param("userId", userId.toString()))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$").isArray())
          .andExpect(jsonPath("$.length()").value(1));
    }
  }

  // ---------- Message API ----------

  @Nested
  @DisplayName("메시지 API")
  class MessageApi {

    @Test
    @DisplayName("생성 성공: multipart로 메시지 생성 시 201")
    void create_success() throws Exception {
      UserCreateRequest userReq = new UserCreateRequest("msgUser", "msg@test.com", "pass");
      ResultActions userRes = mockMvc.perform(post("/api/users")
          .contentType(APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(userReq)));
      UUID authorId = UUID.fromString(objectMapper.readTree(userRes.andReturn().getResponse().getContentAsString()).get("id").asText());

      PrivateChannelCreateRequest channelReq = new PrivateChannelCreateRequest(List.of(authorId));
      ResultActions channelRes = mockMvc.perform(post("/api/channels/private")
          .contentType(APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(channelReq)));
      UUID channelId = UUID.fromString(objectMapper.readTree(channelRes.andReturn().getResponse().getContentAsString()).get("id").asText());

      MessageCreateRequest msgReq = new MessageCreateRequest("Hello integration", channelId, authorId);
      MockMultipartFile part = new MockMultipartFile("messageCreateRequest", "",
          "application/json", objectMapper.writeValueAsBytes(msgReq));

      mockMvc.perform(multipart("/api/messages").file(part))
          .andExpect(status().isCreated())
          .andExpect(jsonPath("$.content").value("Hello integration"))
          .andExpect(jsonPath("$.channelId").value(channelId.toString()))
          .andExpect(jsonPath("$.authorId").value(authorId.toString()))
          .andExpect(jsonPath("$.id").exists());
    }

    @Test
    @DisplayName("생성 실패: content 누락 시 400")
    void create_fail_validation() throws Exception {
      MessageCreateRequest msgReq = new MessageCreateRequest("", UUID.randomUUID(), UUID.randomUUID());
      MockMultipartFile part = new MockMultipartFile("messageCreateRequest", "",
          "application/json", objectMapper.writeValueAsBytes(msgReq));

      mockMvc.perform(multipart("/api/messages").file(part))
          .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("수정 성공: 메시지 내용 수정 시 200")
    void update_success() throws Exception {
      UserCreateRequest userReq = new UserCreateRequest("editUser", "edit@test.com", "pass");
      ResultActions userRes = mockMvc.perform(post("/api/users")
          .contentType(APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(userReq)));
      UUID authorId = UUID.fromString(objectMapper.readTree(userRes.andReturn().getResponse().getContentAsString()).get("id").asText());

      PrivateChannelCreateRequest channelReq = new PrivateChannelCreateRequest(List.of(authorId));
      ResultActions channelRes = mockMvc.perform(post("/api/channels/private")
          .contentType(APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(channelReq)));
      UUID channelId = UUID.fromString(objectMapper.readTree(channelRes.andReturn().getResponse().getContentAsString()).get("id").asText());

      MessageCreateRequest msgReq = new MessageCreateRequest("Original", channelId, authorId);
      MockMultipartFile part = new MockMultipartFile("messageCreateRequest", "",
          "application/json", objectMapper.writeValueAsBytes(msgReq));
      ResultActions createRes = mockMvc.perform(multipart("/api/messages").file(part));
      UUID messageId = UUID.fromString(objectMapper.readTree(createRes.andReturn().getResponse().getContentAsString()).get("id").asText());

      MessageUpdateRequest update = new MessageUpdateRequest("Updated content");
      mockMvc.perform(patch("/api/messages/" + messageId)
              .contentType(APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(update)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.content").value("Updated content"));
    }

    @Test
    @DisplayName("수정 실패: 존재하지 않는 메시지 ID 시 404")
    void update_fail_notFound() throws Exception {
      MessageUpdateRequest update = new MessageUpdateRequest("x");
      mockMvc.perform(patch("/api/messages/" + UUID.randomUUID())
              .contentType(APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(update)))
          .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("삭제 성공: 메시지 삭제 시 204")
    void delete_success() throws Exception {
      UserCreateRequest userReq = new UserCreateRequest("delMsgUser", "delmsg@test.com", "pass");
      ResultActions userRes = mockMvc.perform(post("/api/users")
          .contentType(APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(userReq)));
      UUID authorId = UUID.fromString(objectMapper.readTree(userRes.andReturn().getResponse().getContentAsString()).get("id").asText());

      PrivateChannelCreateRequest channelReq = new PrivateChannelCreateRequest(List.of(authorId));
      ResultActions channelRes = mockMvc.perform(post("/api/channels/private")
          .contentType(APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(channelReq)));
      UUID channelId = UUID.fromString(objectMapper.readTree(channelRes.andReturn().getResponse().getContentAsString()).get("id").asText());

      MessageCreateRequest msgReq = new MessageCreateRequest("To delete", channelId, authorId);
      MockMultipartFile part = new MockMultipartFile("messageCreateRequest", "",
          "application/json", objectMapper.writeValueAsBytes(msgReq));
      ResultActions createRes = mockMvc.perform(multipart("/api/messages").file(part));
      UUID messageId = UUID.fromString(objectMapper.readTree(createRes.andReturn().getResponse().getContentAsString()).get("id").asText());

      mockMvc.perform(delete("/api/messages/" + messageId))
          .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("삭제 실패: 존재하지 않는 메시지 ID 시 404")
    void delete_fail_notFound() throws Exception {
      mockMvc.perform(delete("/api/messages/" + UUID.randomUUID()))
          .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("목록 조회: 채널에 메시지 없을 때 빈 배열")
    void list_empty() throws Exception {
      UserCreateRequest userReq = new UserCreateRequest("emptyMsgUser", "empty@test.com", "pass");
      ResultActions userRes = mockMvc.perform(post("/api/users")
          .contentType(APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(userReq)));
      UUID authorId = UUID.fromString(objectMapper.readTree(userRes.andReturn().getResponse().getContentAsString()).get("id").asText());

      PrivateChannelCreateRequest channelReq = new PrivateChannelCreateRequest(List.of(authorId));
      ResultActions channelRes = mockMvc.perform(post("/api/channels/private")
          .contentType(APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(channelReq)));
      UUID channelId = UUID.fromString(objectMapper.readTree(channelRes.andReturn().getResponse().getContentAsString()).get("id").asText());

      mockMvc.perform(get("/api/messages").param("channelId", channelId.toString()))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$").isArray())
          .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("목록 조회: 메시지 생성 후 해당 채널 목록에 포함")
    void list_withData() throws Exception {
      UserCreateRequest userReq = new UserCreateRequest("listMsgUser", "listmsg@test.com", "pass");
      ResultActions userRes = mockMvc.perform(post("/api/users")
          .contentType(APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(userReq)));
      UUID authorId = UUID.fromString(objectMapper.readTree(userRes.andReturn().getResponse().getContentAsString()).get("id").asText());

      PrivateChannelCreateRequest channelReq = new PrivateChannelCreateRequest(List.of(authorId));
      ResultActions channelRes = mockMvc.perform(post("/api/channels/private")
          .contentType(APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(channelReq)));
      UUID channelId = UUID.fromString(objectMapper.readTree(channelRes.andReturn().getResponse().getContentAsString()).get("id").asText());

      MessageCreateRequest msgReq = new MessageCreateRequest("List me", channelId, authorId);
      MockMultipartFile part = new MockMultipartFile("messageCreateRequest", "",
          "application/json", objectMapper.writeValueAsBytes(msgReq));
      mockMvc.perform(multipart("/api/messages").file(part)).andExpect(status().isCreated());

      mockMvc.perform(get("/api/messages").param("channelId", channelId.toString()))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$").isArray())
          .andExpect(jsonPath("$[?(@.content=='List me')]").exists());
    }
  }
}
