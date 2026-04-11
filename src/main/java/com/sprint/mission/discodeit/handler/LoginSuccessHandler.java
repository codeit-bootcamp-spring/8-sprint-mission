package com.sprint.mission.discodeit.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.DTO.dto.UserDto;
import com.sprint.mission.discodeit.service.auth.DiscodeitUserDetails;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {

        log.info("[LoginSuccessHandler] 로그인 성공 처리 시작...");

        // discodeitUserDetails에서 사용자 정보 추출
        if (authentication.getPrincipal() instanceof DiscodeitUserDetails discodeitUserDetails) {
            UserDto userDto = discodeitUserDetails.getUserDto();

            // JSON 응답 설정
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.setStatus(HttpServletResponse.SC_OK);

            // 사용자 정보를 JSON으로 응답
            String responseBody = objectMapper.writeValueAsString(userDto);
            response.getWriter().write(responseBody);

            log.info("[LoginSuccessHandler] 로그인 성공 응답 완료!");
        } else {
            // 예상치 못한 Principal 타입일 경우
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\": \"인증 정보를 처리할 수 없습니다.\"}");

            log.error("[LoginSuccessHandler] 예상치 못한 Principal 타입: {}", authentication.getPrincipal().getClass());
        }
    }
}
