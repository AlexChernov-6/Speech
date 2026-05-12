package com.example.speech.util;

import javafx.scene.image.Image;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import org.postgresql.util.PGobject;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;

public class ImageConverter {

    public static Image convertBytesToImage(byte[] imageBytes) {
        if (imageBytes == null) {
            return getDefaultImage();
        }

        if (imageBytes.length < 4) {
            return getDefaultImage();
        }

        String format = detectImageFormat(imageBytes);

        Image image = tryLoadImage(imageBytes, format);

        if (image != null && !image.isError()) {
            return image;
        }

        Image alternativeImage = tryAlternativeLoading(imageBytes);

        if (alternativeImage != null && !alternativeImage.isError()) {
            return alternativeImage;
        }

        return getDefaultImage();
    }

    private static Image tryLoadImage(byte[] imageBytes, String format) {
        try {
            ByteArrayInputStream bis = new ByteArrayInputStream(imageBytes);
            Image image = new Image(bis);

            if (image.isError()) {
                return null;
            }

            if (image.getWidth() <= 0 || image.getHeight() <= 0 ||
                    Double.isNaN(image.getWidth()) || Double.isNaN(image.getHeight())) {
                return null;
            }

            return image;

        } catch (Exception e) {
            return null;
        }
    }

    private static Image tryAlternativeLoading(byte[] imageBytes) {
        try {
            if (imageBytes.length > 2 && imageBytes[0] == '\\' && imageBytes[1] == 'x') {
                String hexStr = new String(imageBytes, 2, imageBytes.length - 2, StandardCharsets.ISO_8859_1);
                byte[] cleanBytes = hexStringToByteArray(hexStr);
                Image image = new Image(new ByteArrayInputStream(cleanBytes));
                if (!image.isError()) return image;
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }

        try {
            String hexStr = bytesToHex(imageBytes);
            byte[] cleanBytes = hexStringToByteArray(hexStr);
            Image image = new Image(new ByteArrayInputStream(cleanBytes));
            if (!image.isError()) return image;
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }

        try {
            String base64Str = new String(imageBytes, StandardCharsets.ISO_8859_1);
            if (base64Str.length() % 4 == 0 && base64Str.matches("[A-Za-z0-9+/=]*")) {
                byte[] decodedBytes = java.util.Base64.getDecoder().decode(base64Str);
                Image image = new Image(new ByteArrayInputStream(decodedBytes));
                if (!image.isError()) return image;
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }

        return null;
    }

    private static String detectImageFormat(byte[] bytes) {
        if (bytes == null || bytes.length < 2) {
            return "Недостаточно данных";
        }

        if (bytes[0] == (byte) 0xFF && bytes[1] == (byte) 0xD8) {
            return "JPEG";
        }

        if (bytes.length >= 8 &&
                bytes[0] == (byte) 0x89 && bytes[1] == 0x50 &&
                bytes[2] == 0x4E && bytes[3] == 0x47 &&
                bytes[4] == 0x0D && bytes[5] == 0x0A &&
                bytes[6] == 0x1A && bytes[7] == 0x0A) {
            return "PNG";
        }

        // GIF
        if (bytes.length >= 3 &&
                bytes[0] == 'G' && bytes[1] == 'I' && bytes[2] == 'F') {
            return "GIF";
        }

        if (bytes.length >= 2 &&
                bytes[0] == 'B' && bytes[1] == 'M') {
            return "BMP";
        }

        if (bytes.length >= 12 &&
                bytes[0] == 'R' && bytes[1] == 'I' &&
                bytes[2] == 'F' && bytes[3] == 'F' &&
                bytes[8] == 'W' && bytes[9] == 'E' &&
                bytes[10] == 'B' && bytes[11] == 'P') {
            return "WEBP";
        }
        return "Неизвестный формат";
    }

    private static byte[] hexStringToByteArray(String hex) {
        try {
            int len = hex.length();

            if (len % 2 != 0) {
                hex = "0" + hex;
                len = hex.length();
            }

            byte[] data = new byte[len / 2];
            for (int i = 0; i < len; i += 2) {
                data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                        + Character.digit(hex.charAt(i + 1), 16));
            }
            return data;
        } catch (Exception e) {
            System.err.println(e.getMessage());
            return new byte[0];
        }
    }


    public static String bytesToHex(byte[] bytes) {
        if (bytes == null) return "null";

        StringBuilder hex = new StringBuilder();
        for (byte b : bytes) {
            hex.append(String.format("%02x", b & 0xFF));
        }
        return hex.toString();
    }

    public static Image getDefaultImage() {
        try {
            String possiblePaths = "/com/example/speech/image/defaultChannel.png";

            return new Image(Objects.requireNonNull(ImageConverter.class.getResourceAsStream(possiblePaths)));
        } catch (Exception e) {
            System.err.println(e.getMessage());
            return null;
        }
    }
}