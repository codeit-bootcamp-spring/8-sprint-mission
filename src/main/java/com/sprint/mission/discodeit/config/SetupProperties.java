package com.sprint.mission.discodeit.config;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "discodeit.setup")
public class SetupProperties {

    @NotBlank
    private String defaultPassword;
    private Admin admin = new Admin();
    
    private List<SeedUser> seedUsers = new ArrayList<>();

    @Getter
    @Setter
    public static class Admin {
        @NotBlank
        private String username;

        @Email
        @NotBlank
        private String email;

        @NotBlank
        private String password;
    }

    @Getter
    @Setter
    public static class SeedUser {
        @NotBlank
        private String username;

        @Email
        @NotBlank
        private String email;
    }
}
