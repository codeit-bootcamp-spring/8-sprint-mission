package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.BinaryContentService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/binaryContents")
@RequiredArgsConstructor
public class BinaryContentController {

    private final BinaryContentService binaryContentService;
    private final UserRepository userRepository;
    
    @Data
    static class BinaryContentResponse {
        private UUID id;
        private String fileName;
        private String contentType;
        private Long fileSize;
        private String bytes; // Base64 encoded
    }

    /**
     * 여러 첨부 파일 조회
     * GET /api/binaryContents?binaryContentIds=...
     */
    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<List<BinaryContent>> findAllByIds(@RequestParam List<UUID> binaryContentIds) {
        List<BinaryContent> contents = binaryContentService.findAllByIdIn(binaryContentIds);
        return ResponseEntity.ok(contents);
    }

    /**
     * 첨부 파일 조회
     * GET /api/binaryContents/{binaryContentId}
     * React 앱이 bytes와 contentType을 기대하므로 저장된 데이터를 반환
     */
    @RequestMapping(value = "/{binaryContentId}", method = RequestMethod.GET)
    public ResponseEntity<BinaryContentResponse> findById(@PathVariable UUID binaryContentId) {
        BinaryContent content = binaryContentService.findById(binaryContentId)
                .orElseThrow(() -> new IllegalArgumentException("파일 정보를 찾을 수 없습니다."));

        BinaryContentResponse response = new BinaryContentResponse();
        response.setId(content.getId());
        response.setFileName(content.getFileName());
        response.setContentType(content.getContentType());
        response.setFileSize(content.getFileSize());
        response.setBytes(content.getBytes());
        
        return ResponseEntity.ok(response);
    }

    /**
     * 프로필 이미지 조회 (profileId로 사용자를 찾아서 이미지 반환)
     * GET /api/binaryContents/{binaryContentId}/image
     */
    @RequestMapping(value = "/{binaryContentId}/image", method = RequestMethod.GET, produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<Resource> getProfileImage(@PathVariable UUID binaryContentId) {
        // profileId로 사용자 찾기
        User user = userRepository.findAll().stream()
                .filter(u -> u.getProfileId() != null && u.getProfileId().equals(binaryContentId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("프로필 이미지를 찾을 수 없습니다."));

        // 사용자 이름에 맞는 이미지 파일 선택
        String imageFileName = getImageFileNameForUser(user.getName());
        
        // static/images 폴더에서 이미지 파일 로드
        Resource resource = new ClassPathResource("static/images/" + imageFileName);
        
        if (!resource.exists()) {
            // 기본 이미지 반환
            resource = new ClassPathResource("static/images/default-avatar.png");
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + imageFileName + "\"")
                .body(resource);
    }

    private String getImageFileNameForUser(String userName) {
        if (userName == null) {
            return "default-avatar.png";
        }

        String lowerName = userName.toLowerCase();

        // 이름에 맞는 이미지 파일 매핑
        if (lowerName.contains("buzz")) {
            return "buzz.png";
        } else if (lowerName.contains("jessie") || lowerName.contains("제시")) {
            return "jessie.png";
        } else if (lowerName.contains("rex") || lowerName.contains("렉스")) {
            return "rex.png";
        } else if (lowerName.contains("woody") || lowerName.contains("우디")) {
            return "woody.png";
        } else if (lowerName.contains("현승원")) {
            return "aliens.png";
        }

        // 매칭되지 않으면 기본 이미지
        return "default-avatar.png";
    }
}