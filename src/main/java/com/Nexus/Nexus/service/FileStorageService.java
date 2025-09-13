package com.Nexus.Nexus.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * File storage service for handling document uploads
 * This is a basic implementation using local file system
 * In production, this should be replaced with cloud storage (AWS S3, Cloudinary, etc.)
 */
@Service
@Slf4j
public class FileStorageService {
    
    @Value("${app.upload.dir:uploads}")
    private String uploadDir;
    
    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;
    
    /**
     * Upload file to storage
     */
    public String uploadFile(MultipartFile file, String directory) throws IOException {
        // Create directory if it doesn't exist
        Path uploadPath = Paths.get(uploadDir, directory);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        
        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null && originalFilename.contains(".") 
            ? originalFilename.substring(originalFilename.lastIndexOf("."))
            : "";
        
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String uniqueFilename = timestamp + "_" + UUID.randomUUID().toString() + extension;
        
        // Save file
        Path filePath = uploadPath.resolve(uniqueFilename);
        Files.copy(file.getInputStream(), filePath);
        
        // Return relative path for storage in database
        String relativePath = directory + "/" + uniqueFilename;
        log.info("File uploaded successfully: {}", relativePath);
        
        return relativePath;
    }
    
    /**
     * Generate thumbnail for supported file types
     * This is a placeholder implementation
     */
    public String generateThumbnail(MultipartFile file, String originalPath) throws IOException {
        // For now, return null as thumbnail generation is complex
        // In production, you would use libraries like ImageIO for images
        // or PDF libraries for PDF thumbnails
        log.info("Thumbnail generation requested for: {}", originalPath);
        return null;
    }
    
    /**
     * Delete file from storage
     */
    public void deleteFile(String filePath) throws IOException {
        if (filePath == null) return;
        
        Path path = Paths.get(uploadDir, filePath);
        if (Files.exists(path)) {
            Files.delete(path);
            log.info("File deleted: {}", filePath);
        }
    }
    
    /**
     * Generate download URL for file
     */
    public String generateDownloadUrl(String filePath) {
        return baseUrl + "/api/documents/download/" + filePath.replace("/", "_");
    }
    
    /**
     * Get file path for download
     */
    public Path getFilePath(String filePath) {
        return Paths.get(uploadDir, filePath);
    }
    
    /**
     * Check if file exists
     */
    public boolean fileExists(String filePath) {
        if (filePath == null) return false;
        return Files.exists(Paths.get(uploadDir, filePath));
    }
}
