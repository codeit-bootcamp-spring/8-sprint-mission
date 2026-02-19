package com.sprint.mission.discodeit.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 요청마다 요청 ID, URL, 메소드를 MDC에 넣고 응답 헤더에 요청 ID를 담는 인터셉터.
 */
public class MDCLoggingInterceptor implements HandlerInterceptor {

  public static final String MDC_REQUEST_ID = "requestId";
  public static final String MDC_REQUEST_METHOD = "requestMethod";
  public static final String MDC_REQUEST_URI = "requestUri";
  public static final String HEADER_REQUEST_ID = "Discodeit-Request-ID";

  private static final int SHORT_ID_LENGTH = 8;

  @Override
  public boolean preHandle(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull Object handler) {
    String fullId = UUID.randomUUID().toString();
    String shortId = fullId.substring(0, SHORT_ID_LENGTH);
    request.setAttribute(HEADER_REQUEST_ID, fullId);

    MDC.put(MDC_REQUEST_ID, shortId);
    MDC.put(MDC_REQUEST_METHOD, request.getMethod());
    MDC.put(MDC_REQUEST_URI, request.getRequestURI());
    return true;
  }

  @Override
  public void afterCompletion(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull Object handler,
      @Nullable Exception ex) {
    String requestId = (String) request.getAttribute(HEADER_REQUEST_ID);
    if (requestId != null) {
      response.setHeader(HEADER_REQUEST_ID, requestId);
    }
    MDC.remove(MDC_REQUEST_ID);
    MDC.remove(MDC_REQUEST_METHOD);
    MDC.remove(MDC_REQUEST_URI);
  }
}
