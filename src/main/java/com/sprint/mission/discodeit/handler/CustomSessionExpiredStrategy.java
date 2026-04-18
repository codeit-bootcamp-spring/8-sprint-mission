package com.sprint.mission.discodeit.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.web.session.SessionInformationExpiredEvent;
import org.springframework.security.web.session.SessionInformationExpiredStrategy;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class CustomSessionExpiredStrategy implements SessionInformationExpiredStrategy {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void onExpiredSessionDetected(SessionInformationExpiredEvent event)
            throws IOException {

        log.info("[CustomSessionExpiredStrategy] - 세션 만료 처리 시작...");

        HttpServletResponse response = event.getResponse();

        // 세션 만료 안내 메시지 작성 (to. Client)
        Map<String, Object> result = new HashMap<>();

        result.put("success", false);
        result.put("code", "SESSION_EXPIRED");
        result.put("message", "다른 곳에서 로그인되어 현재 세션이 만료되었습니다. 다시 로그인해주세요.");

        // JSON 응답 설정
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        response.getWriter().write(objectMapper.writeValueAsString(result));
    }
}
