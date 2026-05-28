package com.example.speech.util;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import java.io.*;
import java.nio.file.*;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.*;
import static org.awaitility.Awaitility.await;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class FileUtilsTest {

    @TempDir
    static Path tempDir;

    private Path originalDefaultDir;

    @BeforeAll
    static void initJavaFX() {
        try {
            javafx.application.Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            // уже запущен
        }
    }

    @BeforeEach
    void redirectDefaultStorageDir() {
        originalDefaultDir = FileUtils.DEFAULT_STORAGE_DIR;
        FileUtils.DEFAULT_STORAGE_DIR = tempDir;
    }

    @AfterEach
    void restoreDefaultStorageDir() {
        FileUtils.DEFAULT_STORAGE_DIR = originalDefaultDir;
    }

    // ==================== Тесты ====================

    @Test
    void testReadFileToByteArrayStream() throws IOException {
        Path testFile = tempDir.resolve("test.txt");
        byte[] expected = "Hello world".getBytes();
        Files.write(testFile, expected);

        byte[] actual = FileUtils.readFileToByteArrayStream(testFile.toFile());
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void testReadFileToByteArrayStream_FileNotFound() {
        File missingFile = tempDir.resolve("missing.txt").toFile();
        assertThatThrownBy(() -> FileUtils.readFileToByteArrayStream(missingFile))
                .isInstanceOf(IOException.class);
    }

    @Test
    void testSaveToDefaultDir_CreatesNewFile() throws IOException {
        String fileName = "newFile.dat";
        byte[] data = {1, 2, 3, 4, 5};

        Path savedPath = FileUtils.saveToDefaultDir(fileName, data);
        Path expectedPath = tempDir.resolve(fileName);

        assertThat(savedPath).isEqualTo(expectedPath);
        assertThat(Files.exists(expectedPath)).isTrue();
        assertThat(Files.readAllBytes(expectedPath)).isEqualTo(data);
    }

    @Test
    void testSaveToDefaultDir_FileAlreadyExists_ReturnsExisting() throws IOException {
        String fileName = "existing.txt";
        byte[] existingData = "old".getBytes();
        Files.write(tempDir.resolve(fileName), existingData);

        byte[] newData = "new".getBytes();
        Path result = FileUtils.saveToDefaultDir(fileName, newData);

        assertThat(result).isEqualTo(tempDir.resolve(fileName));
        assertThat(Files.readAllBytes(result)).isEqualTo(existingData);
    }

    @Test
    void testCopyFilesToDir() throws IOException {
        Path file1 = tempDir.resolve("src1.txt");
        Path file2 = tempDir.resolve("src2.txt");
        Files.write(file1, "one".getBytes());
        Files.write(file2, "two".getBytes());

        List<File> files = List.of(file1.toFile(), file2.toFile());
        FileUtils.copyFilesToDir(files);

        assertThat(Files.exists(tempDir.resolve("src1.txt"))).isTrue();
        assertThat(Files.exists(tempDir.resolve("src2.txt"))).isTrue();
        assertThat(Files.readString(tempDir.resolve("src1.txt"))).isEqualTo("one");
    }

    @Test
    void testCopyFilesToDir_SkipsMissingFiles() {
        File missing = tempDir.resolve("missing.txt").toFile();
        File valid = tempDir.resolve("valid.txt").toFile();
        assertThatNoException().isThrownBy(() ->
                FileUtils.copyFilesToDir(List.of(missing, valid))
        );
    }

    @Test
    void testGetFileFromDefaultDir_Exists() throws IOException {
        Path existing = tempDir.resolve("present.txt");
        Files.write(existing, "content".getBytes());

        File result = FileUtils.getFileFromDefaultDir("present.txt");
        assertThat(result).exists().hasContent("content");
    }

    @Test
    void testGetFileFromDefaultDir_NotExists() {
        File result = FileUtils.getFileFromDefaultDir("absent.txt");
        assertThat(result).isNull();
    }

    @Test
    void testSaveToDefaultDirAsync_WithByteArray_CompletesSuccessfully() throws Exception {
        String fileName = "async.dat";
        byte[] data = "async data".getBytes();

        FileUtils.SaveResult result = FileUtils.saveToDefaultDirAsync(fileName, data);
        Path path = result.getFuture().get(5, TimeUnit.SECONDS);

        assertThat(path).isEqualTo(tempDir.resolve(fileName));
        assertThat(Files.readAllBytes(path)).isEqualTo(data);
        assertThat(result.getResultNow()).isEqualTo(path);
    }

    @Test
    void testSaveToDefaultDirAsync_WithSupplier_CompletesSuccessfully() throws Exception {
        String fileName = "supplier.dat";
        byte[] data = "from supplier".getBytes();

        FileUtils.SaveResult result = FileUtils.saveToDefaultDirAsync(fileName, () -> data);
        Path path = result.getFuture().get(5, TimeUnit.SECONDS);
        assertThat(Files.readAllBytes(path)).isEqualTo(data);
    }

    @Test
    void testSaveToDefaultDirAsync_FileAlreadyExists_ReturnsImmediately() throws Exception {
        String fileName = "already_there.bin";
        byte[] originalData = "original".getBytes();
        FileUtils.saveToDefaultDir(fileName, originalData);

        FileUtils.SaveResult result = FileUtils.saveToDefaultDirAsync(fileName, "new".getBytes());
        Path path = result.getFuture().get(5, TimeUnit.SECONDS);

        assertThat(path).isEqualTo(tempDir.resolve(fileName));
        assertThat(Files.readString(path)).isEqualTo("original");
        assertThat(result.progressProperty().get()).isEqualTo(1.0);
    }

    @Test
    void testSaveResult_ProgressUpdates() throws Exception {
        byte[] data = new byte[10_000];
        java.util.Arrays.fill(data, (byte) 1);

        FileUtils.SaveResult result = FileUtils.saveToDefaultDirAsync("progress.dat", data);

        await().atMost(2, TimeUnit.SECONDS)
                .until(() -> result.progressProperty().get() > 0.0);

        result.getFuture().get(5, TimeUnit.SECONDS);
        assertThat(result.progressProperty().get()).isEqualTo(1.0);
    }

    @Test
    void testSaveResult_CancelBeforeWrite() throws Exception {
        String fileName = "cancel.dat";

        // Поставщик, который задерживается, чтобы дать время на отмену
        Supplier<byte[]> slowSupplier = () -> {
            try {
                Thread.sleep(200); // даём cancel() время сработать
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return new byte[50_000_000];
        };

        FileUtils.SaveResult result = FileUtils.saveToDefaultDirAsync(fileName, slowSupplier);

        // Ждём, чтобы поток успел запуститься, но ещё не получил данные
        Thread.sleep(50);
        result.cancel();

        assertThatThrownBy(() -> result.getFuture().get(3, TimeUnit.SECONDS))
                .isInstanceOf(java.util.concurrent.ExecutionException.class)
                .hasCauseInstanceOf(IOException.class)
                .hasMessageContaining("Operation cancelled");

        assertThat(Files.exists(tempDir.resolve(fileName))).isFalse();
    }

    @Test
    void testSaveResult_ExceptionInSupplier() {
        FileUtils.SaveResult result = FileUtils.saveToDefaultDirAsync("fail.dat", () -> {
            throw new RuntimeException("supplier failed");
        });

        assertThatThrownBy(() -> result.getFuture().get(2, TimeUnit.SECONDS))
                .hasCauseInstanceOf(RuntimeException.class)
                .hasMessageContaining("supplier failed");
    }

    @Test
    void testSaveResult_GetResultNow_BeforeCompletion() {
        byte[] data = new byte[100_000];
        FileUtils.SaveResult result = FileUtils.saveToDefaultDirAsync("lazy.dat", data);
        assertThat(result.getResultNow()).isNull();
        result.getFuture().join();
        assertThat(result.getResultNow()).isNotNull();
    }
}