package com.sprint.mission.discodeit.util;

import java.util.regex.Pattern;

public class ValidationUtil {
    private static final String EMAIL_REGEX = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$";
    //  멘토 피드백 반영: 상수로 미리 컴파일하여 재사용 (성능 최적화)
    private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);

    public static void validateNotNullOrEmpty(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + "은(는) 필수 입력 값입니다.");
        }
    }

    //  이메일 형식 검증 메서드 추가
    public static void validateEmailFormat(String email) {
        if (email == null || !EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException("유효하지 않은 이메일 형식입니다.");
        }
    }
}