package com.sprint.mission.discodeit.config;

import com.sprint.mission.discodeit.service.ChannelService;
import com.sprint.mission.discodeit.service.MessageService;
import com.sprint.mission.discodeit.service.UserService;
import com.sprint.mission.discodeit.service.jcf.JCFChannelService;
import com.sprint.mission.discodeit.service.jcf.JCFMessageService;
import com.sprint.mission.discodeit.service.jcf.JCFUserService;

public class AppConfig {

    private final UserService userService;
    private final ChannelService channelService;
    private final MessageService messageService;

    public AppConfig() {
        this.userService = new JCFUserService();
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
