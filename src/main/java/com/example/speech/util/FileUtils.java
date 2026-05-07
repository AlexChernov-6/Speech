package com.example.speech.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public final class FileUtils {
    private static final Path DEFAULT_STORAGE_DIR = Paths.get("files");

    private FileUtils() {}

    public static byte[] readFileToByteArrayStream(File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file);
             ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                bos.write(buffer, 0, bytesRead);
            }
            return bos.toByteArray();
        }
    }

    public static Path saveToDefaultDir(String fileName, byte[] data) throws IOException {
        Path dir = DEFAULT_STORAGE_DIR;
        Files.createDirectories(dir);
        Path filePath = dir.resolve(fileName);
        Files.write(filePath, data,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING);
        return filePath;
    }

    public static void copyFilesToDir(List<File> files) throws IOException {
        // Убедимся, что целевая папка существует
        Files.createDirectories(DEFAULT_STORAGE_DIR);

        for (File file : files) {
            if (file == null || !file.exists()) continue;

            Path targetFile = DEFAULT_STORAGE_DIR.resolve(file.getName());
            Files.copy(file.toPath(), targetFile, StandardCopyOption.REPLACE_EXISTING);
        }
    }
}