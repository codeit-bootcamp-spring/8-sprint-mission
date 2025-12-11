package com.sprint.mission.discodeit.util;

// 유효성 검사 로직을 위한 유틸리티 클래스입니다.
public class ValidationUtil {

    /**
     * 문자열 값이 null이거나 공백 문자열(empty string)인지 확인하고,
     * 유효하지 않으면 IllegalArgumentException을 발생시킵니다.
     * * @param value 검사할 문자열 값
     * @param fieldName 오류 메시지에 사용할 필드 이름 (예: "이름", "이메일")
     */
    public static void validateNotNullOrEmpty(String value, String fieldName) {
        if (value == null) {
            throw new IllegalArgumentException(fieldName + "은(는) null일 수 없습니다.");
        }

        // trim()을 사용하여 앞뒤 공백을 제거한 후 길이가 0인지 확인
        if (value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + "은(는) 빈 값일 수 없습니다.");
        }
    }

    // 향후 다른 유효성 검사 메서드를 추가할 수 있습니다. (예: validateEmailFormat 등)
}