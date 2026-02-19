package com.sprint.mission.discodeit.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.dto.user.UserCreateRequest;
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
class UserApiIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Test
  @DisplayName("통합: 유저 생성 성공 -> 201")
  void createUser_success() throws Exception {
    UserCreateRequest req = new UserCreateRequest("jun", "jun@test.com", "pw");

    MockMultipartFile requestPart = new MockMultipartFile(
        "userCreateRequest", "", "application/json",
        objectMapper.writeValueAsBytes(req)
    );

    mockMvc.perform(
            multipart("/api/users")
                .file(requestPart)
                .contentType(MediaType.MULTIPART_FORM_DATA)
        )
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.username").value("jun"))
        .andExpect(jsonPath("$.email").value("jun@test.com"));
  }

  @Test
  @DisplayName("통합: 유저 목록 조회 성공 -> 200")
  void findAll_success() throws Exception {
    mockMvc.perform(get("/api/users"))
        .andExpect(status().isOk());
  }
}
