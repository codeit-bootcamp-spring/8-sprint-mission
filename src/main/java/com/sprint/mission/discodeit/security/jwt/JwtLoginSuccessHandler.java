package com.sprint.mission.discodeit.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.DTO.dto.JwtDTO;
import com.sprint.mission.discodeit.DTO.dto.UserDto;
import com.sprint.mission.discodeit.security.jwt.store.JwtInformation;
import com.sprint.mission.discodeit.security.jwt.store.JwtRegistry;
import com.sprint.mission.discodeit.security.jwt.store.JwtTokenEntity;
import com.sprint.mission.discodeit.service.auth.DiscodeitUserDetails;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class JwtLoginSuccessHandler implements AuthenticationSuccessHandler {

    private final ObjectMapper objectMapper;
    private final JwtTokenProvider tokenProvider;
    private final JwtRegistry jwtRegistry;

    public JwtLoginSuccessHandler(ObjectMapper objectMapper, JwtTokenProvider tokenProvider, JwtRegistry jwtRegistry) {
        log.info("[JwtLoginSuccessHandler] 생성자 호출됨: 응답 JSON 직렬화를 위한 매퍼, JWT 생성/쿠키 유틸리티, 토큰 상태 저장소 주입");
        this.objectMapper = objectMapper;
        this.tokenProvider = tokenProvider;
        this.jwtRegistry = jwtRegistry;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        log.info("[JwtLoginSuccessHandler] onAuthenticationSuccess 시작: 응답 구성 준비");

        // 응답 인코딩/콘텐츠 타입 설정
        response.setCharacterEncoding("UTF-8");
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        // Principal 유효성 확인 및 캐스팅
        if (authentication.getPrincipal() instanceof DiscodeitUserDetails userDetails) {
            try {
                log.info("[jwtLoginSuccessHandler] 기존 토큰 무효화 시작 - username={}", userDetails.getUsername());

                // 새 Access/Refresh 발급
                log.info("[JwtLoginSuccessHandler] 새 토큰 발급 시작");
                String accessToken = tokenProvider.generateAccessToken(userDetails);
                String refreshToken = tokenProvider.generateRefreshToken(userDetails);

                JwtInformation information = new JwtInformation(
                        userDetails.getUserDto(),
                        accessToken,
                        refreshToken
                );

                jwtRegistry.registerJwtInformation(information);
                log.info("[JwtLoginSuccessHandler] JwtRegistry에 새 토큰 등록 완료");

                // 토큰 메타데이터 저장 (toEntity로 중복 제거)
                log.info("[JwtLoginSuccessHandler] 토큰 메타데이터 저장 시작");
                JwtTokenEntity accessEntity = tokenProvider.toEntity(accessToken);
                JwtTokenEntity refreshEntity = tokenProvider.toEntity(refreshToken);

                // 리프레시 쿠키 설정
                log.info("[JwtLoginSuccessHandler] 리프레시 쿠키 설정 시작");
                tokenProvider.addRefreshCookie(response, refreshToken);

                // 사용자 DTO 구성
                UserDto userDto = userDetails.getUserDto();

                JwtDTO jwtDto = new JwtDTO(userDto, accessToken);
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().write(objectMapper.writeValueAsString(jwtDto));

                log.info("[JwtLoginSuccessHandler] onAuthenticationSuccess 완료: 응답 전송됨");
            } catch (Exception e) {
                // 예외 발생 시 처리(500)
                log.info("[JwtLoginSuccessHandler] 예외 발생: {}", e.getMessage());
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().write(objectMapper.createObjectNode()
                        .put("success", false)
                        .put("message", "Token generate failed")
                        .toString());
            }
        } else {
            // 인증 실패 시 처리(401)
            log.info("[JwtLoginSuccessHandler] Invalid principal: {}", authentication.getPrincipal());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write(objectMapper.createObjectNode()
                    .put("success", false)
                    .put("message", "Invalid principal")
                    .toString());
        }
    }
}
