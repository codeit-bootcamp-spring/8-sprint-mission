package com.sprint.mission.discodeit.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.dto.user.UserCreateRequest;
import com.sprint.mission.discodeit.dto.user.UserDto;
import com.sprint.mission.discodeit.service.UserService;
import com.sprint.mission.discodeit.service.UserStatusService;
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

@WebMvcTest(UserController.class)
@ActiveProfiles("test")
class UserControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private UserService userService;

  @MockitoBean
  private UserStatusService userStatusService;

  @MockitoBean
  private JpaMetamodelMappingContext jpaMappingContext;

  @Test
  @DisplayName("POST /api/users: 성공 - 201 + JSON 응답 검증")
  void create_success() throws Exception {
    UUID id = UUID.randomUUID();
    UserDto response = new UserDto(id, "jun", "jun@test.com", null, false);

    when(userService.create(any(), any())).thenReturn(response);

    UserCreateRequest req = new UserCreateRequest("jun", "jun@test.com", "testPassword");
    byte[] json = objectMapper.writeValueAsBytes(req);

    MockMultipartFile requestPart = new MockMultipartFile(
        "userCreateRequest", "", "application/json", json
    );

    mockMvc.perform(
            multipart("/api/users")
                .file(requestPart)
                .contentType(MediaType.MULTIPART_FORM_DATA)
        )
        .andExpect(status().isCreated())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.id").value(id.toString()))
        .andExpect(jsonPath("$.username").value("jun"))
        .andExpect(jsonPath("$.email").value("jun@test.com"));
  }

  @Test
  @DisplayName("POST /api/users: 실패 - validation 에러(빈 username) -> 400")
  void create_fail_validation() throws Exception {
    UserCreateRequest req = new UserCreateRequest("", "jun@test.com", "testPassword");
    byte[] json = objectMapper.writeValueAsBytes(req);

    MockMultipartFile requestPart = new MockMultipartFile(
        "userCreateRequest", "", "application/json", json
    );

    mockMvc.perform(
            multipart("/api/users")
                .file(requestPart)
                .contentType(MediaType.MULTIPART_FORM_DATA)
        )
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));
  }
}
