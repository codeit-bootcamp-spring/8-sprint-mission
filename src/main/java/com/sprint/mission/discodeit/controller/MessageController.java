package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.DTO.request.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.DTO.request.MessageCreateRequest;
import com.sprint.mission.discodeit.DTO.request.MessageUpdateRequest;
import com.sprint.mission.discodeit.DTO.response.BinaryContentResponse;
import com.sprint.mission.discodeit.DTO.response.MessageResponse;
import com.sprint.mission.discodeit.Exception.FileUploadException;
import com.sprint.mission.discodeit.FileUploadUtils;
import com.sprint.mission.discodeit.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/message")
public class MessageController {
    private final MessageService messageService;
    private final FileUploadUtils fileUploadUtils;

    public static List<BinaryContentCreateRequest> saveFiles(List<MultipartFile> files, String uploadPath) {
        List<BinaryContentCreateRequest> binaryRequests = new ArrayList<>();

        // 롤백을 위한 저장된 파일의 경로를 임시로 담아두는 리스트
        List<File> uploadedFiles = new ArrayList<>();

        if (files != null && !files.isEmpty()) {
            try {
                for (MultipartFile file : files) {
                    String originalFileName = file.getOriginalFilename();
                    String contentType = file.getContentType();
                    byte[] bytes = file.getBytes();
                    String ext = (originalFileName != null && originalFileName.contains("."))
                            ? originalFileName.substring(originalFileName.lastIndexOf("."))
                            : "";
                    String savedName = UUID.randomUUID().toString().replace("-", "") + ext;

                    File targetFile = new File(uploadPath, savedName);
                    file.transferTo(targetFile);

                    uploadedFiles.add(targetFile);

                    binaryRequests.add(new BinaryContentCreateRequest(
                            originalFileName,
                            savedName,
                            uploadPath,
                            contentType,
                            bytes,
                            null
                    ));
                }
            } catch (IOException e) {
                throw new FileUploadException("파일 업로드 중 오류가 발생하였습니다.", e, uploadedFiles);
            }
        }
        return binaryRequests;
    }

    @RequestMapping(value = "/create", method = RequestMethod.GET)
    public ResponseEntity<MessageResponse> create(@RequestPart(value = "request") MessageCreateRequest messageCreateRequest,
                                  @RequestPart(value = "files", required = false) List<MultipartFile> files) {
        String uploadPath = fileUploadUtils.getUploadPath("images");
        List<BinaryContentCreateRequest> binaryRequests = files != null && !files.isEmpty() ? saveFiles(files, uploadPath) : List.of();
        return ResponseEntity.ok(messageService.create(messageCreateRequest, binaryRequests));
    }

    @RequestMapping(value = "/find", method = RequestMethod.GET)
    public ResponseEntity<List<MessageResponse>> findAllByChannelId(@RequestParam(value="id") UUID channelId){
        List<MessageResponse> messageResponses = messageService.findallByChannelId(channelId);
        return ResponseEntity.ok(messageResponses);
    }

    @RequestMapping(value = "/update", method = RequestMethod.GET)
    public ResponseEntity<MessageResponse> update(@RequestParam(value="id") UUID id,
                                                  @RequestPart(value = "request") MessageUpdateRequest messageUpdateRequest,
                                                  @RequestPart(value = "files", required = false) List<MultipartFile> files) {
        String uploadPath = fileUploadUtils.getUploadPath("images");
        List<BinaryContentCreateRequest> binaryRequests = files != null && !files.isEmpty() ? saveFiles(files, uploadPath) : List.of();
        MessageResponse messageResponse = messageService.update(id, messageUpdateRequest, binaryRequests);
        return ResponseEntity.ok(messageResponse);
    }

    @RequestMapping(value = "/delete/{id}", method = RequestMethod.GET)
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        messageService.delete(id);
        return ResponseEntity.ok().build();
    }
}
