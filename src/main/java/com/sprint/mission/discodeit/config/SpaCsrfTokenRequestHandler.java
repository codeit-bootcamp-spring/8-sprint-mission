package com.sprint.mission.discodeit.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.function.Supplier;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.csrf.CsrfTokenRequestHandler;
import org.springframework.security.web.csrf.XorCsrfTokenRequestAttributeHandler;
import org.springframework.util.StringUtils;

public class SpaCsrfTokenRequestHandler implements CsrfTokenRequestHandler {

  private final CsrfTokenRequestHandler plain = new CsrfTokenRequestAttributeHandler();

  private final CsrfTokenRequestHandler xor = new XorCsrfTokenRequestAttributeHandler();

  @Override
  public void handle(HttpServletRequest request, HttpServletResponse response,
      Supplier<CsrfToken> csrfToken) {

    // BREACH 공격 방지를 위해 응답 본문에는 XOR된 토큰을 사용
    this.xor.handle(request, response, csrfToken);
    // 지연 로딩되는 토큰을 강제로 로드하여 쿠키에 렌더링되도록 함
    csrfToken.get();
  }

  @Override
  public String resolveCsrfTokenValue(HttpServletRequest request, CsrfToken csrfToken) {

    String headerValue = request.getHeader(csrfToken.getHeaderName());

    // 헤더에 토큰이 있으면 (SPA 요청) 평문 핸들러로, 없으면 (Form 요청) XOR 핸들러로 해석
    return (StringUtils.hasText(headerValue) ? this.plain : this.xor).resolveCsrfTokenValue(request,
        csrfToken);
  }
}
