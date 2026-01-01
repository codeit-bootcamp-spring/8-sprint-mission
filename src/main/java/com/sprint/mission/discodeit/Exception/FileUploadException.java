package com.sprint.mission.discodeit.Exception;

import java.io.File;
import java.util.List;

public class FileUploadException extends RuntimeException {
    private final List<File> uploadedFiles;
    public FileUploadException(String message, Throwable cause, List<File> uploadedFiles) {
        super(message, cause);
        this.uploadedFiles = uploadedFiles;
    }
    public List<File> getUploadedFiles() {
        return uploadedFiles;
    }
}
