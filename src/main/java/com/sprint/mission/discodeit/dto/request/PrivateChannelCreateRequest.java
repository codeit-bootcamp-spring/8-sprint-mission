package com.sprint.mission.discodeit.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;

public record PrivateChannelCreateRequest(

    //NotEmpty는 List,Map등 전용
    @NotEmpty(message = "비공개 채널은 최소 한 명 이상의 참여자가 필요합니다.")
    @Size(min = 1, message = "참여자는 최소 1명 이상이어야 합니다.")
    List<UUID> participantIds
) {

}
