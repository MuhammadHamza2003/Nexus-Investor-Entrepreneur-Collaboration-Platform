package com.Nexus.Nexus.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Configuration for file upload handling
 */
@Configuration
@Slf4j
public class FileUploadConfig {
    
    @Value("${app.upload.dir:uploads}")
    private String uploadDir;
    
    @PostConstruct
    public void init() {
        try {
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
                log.info("Created upload directory: {}", uploadPath.toAbsolutePath());
            } else {
                log.info("Upload directory already exists: {}", uploadPath.toAbsolutePath());
            }
        } catch (IOException e) {
            log.error("Could not create upload directory", e);
            throw new RuntimeException("Could not create upload directory", e);
        }
    }
}
