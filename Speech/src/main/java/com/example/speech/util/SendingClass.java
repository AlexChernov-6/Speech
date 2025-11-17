package com.example.speech.util;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.swing.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

public class SendingClass {
    private static final String EMAIL_ADDRESS;
    private static final String EMAIL_PASSWORD;

    private static final Properties PROPERTIES;

    private static final Session SESSION;

    //Потокобезопасная реализация Map, нужна что бы данные корректно записывались при единовременном обращении
    private static ConcurrentHashMap<String, String> verificationCodes = new ConcurrentHashMap<>();

    public enum ContextDelivery {
        SEND_CONFIRMATION_CODE,
        SEND_LOST_PASSWORD
    }

    static {
        PROPERTIES = new Properties();

        PROPERTIES.put("mail.smtp.auth", "true");
        PROPERTIES.put("mail.smtp.starttls.enable", "true");
        PROPERTIES.put("mail.smtp.host", "smtp.gmail.com");
        PROPERTIES.put("mail.smtp.port", "587");

        try (FileInputStream fileInputStream = new FileInputStream("config.properties")) {
            PROPERTIES.load(fileInputStream);

            EMAIL_ADDRESS = PROPERTIES.getProperty("email.emailAddress");
            EMAIL_PASSWORD = PROPERTIES.getProperty("email.emailPassword");

            if (EMAIL_ADDRESS == null || EMAIL_PASSWORD == null) {
                throw new RuntimeException("Email credentials not found in config.properties");
            }

        } catch (IOException e) {
            throw new RuntimeException("Failed to load email configuration", e);
        }

        // Получаем сессию с аутентификацией
        SESSION = Session.getInstance(PROPERTIES, new javax.mail.Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(EMAIL_ADDRESS, EMAIL_PASSWORD);
            }
        });
    }

    public static void sendPostalDelivery(String recipientEmail, ContextDelivery context) {
        switch (context) {
            case SEND_CONFIRMATION_CODE:
                ConfirmationEmail(recipientEmail);
                break;
            case SEND_LOST_PASSWORD:

                break;
            default:
                throw new IllegalArgumentException("Неизвестный контекст: " + context);
        }
    }

    private static void ConfirmationEmail(String recipientEmail) {
        try {
            // Создаем сообщение
            Message message = new MimeMessage(SESSION);
            message.setFrom(new InternetAddress(EMAIL_ADDRESS, "Speech"));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
            message.setSubject("Код подтверждения регистрации");
            String code = generateVerificationCode();

            message.setText(String.format(
                            "Здравствуйте!\n\n" +
                            "Ваш код подтверждения: %s\n\n" +
                            "Если вы не запрашивали этот код, проигнорируйте это письмо.\n\n" +
                            "С уважением,\nКоманда Speech Application", code));

            // Отправляем сообщение
            Transport.send(message);

            verificationCodes.put(recipientEmail, code);

        } catch (MessagingException | UnsupportedEncodingException e) {
            System.err.println(e.getMessage());
        }
    }

    // Генерация 6-значного кода
    private static String generateVerificationCode() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code);
    }
}
