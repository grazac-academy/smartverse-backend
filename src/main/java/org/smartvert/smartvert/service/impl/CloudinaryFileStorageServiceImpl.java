package org.smartvert.smartvert.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.smartvert.smartvert.service.FileStorageService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CloudinaryFileStorageServiceImpl implements FileStorageService {

    private final Cloudinary cloudinary;

    @Override
    public UploadResult upload(MultipartFile file, String folder) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }

        if (folder == null || folder.isBlank()) {
            throw new IllegalArgumentException("Folder cannot be blank");
        }

        var options = ObjectUtils.asMap("folder", folder, "resource_type", "auto", "unique_filename", true, "overwrite", false);

        Map<?, ?> result = cloudinary.uploader().upload(file.getBytes(), options);
        return new UploadResult((String) result.get("public_id"), (String) result.get("secure_url"));

    }

    @Override
    public void delete(String publicId) throws IOException {

        if (publicId == null || publicId.isBlank()) {
            throw new IllegalArgumentException("Public ID cannot be blank");
        }

        cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());

    }


}
