package com.fakecompanydetector.controller;

import com.fakecompanydetector.service.file.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final FileStorageService fileStorageService;

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "File is empty"));
        }

        if (file.getSize() > 5 * 1024 * 1024) { // 5MB
            return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                    .body(Map.of("error", "File exceeds maximum size of 5MB"));
        }

        try {
            if (!isValidImage(file)) {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid file type. Only PNG, JPG, JPEG, and WEBP are allowed."));
            }

            String fileUrl = fileStorageService.storeFile(file);
            return ResponseEntity.ok(Map.of("url", fileUrl));

        } catch (IOException e) {
            log.error("Failed to store file", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Could not store file. Please try again."));
        }
    }

    @GetMapping("/{fileName:.+}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileName) {
        try {
            Path filePath = Paths.get(uploadDir).toAbsolutePath().normalize().resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists()) {
                String contentType = "application/octet-stream";
                if (fileName.toLowerCase().endsWith(".png")) contentType = "image/png";
                else if (fileName.toLowerCase().endsWith(".jpg") || fileName.toLowerCase().endsWith(".jpeg")) contentType = "image/jpeg";
                else if (fileName.toLowerCase().endsWith(".webp")) contentType = "image/webp";

                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception ex) {
            return ResponseEntity.notFound().build();
        }
    }

    private boolean isValidImage(MultipartFile file) throws IOException {
        byte[] magicBytes = new byte[12];
        try (InputStream is = file.getInputStream()) {
            if (is.read(magicBytes) < 4) {
                return false;
            }
        }
        
        // PNG: 89 50 4E 47
        if (magicBytes[0] == (byte) 0x89 && magicBytes[1] == (byte) 0x50 && magicBytes[2] == (byte) 0x4E && magicBytes[3] == (byte) 0x47) {
            return true;
        }
        
        // JPEG: FF D8 FF
        if (magicBytes[0] == (byte) 0xFF && magicBytes[1] == (byte) 0xD8 && magicBytes[2] == (byte) 0xFF) {
            return true;
        }

        // WEBP: RIFF ... WEBP (bytes 0-3 are RIFF, 8-11 are WEBP)
        if (magicBytes[0] == 'R' && magicBytes[1] == 'I' && magicBytes[2] == 'F' && magicBytes[3] == 'F' &&
            magicBytes[8] == 'W' && magicBytes[9] == 'E' && magicBytes[10] == 'B' && magicBytes[11] == 'P') {
            return true;
        }

        return false;
    }
}
