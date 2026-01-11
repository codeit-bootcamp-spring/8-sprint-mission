package com.sprint.mission.discodeit.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateRequest {
    private UUID id;
    private String name;
    private String password;
    private String profileImage; //  추가

    public String getEmail() {
        return "";
    }
}