package com.sprint.mission.discodeit.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.web.servlet.HandlerInterceptor;

public class MDCLoggingInterceptor implements HandlerInterceptor {

  public static final String HEADER_REQUEST_ID = "Discodeit-Request-ID";

  public static final String MDC_REQUEST_ID = "requestId";
  public static final String MDC_METHOD = "method";
  public static final String MDC_URL = "url";

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
      Object handler) {

    String requestId = UUID.randomUUID().toString();
    String method = request.getMethod();

    // URL(요구사항이 URL이라 URI+query 형태로 구성)
    String url = request.getRequestURI();
    if (request.getQueryString() != null) {
      url = url + "?" + request.getQueryString();
    }

    MDC.put(MDC_REQUEST_ID, requestId);
    MDC.put(MDC_METHOD, method);
    MDC.put(MDC_URL, url);

    response.setHeader(HEADER_REQUEST_ID, requestId);

    return true;
  }

  @Override
  public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
      Object handler, Exception ex) {

    MDC.clear();
  }
}
