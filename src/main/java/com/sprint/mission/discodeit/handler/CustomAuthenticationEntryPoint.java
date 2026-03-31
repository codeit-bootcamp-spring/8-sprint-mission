package com.sprint.mission.discodeit.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
            throws IOException, ServletException {
        log.info("[CustomAuthenticationEntryPoint] 인증 실패 처리 시작");
        log.warn("[CustomAuthenticationEntryPoint] 인증되지 않은 접근입니다. 요청 URL: {}", request.getRequestURL());
        log.warn("[CustomAuthenticationEntryPoint] 예외 메시지: {}", authException.getMessage());

        // JSON 에러 응답 생성
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("error", "UNAUTHORIZED");
        errorResponse.put("message", "로그인이 필요하거나 유효하지 않은 인증 정보입니다.");
        errorResponse.put("status", 401);

        // 응답 헤더 설정 (401 Unauthorized)
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        // JSON 응답 전송
        String responseBody = objectMapper.writeValueAsString(errorResponse);
        response.getWriter().write(responseBody);

        log.info("[CustomAuthenticationEntryPoint] 인증 실패 처리 완료!");
    }
}
