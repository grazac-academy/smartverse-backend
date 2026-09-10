package org.smartvert.smartvert.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface FileStorageService {

    UploadResult upload(MultipartFile file, String folder) throws IOException;

    void delete(String publicId) throws IOException;

    record UploadResult(
            String publicId,
            String url
    ) {
    }
}
