package com.sprint.mission.discodeit.config;

import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.file.FileChannelRepository;
import com.sprint.mission.discodeit.repository.file.FileMessageRepository;
import com.sprint.mission.discodeit.repository.file.FileUserRepository;
import com.sprint.mission.discodeit.service.ChannelService;
import com.sprint.mission.discodeit.service.MessageService;
import com.sprint.mission.discodeit.service.UserService;
import com.sprint.mission.discodeit.service.file.FileChannelService;
import com.sprint.mission.discodeit.service.file.FileMessageService;
import com.sprint.mission.discodeit.service.file.FileUserService;
import com.sprint.mission.discodeit.service.jcf.JCFChannelService;
import com.sprint.mission.discodeit.service.jcf.JCFMessageService;
import com.sprint.mission.discodeit.service.jcf.JCFUserService;

public class AppConfig {

    private final UserService userService;
    private final ChannelService channelService;
    private final MessageService messageService;

    private final UserRepository userRepository;
    private final ChannelRepository channelRepository;
    private final MessageRepository messageRepository;

    // FILE 기반 사용
//    private static final StorageMode MODE = StorageMode.FILE;
    // JCF 기반 사용
    private static final StorageMode MODE = StorageMode.JCF;

    public AppConfig() {

        switch (MODE) {
            case FILE -> {
                // Repository
                this.userRepository = new FileUserRepository();
                this.channelRepository = new FileChannelRepository();
                this.messageRepository = new FileMessageRepository();

                // Service
                this.userService = new FileUserService(userRepository);
                this.channelService = new FileChannelService(channelRepository);
                this.messageService = new FileMessageService(userService, channelService, messageRepository);
            }

            case JCF -> {
                // JCF는 내부 Map을 가지고 있기에 Repository가 존재 하지 않아도 된다.
                this.userRepository = null;
                this.channelRepository = null;
                this.messageRepository = null;

                // Service
                this.userService = new JCFUserService();
                this.channelService = new JCFChannelService();
                this.messageService = new JCFMessageService(userService, channelService);
            }
            default -> throw new IllegalArgumentException("존재하지 않는 모드입니다.");
        }
    }

    public UserService getUserService() { return userService; }
    public ChannelService getChannelService() { return channelService; }
    public MessageService getMessageService() { return messageService; }

    // JCF 모드에선 아래 3개는 null일 것이다.
    public UserRepository getUserRepository() { return userRepository; }
    public ChannelRepository getChannelRepository() { return channelRepository; }
    public MessageRepository getMessageRepository() { return messageRepository; }
}
