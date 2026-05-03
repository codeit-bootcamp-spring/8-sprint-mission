package com.sprint.mission.discodeit.event.message;

import com.sprint.mission.discodeit.config.MDCLoggingInterceptor;
import java.util.UUID;
import lombok.Getter;
import org.slf4j.MDC;

@Getter
public class S3UploadFailedEvent {

  private final UUID binaryContentId;
  private final String errorMessage;    // e.getMessage()
  private final String requestId;       // MDC에서 가져온 것

  public S3UploadFailedEvent(UUID binaryContentId, Throwable e) {
    this.binaryContentId = binaryContentId;
    this.errorMessage = e.getMessage();
    this.requestId = MDC.get(MDCLoggingInterceptor.REQUEST_ID);
  }
}