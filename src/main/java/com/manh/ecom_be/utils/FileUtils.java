package com.manh.ecom_be.utils;

import org.apache.commons.io.FilenameUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.UUID;

public class FileUtils {
    private static String UPLOADS_FOLDER = "uploads";

    public static void deleteFile(String filename) throws IOException {
        java.nio.file.Path uploadDir = Paths.get(UPLOADS_FOLDER);
        java.nio.file.Path filePath = uploadDir.resolve(filename);

        if (Files.exists(filePath)) {
            Files.delete(filePath);
        } else {

        }
    }

    public static boolean isImageFile(MultipartFile file) {
        return true;
    }

    public static String storeFile(MultipartFile file) throws IOException {
        if (!isImageFile(file) || file.getOriginalFilename() == null) {
            throw new IOException("Invalid image format");
        }

        String filename = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        String extension = FilenameUtils.getExtension(filename);

        String uniqueFilename = UUID.randomUUID().toString() + "." + System.nanoTime() + "." + extension;

        java.nio.file.Path uploadDir = Paths.get(UPLOADS_FOLDER);

        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }
        java.nio.file.Path destination = Paths.get(uploadDir.toString(), uniqueFilename);

        Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);

        return uniqueFilename;
    }
}
