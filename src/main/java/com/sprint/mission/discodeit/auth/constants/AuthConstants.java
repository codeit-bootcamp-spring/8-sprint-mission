package com.sprint.mission.discodeit.auth.constants;

public final class AuthConstants {

  private AuthConstants() {
    // 유틸리티 클래스는 인스턴스화하지 않음
    throw new AssertionError("Utility class cannot be instantiated");
  }

  // ==================== Cookie Names ====================
  public static final String COOKIE_SESSION_ID = "JSESSIONID";
  public static final String COOKIE_XSRF_TOKEN = "XSRF-TOKEN";

  // ==================== Error Codes ====================
  public static final String ERROR_UNAUTHORIZED = "UNAUTHORIZED";
  public static final String ERROR_AUTHENTICATION_FAILED = "AUTHENTICATION_FAILED";
  public static final String ERROR_ACCESS_DENIED = "ACCESS_DENIED";
  public static final String ERROR_SESSION_EXPIRED = "SESSION_EXPIRED";
  public static final String ERROR_INTERNAL_SERVER_ERROR = "INTERNAL_SERVER_ERROR";

  // ==================== Error Messages ====================
  public static final String MSG_LOGIN_REQUIRED = "로그인이 필요합니다.";
  public static final String MSG_INVALID_CREDENTIALS = "아이디 또는 비밀번호가 올바르지 않습니다.";
  public static final String MSG_ACCOUNT_DISABLED = "비활성화된 계정입니다.";
  public static final String MSG_LOGIN_FAILED = "로그인에 실패했습니다. 다시 시도해주세요.";
  public static final String MSG_ACCESS_DENIED = "해당 리소스에 접근할 권한이 없습니다.";
  public static final String MSG_SESSION_EXPIRED = "다른 기기에서의 로그인으로 인해 현재 세션이 만료되었습니다. 다시 로그인해주세요.";
  public static final String MSG_AUTH_PROCESSING_ERROR = "인증 정보를 처리하는 중 오류가 발생했습니다.";

  // ==================== Log Prefixes ====================
  public static final String LOG_PREFIX_LOGIN_SUCCESS = "[LoginSuccessHandler]";
  public static final String LOG_PREFIX_LOGIN_FAILURE = "[LoginFailureHandler]";
  public static final String LOG_PREFIX_ACCESS_DENIED = "[CustomAccessDeniedHandler]";
  public static final String LOG_PREFIX_SESSION_EXPIRED = "[CustomSessionExpiredStrategy]";
  public static final String LOG_PREFIX_CSRF_TOKEN = "[CsrfTokenController]";
}

