//pr 테스트 찐찐찐막


package com.sprint.mission.discodeit;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.jcf.JCFChannelRepository;
import com.sprint.mission.discodeit.repository.jcf.JCFMessageRepository;
import com.sprint.mission.discodeit.repository.jcf.JCFUserRepository;
import com.sprint.mission.discodeit.service.ChannelService;
import com.sprint.mission.discodeit.service.MessageService;
import com.sprint.mission.discodeit.service.UserService;
import com.sprint.mission.discodeit.service.basic.BasicChannelService;
import com.sprint.mission.discodeit.service.basic.BasicMessageService;
import com.sprint.mission.discodeit.service.basic.BasicUserService;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.NoSuchElementException;


public class JavaApplication {

    /**
     * 팩토리 메서드: JCF Repository를 Basic Service에 주입하여 서비스 인스턴스를 생성합니다.
     * 저장소 구현체(JCF)와 서비스 비즈니스 로직(Basic)을 연결합니다.
     */
    private static UserService getUserService() {
        // UserRepository를 JCF 구현체로 결정
        UserRepository userRepository = JCFUserRepository.getInstance();
        // BasicUserService에 주입
        return new BasicUserService(userRepository);
    }

    private static ChannelService getChannelService() {
        // ChannelRepository를 JCF 구현체로 결정
        ChannelRepository channelRepository = JCFChannelRepository.getInstance();
        // BasicChannelService에 주입
        return new BasicChannelService(channelRepository);
    }

    private static MessageService getMessageService() {
        // MessageService는 3개의 Repository에 의존
        MessageRepository messageRepository = JCFMessageRepository.getInstance();
        UserRepository userRepository = JCFUserRepository.getInstance();
        ChannelRepository channelRepository = JCFChannelRepository.getInstance();

        // BasicMessageService에 3개의 Repository를 주입
        return new BasicMessageService(messageRepository, userRepository, channelRepository);
    }


    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println("     ✅ 2차 미션: BasicService 구조 테스트 시작 ✅    ");
        System.out.println("=================================================");

        // 1. 서비스 인스턴스 획득 (Basic*Service + JCF*Repository 주입)
        UserService userService = getUserService();
        ChannelService channelService = getChannelService();
        MessageService messageService = getMessageService();

        UUID userAId;
        UUID channelXId;
        UUID userBId;

        // --- 1단계: 등록 (Create) 테스트 ---
        System.out.println("\n--- 1. 등록 (Create) 및 구조 확인 ---");

        // User 등록
        User userA = new User("Alice_Basic", "alice@basic.com");
        User userB = new User("Bob_Basic", "bob@basic.com");
        userService.save(userA);
        userService.save(userB);
        userAId = userA.getId();
        userBId = userB.getId();

        // Channel 등록
        Channel channelX = new Channel("Basic Channel", userAId);
        channelService.save(channelX);
        channelXId = channelX.getId();

        // Message 등록 (BasicMessageService의 검증 로직이 Repository를 통해 User/Channel을 찾음)
        Message message1 = new Message("Message from Basic Service.", userAId, channelXId);
        messageService.save(message1);
        System.out.printf("성공: User A, Channel X, Message 1 등록 완료. (저장 로직은 Repository에 위임)\n");


        // --- 2단계: 수정 (Update) 및 조회 (Read) ---
        System.out.println("\n--- 2. 수정 및 조회 테스트 ---");

        // User A 수정 (Service는 Repository에 위임, Repository는 Map에 저장)
        userA.update("Alice_Layered", "layered_alice@basic.com");
        userService.update(userA);

        // 수정 확인
        Optional<User> updatedUserA = userService.findById(userAId);
        updatedUserA.ifPresent(u -> System.out.println("수정 확인 (Layered): " + u.getName()));

        // Stream API를 통한 JCF 데이터 조회 테스트
        List<User> allUsers = userService.findAll();
        long bobCount = allUsers.stream()
                .filter(u -> u.getName().equals("Bob_Basic"))
                .count();
        System.out.println("Stream API 조회 (Bob 수): " + bobCount);


        // --- 3단계: 심화 요구사항 검증 (BasicMessageService의 비즈니스 로직 테스트) ---
        System.out.println("\n--- 3. 심화: BasicService의 의존성 검증 테스트 ---");

        // 3-1. 존재하지 않는 User로 Message 생성 시도 (실패 예상)
        System.out.print("실패 테스트 (존재하지 않는 User): ");
        try {
            Message invalidUserMessage = new Message("Invalid user.", UUID.randomUUID(), channelXId);
            messageService.save(invalidUserMessage);
        } catch (IllegalArgumentException e) {
            // BasicMessageService의 비즈니스 로직이 Repository를 통해 존재 여부를 확인하고 예외 발생
            System.out.println("성공적으로 예외 발생 (비즈니스 로직 OK) - " + e.getMessage());
        }

        // 3-2. User B 삭제 (Service -> Repository)
        userService.delete(userBId);
        System.out.println("User B 삭제 완료.");

        // 3-3. 방금 삭제된 User B로 Message 생성 시도 (실패 예상)
        System.out.print("실패 테스트 (삭제된 User B): ");
        try {
            Message deletedUserMessage = new Message("Deleted user message.", userBId, channelXId);
            messageService.save(deletedUserMessage);
        } catch (IllegalArgumentException e) {
            System.out.println("성공적으로 예외 발생 (검증 로직 OK) - " + e.getMessage());
        }


        // --- 4단계: File Repository 교체 테스트 (유연성 입증) ---
        // BasicService는 Repository 인터페이스에 의존하므로, 구현체만 바꿔도 작동해야 함.
        System.out.println("\n--- 4. Repository 교체 테스트 ---");

        // File Repository 인스턴스를 생성 (JCF와 독립된 데이터 공간 사용)
        UserRepository fileUserRepository = com.sprint.mission.discodeit.repository.file.FileUserRepository.getInstance();
        UserService fileUserService = new BasicUserService(fileUserRepository); // FileRepo 주입

        // 새로운 User 생성 (FileRepo에 저장)
        User fileUser = new User("File_User", "file@io.com");
        fileUserService.save(fileUser);

        // 파일에 저장되었는지 확인
        Optional<User> foundFileUser = fileUserService.findById(fileUser.getId());
        System.out.println("File Repo에 저장 후 조회: " + (foundFileUser.isPresent() ? foundFileUser.get().getName() : "실패"));


        System.out.println("\n=================================================");
        System.out.println("     ✅ 2차 미션: BasicService 구조 테스트 완료 ✅    ");
        System.out.println("=================================================");
    }
}