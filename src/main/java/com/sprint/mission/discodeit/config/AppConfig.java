package com.sprint.mission.discodeit.config;

import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.file.FileUserRepository;
import com.sprint.mission.discodeit.repository.jcf.JCFUserRepository;
import com.sprint.mission.discodeit.service.ChannelService;
import com.sprint.mission.discodeit.service.MessageService;
import com.sprint.mission.discodeit.service.UserService;
import com.sprint.mission.discodeit.service.file.FileUserService;
import com.sprint.mission.discodeit.service.jcf.JCFChannelService;
import com.sprint.mission.discodeit.service.jcf.JCFMessageService;
import com.sprint.mission.discodeit.service.jcf.JCFUserService;

public class AppConfig {

    private final UserService userService;
    private final ChannelService channelService;
    private final MessageService messageService;
    private final UserRepository userRepository;

    public AppConfig() {
        this.userRepository = new FileUserRepository();
        this.userService = new FileUserService(userRepository);
        this.channelService = new JCFChannelService();
        this.messageService = new JCFMessageService(userService, channelService);
    }

    public UserService getUserService() {
        return userService;
    }

    public  ChannelService getChannelService() {
        return channelService;
    }

    public  MessageService getMessageService() {
        return messageService;
    }
}
