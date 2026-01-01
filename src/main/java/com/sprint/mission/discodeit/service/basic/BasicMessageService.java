package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.DTO.request.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.DTO.request.MessageCreateRequest;
import com.sprint.mission.discodeit.DTO.response.MessageResponse;
import com.sprint.mission.discodeit.DTO.request.MessageUpdateRequest;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class BasicMessageService implements MessageService {
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final ChannelRepository channelRepository;
    private final BinaryContentRepository binaryContentRepository;


    @Override
    public MessageResponse create(MessageCreateRequest request, List<BinaryContentCreateRequest> binaryRequests) {
        UUID userId =  request.userId();
        UUID channelId = request.channelId();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("해당하는 유저를 찾을 수 없습니다."));
        Channel channel = channelRepository.findById(channelId)
                .orElseThrow(() -> new NoSuchElementException("해당하는 채널을 찾을 수 없습니다."));

        List<BinaryContentCreateRequest> attachments = (binaryRequests != null) ? binaryRequests : Collections.emptyList();

        List<UUID> attachmentIds = attachments.stream()
                .map(attachmentRequest -> {
                    String originalFileName = attachmentRequest.originalFileName();
                    String savedName = attachmentRequest.savedName();
                    String uploadPath = attachmentRequest.uploadPath();
                    String contentType = attachmentRequest.contentType();
                    byte[] bytes = attachmentRequest.bytes();
                    String description = attachmentRequest.description();

                    BinaryContent binaryContent = new BinaryContent(originalFileName, savedName, uploadPath, contentType, bytes, description);
                    BinaryContent createdBinaryContent = binaryContentRepository.save(binaryContent);
                    return createdBinaryContent.getId();
                })
                .toList();

        String channelName = Optional.ofNullable(channel.getName())
                .orElse("PRIVATE");

        Message message = new Message(
                request.userId(),
                request.channelId(),
                channelName,
                user.getName(),
                request.content(),
                attachmentIds
        );

        Message savedMessage = messageRepository.save(message);

        return MessageResponse.from(savedMessage);
    }


    @Override
    public MessageResponse findById(UUID id) {
        Message message = messageRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("해당하는 메시지를 찾을 수 없습니다."));

        return MessageResponse.from(message);
    }

    @Override
    public List<MessageResponse> findallByChannelId(UUID id) {
        List<Message> messages = messageRepository.findAll();

        return messages.stream()
                .filter(message-> message.getChannelId().equals(id))
                .map(MessageResponse::from)
                .toList();
    }

    @Override
    public Map<String, List<Message>> findMessagesByFrom() {
        return messageRepository.findAll().stream()
                .collect(
                        Collectors.groupingBy(
                                Message::getFrom,
                                Collectors.toList()
                ));
    }

    @Override
    public MessageResponse update(UUID id, MessageUpdateRequest request, List<BinaryContentCreateRequest> binaryRequests) {
        Message message = messageRepository.findById(id)
                .orElseThrow(() ->  new NoSuchElementException("수정하려는 메시지가 없습니다."));

        List<BinaryContentCreateRequest> attachments = (binaryRequests != null) ? binaryRequests : Collections.emptyList();

        List<UUID> attachmentIds = attachments.stream()
                .map(attachmentRequest -> {
                    String originalFileName = attachmentRequest.originalFileName();
                    String savedName = attachmentRequest.savedName();
                    String uploadPath = attachmentRequest.uploadPath();
                    String contentType = attachmentRequest.contentType();
                    byte[] bytes = attachmentRequest.bytes();
                    String description = attachmentRequest.description();

                    BinaryContent binaryContent = new BinaryContent(originalFileName, savedName, uploadPath, contentType, bytes, description);
                    BinaryContent createdBinaryContent = binaryContentRepository.save(binaryContent);
                    return createdBinaryContent.getId();
                })
                .toList();

        message.update(request.content());

        return MessageResponse.from(message);
    }

    @Override
    public void delete(UUID id) {
        Message message = messageRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("삭제하려는 메시지가 없습니다."));

        if(message.getAttachmentIds() != null) {
            message.getAttachmentIds().forEach(binaryContentRepository::delete);
        }
        messageRepository.delete(id);
    }
}
