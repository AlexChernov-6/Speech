package com.example.speech.util;

import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

public final class FileUtils {
    public static Path DEFAULT_STORAGE_DIR = Paths.get(System.getProperty("user.dir"), "files");
    private static final AtomicLong totalBytes = new AtomicLong(0);

    private static final ScheduledExecutorService scheduler =
            Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "FileUtils-progress");
                t.setDaemon(true);
                return t;
            });

    private FileUtils() {}

    public static byte[] readFileToByteArrayStream(File file) throws IOException {
        return Files.readAllBytes(file.toPath());
    }

    public static Path saveToDefaultDir(String fileName, byte[] data) throws IOException {
        Path dir = DEFAULT_STORAGE_DIR;
        Files.createDirectories(dir);
        Path filePath = dir.resolve(fileName);

        if (Files.exists(filePath))
            return filePath;

        Files.write(filePath, data, StandardOpenOption.CREATE_NEW);
        return filePath;
    }

    public static SaveResult saveToDefaultDirAsync(String fileName, java.util.function.Supplier<byte[]> dataSupplier) {
        SaveResult result = new SaveResult(fileName, dataSupplier);
        result.start();
        return result;
    }

    public static void copyFilesToDir(List<File> files) throws IOException {
        // Убедимся, что целевая папка существует
        Files.createDirectories(DEFAULT_STORAGE_DIR);

        for (File file : files) {
            if (file == null || !file.exists()) continue;

            Path targetFile = DEFAULT_STORAGE_DIR.resolve(file.getName());
            try {
                Files.copy(file.toPath(), targetFile, StandardCopyOption.REPLACE_EXISTING);
            } catch (FileSystemException ignore) { }
        }
    }

    public static File getFileFromDefaultDir(String fileName) {
        Path filePath = DEFAULT_STORAGE_DIR.resolve(fileName);

        if (Files.exists(filePath))
            return filePath.toFile();

        return null;
    }

    public static SaveResult saveToDefaultDirAsync(String fileName, byte[] data) {
        SaveResult result = new SaveResult(fileName, data);
        result.start();
        return result;
    }

    public static class SaveResult {
        private final CompletableFuture<Path> future = new CompletableFuture<>();
        private final DoubleProperty progress = new SimpleDoubleProperty(0.0);
        private final String fileName;
        private final Supplier<byte[]> dataSupplier;
        private final AtomicLong bytesWritten = new AtomicLong(0);
        private volatile boolean started = false;
        private volatile boolean cancelled = false;
        private Thread writerThread;
        private Path filePath;

        SaveResult(String fileName, byte[] data) {
            this.fileName = fileName;
            this.dataSupplier = () -> data;
        }

        SaveResult(String fileName, Supplier<byte[]> dataSupplier) {
            this.fileName = fileName;
            this.dataSupplier = dataSupplier;
        }

        public synchronized void start() {
            if (started) return;
            started = true;

            writerThread = new Thread(() -> {
                try {
                    byte[] data = dataSupplier.get();
                    if (cancelled) {
                        future.completeExceptionally(new IOException("Operation cancelled"));
                        return;
                    }

                    Path dir = DEFAULT_STORAGE_DIR;
                    Files.createDirectories(dir);
                    filePath = dir.resolve(fileName);

                    if (Files.exists(filePath)) {
                        Platform.runLater(() -> progress.set(1.0));
                        future.complete(filePath);
                        return;
                    }

                    long total = data.length;
                    totalBytes.set(total);
                    try (FileOutputStream fos = new FileOutputStream(filePath.toFile())) {
                        int offset = 0;
                        int bufSize = 4096;
                        while (offset < total && !cancelled) {
                            int len = Math.toIntExact(Math.min(bufSize, total - offset));
                            fos.write(data, offset, len);
                            offset += len;
                            bytesWritten.addAndGet(len);
                        }
                        if (cancelled) {
                            throw new IOException("Operation cancelled");
                        }
                    }

                    Platform.runLater(() -> progress.set(1.0));
                    future.complete(filePath);

                } catch (Exception e) {
                    if (cancelled) {
                        try {
                            Files.deleteIfExists(filePath);
                        } catch (IOException ignored) {}
                        future.completeExceptionally(new IOException("Operation cancelled", e));
                    } else {
                        future.completeExceptionally(e);
                    }
                }
            }, "file-saver");

            writerThread.setDaemon(true);
            writerThread.start();

            scheduler.scheduleAtFixedRate(() -> {
                if (future.isDone()) return;
                long written = bytesWritten.get();
                long total = totalBytes.get();
                if (total == 0) return;
                double p = (double) written / total;
                Platform.runLater(() -> progress.set(p));
            }, 0, 1, TimeUnit.SECONDS);
        }

        public Path getResultNow() {
            if (future.isDone() && !future.isCancelled() && !future.isCompletedExceptionally()) {
                try {
                    return future.get();
                } catch (Exception e) {
                    return null;
                }
            }
            return null;
        }


        public void cancel() {
            cancelled = true;
            if (writerThread != null) {
                writerThread.interrupt();
            }
        }

        public CompletableFuture<Path> getFuture() {
            return future;
        }

        public DoubleProperty progressProperty() {
            return progress;
        }

        /** Используйте ТОЛЬКО в тестах для подмены корневой директории */
        static void setDefaultStorageDirForTest(Path newDir) {
            try {
                Field field = FileUtils.class.getDeclaredField("DEFAULT_STORAGE_DIR");
                field.setAccessible(true);
                Field modifiersField = Field.class.getDeclaredField("modifiers");
                modifiersField.setAccessible(true);
                modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
                field.set(null, newDir);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}