package com.sprint.mission.discodeit.slice.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.controller.ChannelController;
import com.sprint.mission.discodeit.dto.ChannelDto;
import com.sprint.mission.discodeit.dto.UserDto;
import com.sprint.mission.discodeit.dto.request.PublicChannelUpdateRequest;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.Role;
import com.sprint.mission.discodeit.exception.channel.ChannelNotFoundException;
import com.sprint.mission.discodeit.service.ChannelService;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@Disabled
@WebMvcTest(ChannelController.class)
@DisplayName("ChannelController 테스트")
public class ChannelControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private ChannelService channelService;

  UUID channelId;
  UUID testUserId;
  UUID testUserId2;
  UserDto testUserDto;
  UserDto testUserDto2;

  @BeforeEach
  void setUp() {
    channelId = UUID.randomUUID();
    testUserId = UUID.randomUUID();
    testUserId2 = UUID.randomUUID();

    testUserDto = new UserDto(testUserId, "testUser", "test@naver.com", null, true, Role.USER);
    testUserDto2 = new UserDto(testUserId2, "testUser2", "test2@naver.com", null, true, Role.USER);

  }

  @Test
  @DisplayName("채널 수정 요청이 성공적으로 처리되어야 한다.")
  void updateMessage_ValidRequest_Success() throws Exception {

    // given
    PublicChannelUpdateRequest channelReq = new PublicChannelUpdateRequest("update channel",
        "update channel Description");
    ChannelDto testChannelDto = new ChannelDto(channelId, ChannelType.PUBLIC, "update channel",
        "update channel Description", List.of(testUserDto, testUserDto2), Instant.now());

    given(channelService.update(any(), any())).willReturn(testChannelDto);

    // when & then
    mockMvc.perform(patch("/api/channels/{channelId}", channelId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(channelReq)))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("update channel"))
        .andExpect(jsonPath("$.description").value("update channel Description"));

    then(channelService).should().update(eq(channelId), eq(channelReq));
  }

  @Test
  @DisplayName("존재하지 않는 채널 Id로 채널 수정 요청 시 404 에러가 발생해야 한다.")
  void updateMessage_ChannelNotFound_NotFound() throws Exception {

    // given
    UUID nonExistentChannelId = UUID.randomUUID();
    PublicChannelUpdateRequest channelReq = new PublicChannelUpdateRequest("update channel",
        "update channel Description");
    given(channelService.update(eq(nonExistentChannelId), any()))
        .willThrow(new ChannelNotFoundException(nonExistentChannelId));

    // when & then
    mockMvc.perform(patch("/api/channels/{channelId}", nonExistentChannelId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(channelReq)))
        .andDo(print())
        .andExpect(status().isNotFound());

    then(channelService).should().update(eq(nonExistentChannelId), any());
  }

  @Test
  @DisplayName("채널 삭제 요청이 성공적으로 처리되어야 한다.")
  void deleteMessage_ValidRequest_Success() throws Exception {

    // when & then
    mockMvc.perform(delete("/api/channels/{channelId}", channelId))
        .andDo(print())
        .andExpect(status().isNoContent());

    then(channelService).should().delete(eq(channelId));
  }

  @Test
  @DisplayName("존재하지 않는 채널 Id로 채널 삭제 요청 시 404 에러가 발생해야 한다.")
  void deleteMessage_ChannelNotFound_NotFound() throws Exception {

    // given
    UUID nonExistentChannelId = UUID.randomUUID();
    willThrow(new ChannelNotFoundException(nonExistentChannelId))
        .given(channelService).delete(eq(nonExistentChannelId));

    // when & then
    mockMvc.perform(delete("/api/channels/{channelId}", nonExistentChannelId))
        .andDo(print())
        .andExpect(status().isNotFound());

    then(channelService).should().delete(eq(nonExistentChannelId));
  }
}
