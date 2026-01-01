package com.sprint.mission.discodeit.Exception;

public class NotExistReadStatusException extends RuntimeException {
    public NotExistReadStatusException() {
        super("userId와 channelId로 조회된 ReadStatus가 존재하지 않습니다.");
    }
}
