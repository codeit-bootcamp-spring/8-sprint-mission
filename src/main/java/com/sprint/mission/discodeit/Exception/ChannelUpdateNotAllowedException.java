package com.sprint.mission.discodeit.Exception;

public class ChannelUpdateNotAllowedException extends RuntimeException {
    public ChannelUpdateNotAllowedException() {
        super("수정할 수 없는 채널입니다.");
    }
}
