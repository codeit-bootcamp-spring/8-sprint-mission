package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.auth.JwtDto;
import jakarta.servlet.http.HttpServletResponse;

/*
    AuthService
    -------------------------
    로그인을 담당하는 서비스
 */
public interface AuthService {
  /**
   * 리프레시 토큰을 사용하여 새로운 토큰 세트를 발급
   * @param refreshToken 클라이언트로부터 받은 기존 리프레시 토큰
   * @return 발급된 액세스 토큰, 리프레시 토큰, 사용자 정보를 포함한 DTO
   */
  JwtDto refresh(String refreshToken, HttpServletResponse response);
}
