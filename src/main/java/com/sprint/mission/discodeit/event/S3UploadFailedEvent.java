package com.sprint.mission.discodeit.event;

import com.sprint.mission.discodeit.config.MDCLoggingInterceptor;
import java.util.UUID;
import lombok.Getter;
import org.slf4j.MDC;

@Getter
public class S3UploadFailedEvent {

  private final UUID binaryContentId;
  private final Throwable exception;
  private final String requestId;

  public S3UploadFailedEvent(UUID binaryContentId, Throwable exception) {
    this.binaryContentId = binaryContentId;
    this.exception = exception;
    this.requestId = MDC.get(MDCLoggingInterceptor.REQUEST_ID);
  }
}