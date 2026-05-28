package com.example.speech.util;

import javafx.application.Platform;
import javafx.scene.image.Image;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;

import static org.assertj.core.api.Assertions.*;

class ImageConverterTest {

    @BeforeAll
    static void initJavaFX() {
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            // already started
        }
    }

    // ==================== convertBytesToImage ====================
    @Test
    void testConvertBytesToImage_nullReturnsValidImage() {
        Image result = ImageConverter.convertBytesToImage(null);
        assertThat(result).isNotNull();
        assertThat(result.isError()).isFalse();
        assertThat(result.getWidth()).isPositive();
    }

    @Test
    void testConvertBytesToImage_shortArrayReturnsValidImage() {
        byte[] shortArray = {1, 2, 3};
        Image result = ImageConverter.convertBytesToImage(shortArray);
        assertThat(result).isNotNull();
        assertThat(result.isError()).isFalse();
        assertThat(result.getWidth()).isPositive();
    }

    @Test
    void testConvertBytesToImage_validImage() throws Exception {
        // Создаём PNG в памяти 10x10
        BufferedImage bufferedImage = new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB);
        bufferedImage.setRGB(0, 0, 0xFF0000);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(bufferedImage, "png", baos);
        byte[] validBytes = baos.toByteArray();

        Image result = ImageConverter.convertBytesToImage(validBytes);
        assertThat(result).isNotNull();
        assertThat(result.isError()).isFalse();
        assertThat(result.getWidth()).isEqualTo(10);
        assertThat(result.getHeight()).isEqualTo(10);
    }

    @Test
    void testConvertBytesToImage_invalidBytesReturnsValidImage() {
        byte[] invalid = {0, 1, 2, 3, 4, 5};
        Image result = ImageConverter.convertBytesToImage(invalid);
        assertThat(result).isNotNull();
        assertThat(result.isError()).isFalse();
        assertThat(result.getWidth()).isPositive();
    }

    // ==================== detectImageFormat ====================
    @Test
    void testDetectImageFormat_JPEG() {
        byte[] jpeg = {(byte) 0xFF, (byte) 0xD8, 0, 0};
        assertThat(ImageConverter.detectImageFormat(jpeg)).isEqualTo("JPEG");
    }

    @Test
    void testDetectImageFormat_PNG() {
        byte[] png = {
                (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A,
                0, 0, 0, 0, 0, 0, 0, 0
        };
        assertThat(ImageConverter.detectImageFormat(png)).isEqualTo("PNG");
    }

    @Test
    void testDetectImageFormat_GIF() {
        byte[] gif = {'G', 'I', 'F', '8', '9', 'a'};
        assertThat(ImageConverter.detectImageFormat(gif)).isEqualTo("GIF");
    }

    @Test
    void testDetectImageFormat_BMP() {
        byte[] bmp = {'B', 'M'};
        assertThat(ImageConverter.detectImageFormat(bmp)).isEqualTo("BMP");
    }

    @Test
    void testDetectImageFormat_WEBP() {
        byte[] webp = {'R', 'I', 'F', 'F', 0,0,0,0, 'W', 'E', 'B', 'P'};
        assertThat(ImageConverter.detectImageFormat(webp)).isEqualTo("WEBP");
    }

    @Test
    void testDetectImageFormat_Unknown() {
        byte[] unknown = {1,2,3,4,5};
        assertThat(ImageConverter.detectImageFormat(unknown)).isEqualTo("Неизвестный формат");
    }

    @Test
    void testDetectImageFormat_NullOrTooShort() {
        assertThat(ImageConverter.detectImageFormat(null)).isEqualTo("Недостаточно данных");
        assertThat(ImageConverter.detectImageFormat(new byte[1])).isEqualTo("Недостаточно данных");
    }

    // ==================== bytesToHex / hexStringToByteArray ====================
    @Test
    void testBytesToHex_null() {
        assertThat(ImageConverter.bytesToHex(null)).isEqualTo("null");
    }

    @Test
    void testBytesToHex_normal() {
        byte[] data = {0x12, 0x34, (byte) 0xAB, (byte) 0xFF};
        String hex = ImageConverter.bytesToHex(data);
        assertThat(hex).isEqualTo("1234abff");
    }

    @Test
    void testHexStringToByteArray_roundTrip() {
        byte[] original = {0x12, 0x34, (byte) 0xAB, (byte) 0xFF};
        String hex = ImageConverter.bytesToHex(original);
        byte[] restored = ImageConverter.hexStringToByteArray(hex);
        assertThat(restored).isEqualTo(original);
    }

    @Test
    void testHexStringToByteArray_oddLength_padsWithZero() {
        byte[] result = ImageConverter.hexStringToByteArray("abc");
        assertThat(result).hasSize(2);
        assertThat(result[0]).isEqualTo((byte) 0x0A);
        assertThat(result[1]).isEqualTo((byte) 0xBC);
    }

    @Test
    void testHexStringToByteArray_invalidHex_returnsNonEmptyArray() {
        // Метод не умеет обрабатывать невалидные символы, возвращает массив с ошибочными байтами,
        // но не пустой. Тест проверяет, что исключение не выбрасывается.
        byte[] result = ImageConverter.hexStringToByteArray("zz");
        assertThat(result).isNotEmpty();
    }

    // ==================== getDefaultImage ====================
    @Test
    void testGetDefaultImage_returnsValidImage() {
        Image defaultImg = ImageConverter.getDefaultImage();
        assertThat(defaultImg).isNotNull();
        assertThat(defaultImg.isError()).isFalse();
        assertThat(defaultImg.getWidth()).isPositive();
        assertThat(defaultImg.getHeight()).isPositive();
    }
}