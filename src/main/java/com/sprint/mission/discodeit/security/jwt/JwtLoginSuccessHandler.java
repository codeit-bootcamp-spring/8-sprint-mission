package com.sprint.mission.discodeit.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.dto.JwtDTO;
import com.sprint.mission.discodeit.dto.JwtInformation;
import com.sprint.mission.discodeit.dto.UserDto;
import com.sprint.mission.discodeit.dto.response.ErrorResponse;
import com.sprint.mission.discodeit.entity.DiscodeitUserDetails;
import com.sprint.mission.discodeit.exception.ErrorCode;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtLoginSuccessHandler implements AuthenticationSuccessHandler {

  private final ObjectMapper objectMapper;
  private final JwtTokenProvider tokenProvider;
  private final JwtRegistry jwtRegistry;
  private final CacheManager cacheManager;

  @Override
  public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
      Authentication authentication) throws IOException, ServletException {

    // 응답 인코딩/콘텐츠 타입 설정
    response.setCharacterEncoding("UTF-8");
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);

    if (authentication.getPrincipal() instanceof DiscodeitUserDetails discodeitUserDetails) {
      try {
        String accessToken = tokenProvider.generateAccessToken(discodeitUserDetails);
        String refreshToken = tokenProvider.generateRefreshToken(discodeitUserDetails);

        tokenProvider.addRefreshCookie(response, refreshToken);

        UserDto userDto = discodeitUserDetails.getUserDto();

        UserDto onlineUserDto = new UserDto(
            userDto.id(),
            userDto.username(),
            userDto.email(),
            userDto.profile(),
            true,
            userDto.role()
        );

        JwtDTO jwtDto = new JwtDTO(onlineUserDto, accessToken);
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().write(objectMapper.writeValueAsString(jwtDto));

        jwtRegistry.registerJwtInformation(
            new JwtInformation(
                discodeitUserDetails.getUserDto(),
                accessToken,
                refreshToken
            )
        );

        evictCache(discodeitUserDetails.getUserDto().id());

      } catch (Exception e) {
        ErrorCode errorCode = ErrorCode.INTERNAL_SERVER_ERROR;

        response.setStatus(errorCode.getStatus().value());

        ErrorResponse errorResponse = new ErrorResponse(
            Instant.now(),
            errorCode.name(),
            errorCode.getMessage(),
            null,
            e.getClass().getSimpleName(),
            errorCode.getStatus().value()
        );

        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
      }
    } else {
      ErrorCode errorCode = ErrorCode.UNEXPECTED_PRINCIPAL_TYPE;

      response.setStatus(errorCode.getStatus().value());

      ErrorResponse errorResponse = new ErrorResponse(
          Instant.now(),
          errorCode.name(),
          errorCode.getMessage(),
          null,
          "SecurityContext Principal Type Mismatch",
          errorCode.getStatus().value()
      );

      response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
  }

  private void evictCache(UUID userId) {
    Cache userCache = cacheManager.getCache("user");

    if (userCache != null) {
      userCache.clear();
      log.info("[JwtLoginSuccessHandler] 로그인 성공으로 인한 사용자 캐시 삭제: {}", userId);
    }

    Cache channelCache = cacheManager.getCache("channel");
    if (channelCache != null) {
      channelCache.clear();
      log.info("[JwtLoginSuccessHandler] 로그인 성공으로 인한 채널 캐시 삭제: {}", userId);
    }
  }
}
