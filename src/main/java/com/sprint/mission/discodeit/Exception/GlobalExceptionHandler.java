package com.sprint.mission.discodeit.Exception;

import com.sprint.mission.discodeit.DTO.response.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.File;
import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.NoSuchElementException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    // 커스텀 예외 start
    // 비밀번호 불일치 -> 401 Unauthorized
    @ExceptionHandler(InvalidPasswordException.class)
    public ResponseEntity<ErrorResponse> handleInvalidPasswordException(InvalidPasswordException e) {
        log.warn("Invalid Password: {}", e.getMessage());
        ErrorResponse response = new ErrorResponse("AUTH_INVALID_PW", e.getMessage());
        return new ResponseEntity<>(new ErrorResponse("Auth_001", e.getMessage()), HttpStatus.UNAUTHORIZED);
    }

    // 이미 로그인 상태 -> 409 Conflict
    @ExceptionHandler(AlreadyLoggedInException.class)
    public ResponseEntity<ErrorResponse> handleAlreadyLoggedInException(AlreadyLoggedInException e) {
        log.warn("Already Logged In: {}", e.getMessage());
        ErrorResponse response = new ErrorResponse("Auth_ALREADY_LOGIN", e.getMessage());
        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    // 이미 로그아웃 상태 -> 409 Conflict
    @ExceptionHandler(AlreadyLoggedOutException.class)
    public ResponseEntity<ErrorResponse> handleAlreadyLoggedOutException(AlreadyLoggedOutException e) {
        log.warn("Already Logged Out: {}", e.getMessage());
        ErrorResponse response = new ErrorResponse("Auth_ALREADY_LOGOUT", e.getMessage());
        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    // 채널 업데이트 불가 -> 403 Forbidden
    @ExceptionHandler(ChannelUpdateNotAllowedException.class)
    public ResponseEntity<ErrorResponse> handleChannelUpdateNotAllowedException(ChannelUpdateNotAllowedException e) {
        log.warn("Channel Update Not Allowed: {}", e.getMessage());
        ErrorResponse response = new ErrorResponse("Auth_CHANNEL_UPDATE_NOT_ALLOWED", e.getMessage());
        return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
    }

    // 읽음 상태(데이터) 없음 -> 404 Not Found
    @ExceptionHandler(NotExistReadStatusException.class)
    public  ResponseEntity<ErrorResponse> handleNotExistReadStatusException(NotExistReadStatusException e) {
        log.warn("Not Exist Read Status: {}", e.getMessage());
        ErrorResponse response = new ErrorResponse("STAUS_NOT_FOUND", e.getMessage());
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(FileUploadException.class)
    public ResponseEntity<ErrorResponse> handleFileUploadException(FileUploadException e) {
        log.warn("File not uploaded: {}", e.getMessage());

        List<File> filesToDelete = e.getUploadedFiles();
        if (filesToDelete != null) {
            for(File file: filesToDelete) {
                if (file.exists() && file.delete()) {
                    log.info("complete delete file caused by failed uploading: {}", file.getName());
                }
            }
        }
        ErrorResponse response = new ErrorResponse("FILE_NOT_UPLOADED", e.getMessage());

        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // 커스텀 예외 end

    // 자바 표준 예외 start
    // 데이터를 찾을 수 없음 (Optional.get()) - > 404 Error
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ErrorResponse> handleNoSuchElementException(NoSuchElementException e) {
        log.warn("Resoucre Not Found: {}", e.getMessage());
        ErrorResponse response = new ErrorResponse("RESOURCE_NOT_FOUND", e.getMessage());
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    //잘못된 인자 값 (null, 잘못된 포맷) -> 400 Bad Request
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handlerIllegalArgument(IllegalArgumentException e) {
        log.warn("Illegal Argument: {}", e.getMessage());
        ErrorResponse response = new ErrorResponse("Bad Request", "잘못된 접근입니다.");
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    //객체 상태 오류 (로직 흐름상 불가능한 상태) -> 409 Conflict
    @ExceptionHandler(IllegalStateException.class)
    public  ResponseEntity<ErrorResponse> handlerIllegalStateException(IllegalStateException e) {
        log.warn("Illegal State: {}", e.getMessage());
        ErrorResponse response = new ErrorResponse("INVALID_STATE", e.getMessage());
        return new  ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    //Spring Security 예외
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handlerAccessDenied(AccessDeniedException e) {
        log.warn("Access Denied: {}", e.getMessage());
        ErrorResponse response = new ErrorResponse("FORBIDDEN", "접근 권한이 없습니다.");
        return new  ResponseEntity<>(response, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(IOException.class)
    public ResponseEntity<ErrorResponse> handleIOException(IOException e) {
        log.error("File Upload Error: ", e);
        ErrorResponse response = new ErrorResponse("FILE_UPLOAD_ERROR", "파일 업로드 중 오류가 발생했습니다.");
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handlerException(Exception e) {
        log.warn("Unhandled Exception: ", e);
        ErrorResponse response = new ErrorResponse("INTERNAL_SERVER_ERROR", "서버 오류 발생");
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    // 자바 표준 예외 end
}
