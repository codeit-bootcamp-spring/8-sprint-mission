package com.sprint.mission.discodeit.util;

import java.util.regex.Pattern;

public class ValidationUtil {

    // 멘토님 피드백 반영: 정규식 패턴을 상수로 미리 컴파일하여 재사용합니다.
    private static final String EMAIL_REGEX = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$";
    private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);

    public static void validateNotNull(Object value, String fieldName) {
        if (value == null) {
            throw new IllegalArgumentException(fieldName + "은(는) null일 수 없습니다.");
        }
    }

    public static void validateEmail(String email) {
        // Pattern.matches(EMAIL_REGEX, email) 대신 미리 컴파일된 객체의 matcher를 사용합니다.
        if (email == null || !EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException("올바르지 않은 이메일 형식입니다.");
        }
    }
}