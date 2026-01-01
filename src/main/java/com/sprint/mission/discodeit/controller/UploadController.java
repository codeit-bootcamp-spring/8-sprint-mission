package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.DTO.response.BinaryContentResponse;
import com.sprint.mission.discodeit.FileUploadUtils;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.service.BinaryContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/upload")
public class UploadController {
    private final BinaryContentService binaryContentService;

    private final FileUploadUtils fileUploadUtils;

    @RequestMapping(value="/single-file", method= RequestMethod.GET)
    public ResponseEntity<String> singleFileUpload(@RequestParam MultipartFile singleFile,
                                                   @RequestParam(value = "singleFileDescription", required = false) String singleFileDescription) {
        try {
            String uploadPath = fileUploadUtils.getUploadPath("images");
            String originalFileName = singleFile.getOriginalFilename();
            String contentType = singleFile.getContentType();
            byte[] bytes = singleFile.getBytes();
            String ext = "";
            if (originalFileName != null && originalFileName.contains(".")) {
                //확장자 추출
                ext = originalFileName.substring(originalFileName.lastIndexOf("."));
            }
            //Random으로 UUID 생성 후 -가 존재하는 경우 삭제 후 확장자와 결합
            String savedName = UUID.randomUUID().toString().replace("-", "") + ext;

            //saveName 이름과 uploadPath 경로를 가진 File객체 생성
            File targetFile = new File(uploadPath, savedName);

            //저장
            singleFile.transferTo(targetFile);

            System.out.println("최종 저장 경로: " + targetFile.getAbsolutePath());

            return ResponseEntity.ok("파일 업로드 성공. 저장된 이름: " + savedName);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body("파일 업로드 실패: " + e.getMessage());
        }
    }

    @RequestMapping(value = "/multi-file", method = RequestMethod.GET)
    public ResponseEntity<String> multifleUpload(@RequestParam List<MultipartFile> multiFiles,
                                                 @RequestParam(value = "multiFileDescription") String multiFileDescription) {
        List<BinaryContentResponse> files = new ArrayList<>();
        List<String> savedFileNames = new ArrayList<>();

        String uploadPath = fileUploadUtils.getUploadPath("images");

        try {
            for(MultipartFile file : multiFiles) {
                String originalFileName = file.getOriginalFilename();
                String contentType = file.getContentType();
                byte[] bytes = file.getBytes();
                String ext = originalFileName.substring(originalFileName.lastIndexOf("."));
                String savedName = UUID.randomUUID().toString().replace("-", "") + ext;
                file.transferTo(new File(uploadPath, savedName));
                files.add(new BinaryContentResponse(originalFileName, savedName, uploadPath, contentType, bytes, multiFileDescription, Instant.now()));
                savedFileNames.add(savedName);
            }

            return ResponseEntity.ok("파일 업로드 성공. 저장된 이름: " + savedFileNames);
        } catch (IOException e) {
            for (BinaryContentResponse response : files) {
                File deleteFile = new File(response.uploadPath(), response.savedName());
                if (deleteFile.exists()) {
                    deleteFile.delete();
                }
            }
            return ResponseEntity.status(500)
                    .body("파일 업로드 실패: " + e.getMessage());
        }

    }
    @RequestMapping(value = "find", method = RequestMethod.GET)
    public ResponseEntity<BinaryContentResponse> find(@RequestParam(value = "id") UUID binaryContentId) {
        BinaryContentResponse binaryContent = binaryContentService.find(binaryContentId);
        return ResponseEntity.ok(binaryContent);
    }
}
