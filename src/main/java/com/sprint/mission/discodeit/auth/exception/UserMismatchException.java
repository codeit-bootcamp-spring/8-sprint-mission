package com.sprint.mission.discodeit.auth.exception;

import com.sprint.mission.discodeit.global.exception.AuthException;
import com.sprint.mission.discodeit.global.exception.ErrorCode;

import java.util.Map;

public class UserMismatchException extends AuthException
{
    public UserMismatchException() {
        super(ErrorCode.USER_MISMATCH, Map.of());
    }
}
