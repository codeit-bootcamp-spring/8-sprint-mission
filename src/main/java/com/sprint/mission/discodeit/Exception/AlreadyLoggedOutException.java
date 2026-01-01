package com.sprint.mission.discodeit.Exception;

public class AlreadyLoggedOutException extends RuntimeException {
    public AlreadyLoggedOutException() {
        super("이미 로그아웃된 유저입니다.");
    }
}
