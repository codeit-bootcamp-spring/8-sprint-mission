package com.sprint.mission.discodeit.Exception;

public class AlreadyLoggedInException extends RuntimeException {
    public AlreadyLoggedInException() {
        super("이미 로그인된 유저입니다.");
    }
}
