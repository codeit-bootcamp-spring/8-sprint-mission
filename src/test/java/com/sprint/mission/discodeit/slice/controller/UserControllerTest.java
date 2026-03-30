package com.sprint.mission.discodeit.slice.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.controller.UserController;
import com.sprint.mission.discodeit.dto.BinaryContentDto;
import com.sprint.mission.discodeit.dto.UserDto;
import com.sprint.mission.discodeit.dto.request.UserCreateRequest;
import com.sprint.mission.discodeit.dto.request.UserUpdateRequest;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.Role;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.service.UserService;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(UserController.class)
@DisplayName("UserController 테스트")
public class UserControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private UserService userService;

  private UUID userId;
  private UUID profileId;
  private byte[] profileBytes;
  private BinaryContent testProfile;
  private BinaryContentDto testProfileDto;
  private User testUser;
  private UserDto testUserDto;

  @BeforeEach
  void setUp() {
    userId = UUID.randomUUID();
    profileId = UUID.randomUUID();
    profileBytes = new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
    testProfile = new BinaryContent("profile.png", 10L, "image/png");
    testProfileDto = new BinaryContentDto(profileId, "profile.png", 10L, "image/png");
    testUser = new User("testUser", "test@naver.com", "test1234", testProfile);
    testUserDto = new UserDto(userId, "testUser", "test@naver.com", testProfileDto, Role.USER);
  }

  @Test
  @DisplayName("사용자 등록 요청이 성공적으로 처리되어야 한다.")
  void createUser_ValidRequest_Success() throws Exception {

    // given
    UserCreateRequest userReq = new UserCreateRequest("testUser", "test@naver.com", "test1234");
    String userJson = objectMapper.writeValueAsString(userReq);

    MockMultipartFile userPart = new MockMultipartFile(
        "userCreateRequest",
        "",
        "application/json",
        userJson.getBytes(StandardCharsets.UTF_8));

    MockMultipartFile profilePart = new MockMultipartFile(
        "profile",
        "profile.png",
        "image/png",
        profileBytes);

    given(userService.create(any(), any())).willReturn(testUserDto);

    // when & then
    mockMvc.perform(multipart("/api/users")
            .file(userPart)
            .file(profilePart))
        .andDo(print())
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.username").value(testUser.getUsername()))
        .andExpect(jsonPath("$.id").exists());

    then(userService).should().create(eq(userReq), any());
  }

  @Test
  @DisplayName("유효하지 않은 이메일 형식으로 등록 요청 시 400 에러가 발생해야 한다.")
  void createUser_InValidEmail_BadRequests() throws Exception {

    // given
    UserCreateRequest invalidUserReq = new UserCreateRequest("testUser", "wrong-email-format",
        "test1234");
    String userJson = objectMapper.writeValueAsString(invalidUserReq);

    MockMultipartFile userPart = new MockMultipartFile(
        "userCreateRequest",
        "",
        "application/json",
        userJson.getBytes(StandardCharsets.UTF_8));

    MockMultipartFile profilePart = new MockMultipartFile(
        "profile",
        "profile.png",
        "image/png",
        profileBytes);

    // when & then
    mockMvc.perform(multipart("/api/users")
            .file(userPart)
            .file(profilePart))
        .andDo(print())
        .andExpect(status().isBadRequest());

    then(userService).shouldHaveNoInteractions();
  }

  @Test
  @DisplayName("사용자 수정 요청이 성공적으로 처리되어야 한다.")
  void updateUser_ValidRequest_Success() throws Exception {

    // given
    UserUpdateRequest userReq = new UserUpdateRequest("updateTestUser", "test@naver.com",
        "test1234");
    String userJson = objectMapper.writeValueAsString(userReq);

    MockMultipartFile userPart = new MockMultipartFile(
        "userUpdateRequest",
        "",
        "application/json",
        userJson.getBytes(StandardCharsets.UTF_8));

    testUserDto = new UserDto(userId, "updateTestUser", "test@naver.com", testProfileDto,
        Role.USER);
    given(userService.update(eq(userId), eq(userReq), any())).willReturn(testUserDto);

    // when & then
    mockMvc.perform(multipart("/api/users/{userId}", userId)
            .file(userPart)
            .with(request -> {
              request.setMethod("PATCH");
              return request;
            }))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.username").value("updateTestUser"));

    then(userService).should().update(eq(userId), eq(userReq), any());
  }

  @Test
  @DisplayName("존재하지 않는 사용자 Id로 수정 요청 시 404 에러가 발생해야 한다.")
  void updateUser_UserNotFound_NotFound() throws Exception {

    // given
    UserUpdateRequest userReq = new UserUpdateRequest("updateTestUser", "test@naver.com",
        "test1234");
    String userJson = objectMapper.writeValueAsString(userReq);

    MockMultipartFile userPart = new MockMultipartFile(
        "userUpdateRequest",
        "",
        "application/json",
        userJson.getBytes(StandardCharsets.UTF_8));

    UUID nonExistentUserId = UUID.randomUUID();
    given(userService.update(eq(nonExistentUserId), any(), any()))
        .willThrow(new UserNotFoundException(nonExistentUserId));

    // when & then
    mockMvc.perform(multipart("/api/users/{userId}", nonExistentUserId)
            .file(userPart)
            .with(request -> {
              request.setMethod("PATCH");
              return request;
            }))
        .andDo(print())
        .andExpect(status().isNotFound());
  }
}
