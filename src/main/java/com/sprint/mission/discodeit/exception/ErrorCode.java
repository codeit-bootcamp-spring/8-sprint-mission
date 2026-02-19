package com.sprint.mission.discodeit.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {
  USER_NOT_FOUND("해당 사용자를 찾을 수 없습니다."),
  DUPLICATE_USER("이미 존재하는 사용자입니다."),

  CHANNEL_NOT_FOUND("해당 채널을 찾을 수 없습니다."),
  PRIVATE_CHANNEL_UPDATE("비공개 채널은 수정할 수 없습니다."),

  MESSAGE_NOT_FOUND("해당 메시지를 찾을 수 없습니다."),

  RESOURCE_NOT_FOUND("요청하신 리소스를 찾을 수 없습니다."),

  INVALID_CREDENTIALS("이메일 또는 비밀번호가 일치하지 않습니다."), // 로그인 실패 전용
  UNAUTHORIZED("로그인이 필요한 서비스입니다."), // 세션이 만료되었거나 없을 때
  FORBIDDEN("해당 리소스에 대한 접근 권한이 없습니다."), // 남의 메시지를 삭제하려 할 때

  //요청 데이터 검증
  INVALID_INPUT_VALUE("입력값이 올바르지 않습니다."), //@Valid 검사 실패용
  METHOD_NOT_ALLOWED("지원하지 않는 HTTP 메소드입니다."), //POST 자리에 GET 보냈을 때

  //비즈니스 로직 제약
  ALREADY_PROCESSED("이미 처리된 요청입니다."), //중복 요청 방지
  INVALID_STATE("잘못된 상태 변경 요청입니다."), //이미 삭제된 유저를 또 삭제하려 할 때

  //서버 및 시스템
  INTERNAL_SERVER_ERROR("서버 내부 오류가 발생했습니다."); //최후의 보루 (500 에러 포장지)
  
  private final String message;

  ErrorCode(String message) {
    this.message = message;
  }
}
