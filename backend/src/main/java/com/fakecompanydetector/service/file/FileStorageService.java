package com.fakecompanydetector.service.file;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface FileStorageService {
    /**
     * Stores a file and returns its public URL.
     *
     * @param file the file to store
     * @return the public URL of the stored file
     * @throws IOException if storage fails
     */
    String storeFile(MultipartFile file) throws IOException;
}
