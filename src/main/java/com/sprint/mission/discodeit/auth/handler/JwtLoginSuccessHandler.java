package com.sprint.mission.discodeit.auth.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.auth.dto.info.JwtInformation;
import com.sprint.mission.discodeit.auth.dto.response.JwtDto;
import com.sprint.mission.discodeit.auth.service.DiscodeitUserDetails;
import com.sprint.mission.discodeit.domain.user.event.UserLogInOutEvent;
import com.sprint.mission.discodeit.global.secutiry.JwtRegistry;
import com.sprint.mission.discodeit.global.secutiry.JwtTokenProvider;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@RequiredArgsConstructor
@Component
public class JwtLoginSuccessHandler implements AuthenticationSuccessHandler
{
    private final ObjectMapper objectMapper;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtRegistry jwtRegistry;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        if (authentication.getPrincipal() instanceof DiscodeitUserDetails userDetails) {
            try {
                String accessToken = jwtTokenProvider.generateAccessToken(userDetails);
                String refreshToken = jwtTokenProvider.generateRefreshToken(userDetails);

                jwtRegistry.registerJwtInformation(
                        new JwtInformation(
                                userDetails.getUserDto(),
                                accessToken,
                                refreshToken
                        )
                );

                Cookie refreshTokenCookie = jwtTokenProvider.generateRefreshTokenCookie(refreshToken);
                response.addCookie(refreshTokenCookie);

                JwtDto jwtDto = new JwtDto(userDetails.getUserDto(), accessToken);

                response.setStatus(HttpServletResponse.SC_OK);
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write(objectMapper.writeValueAsString(jwtDto));

                applicationEventPublisher.publishEvent(new UserLogInOutEvent(userDetails.getUserDto().id(), true));

            } catch (Exception e) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().write(objectMapper.createObjectNode()
                        .put("success", false)
                        .put("message", "Token generation failed")
                        .toString());
            }
        } else {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write(objectMapper.createObjectNode()
                    .put("success", false)
                    .put("message", "Invalid principal")
                    .toString());
        }
    }
}
