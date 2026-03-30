package com.sprint.mission.discodeit.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class LoginFailureHandler implements AuthenticationFailureHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void onAuthenticationFailure(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception
    ) throws IOException, ServletException {

        log.info("[LoginFailureHandler] 로그인 실패 처리 시작...");
        log.info("[LoginFailureHandler] 실패 사유: " + exception.getClass().getSimpleName());

        // 에러 메시지 결정
        String errorMessage = "";
        if (exception instanceof BadCredentialsException) {
            errorMessage = "아이디 또는 비밀번호가 올바르지 않습니다.";
        } else if (exception instanceof DisabledException) {
            errorMessage = "비활성화된 계정입니다.";
        } else {
            errorMessage = "로그인에 실패했습니다.";
        }

        // JSON 에러 응답 생성
        Map<String, Object> errorResponse = new HashMap<>();

        errorResponse.put("success", false);
        errorResponse.put("error", "AUTHENTICATION_FAILED");
        errorResponse.put("message", errorMessage);

        // 응답 헤더 설정
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        // JSON 응답 전송
        String responseBody = objectMapper.writeValueAsString(errorResponse);
        response.getWriter().write(responseBody);

        log.info("[LoginFailureHandler] 로그인 실패 응답 완료!");
    }
}
