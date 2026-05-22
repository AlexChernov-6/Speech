package com.example.speech.util;

import jakarta.persistence.AttributeConverter;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class CryptoConverter implements AttributeConverter<String, byte[]> {

    private static final String ALGORITHM = "AES/ECB/PKCS5Padding";
    private static final byte[] KEY;

    static {
        // Загружаем ключ из файла resources/key.txt (временно)
        try (InputStream is = new FileInputStream("key.txt")) {
            KEY = is.readAllBytes();
            // Проверим длину ключа (AES поддерживает 16, 24, 32 байта)
            if (KEY.length != 16 && KEY.length != 24 && KEY.length != 32) {
                throw new RuntimeException("Key must be 16, 24, or 32 bytes long");
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read key file", e);
        }
    }

    @Override
    public byte[] convertToDatabaseColumn(String attribute) {
        if (attribute == null) return null;
        try {
            SecretKey secretKey = new SecretKeySpec(KEY, "AES");
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            return cipher.doFinal(attribute.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new RuntimeException("Error encrypting data", e);
        }
    }

    @Override
    public String convertToEntityAttribute(byte[] dbData) {
        if (dbData == null) return null;
        try {
            SecretKey secretKey = new SecretKeySpec(KEY, "AES");
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            return new String(cipher.doFinal(dbData), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Error decrypting data", e);
        }
    }
}