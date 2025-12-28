package com.example.speech.util;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.swing.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import static com.example.speech.util.HelpfulClass.loadPasswordResetTemplate;

public class SendingClass {
    private static final String EMAIL_ADDRESS;
    private static final String EMAIL_PASSWORD;

    private static final Properties PROPERTIES;

    private static final Session SESSION;

    //Потокобезопасная реализация Map, нужна что бы данные корректно записывались при единовременном обращении
    private static ConcurrentHashMap<String, String> verificationCodes = new ConcurrentHashMap<>();

    // Глобальное хранилище времени последней отправки для каждого email
    private static final ConcurrentHashMap<String, Long> lastSentTime = new ConcurrentHashMap<>();
    private static final long COOLDOWN_MS = 60000;

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

    public static boolean sendPostalDelivery(String recipientEmail, ContextDelivery context) {
        // Проверяем можно ли отправлять
        if (!canSendEmail(recipientEmail)) {
            return false;
        }

        switch (context) {
            case SEND_CONFIRMATION_CODE:
                boolean sentConfirmationCode = ConfirmationEmail(recipientEmail);
                if (sentConfirmationCode) {
                    lastSentTime.put(recipientEmail, System.currentTimeMillis());
                }
                return sentConfirmationCode;
            case SEND_LOST_PASSWORD:
                return sendResetLostPassword(recipientEmail);
            default:
                throw new IllegalArgumentException("Неизвестный контекст: " + context);
        }
    }

    private static boolean ConfirmationEmail(String recipientEmail) {
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
            return true;
        } catch (MessagingException | UnsupportedEncodingException e) {
            System.err.println(e.getMessage());
            return false;
        }
    }

    // Генерация 6-значного кода
    private static String generateVerificationCode() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code);
    }

    // Очистка устаревших записей
    public static void cleanupExpiredEntries() {
        long currentTime = System.currentTimeMillis();
        lastSentTime.entrySet().removeIf(entry ->
                currentTime - entry.getValue() > COOLDOWN_MS
        );
    }

    // Проверяем можно ли отправлять email
    public static boolean canSendEmail(String email) {
        Long lastTime = lastSentTime.get(email);
        if (lastTime == null) return true;

        long timeSinceLastSend = System.currentTimeMillis() - lastTime;
        return timeSinceLastSend >= COOLDOWN_MS;
    }

    // Получаем оставшееся время в секундах
    public static int getRemainingTime(String email) {
        Long lastTime = lastSentTime.get(email);
        if (lastTime == null) return 0;

        long timeSinceLastSend = System.currentTimeMillis() - lastTime;
        long remainingMs = COOLDOWN_MS - timeSinceLastSend;

        return remainingMs > 0 ? (int)(remainingMs / 1000) : 0;
    }

    public static String getVerificationCode(String emil) {
        return verificationCodes.get(emil);
    }

    private static boolean sendResetLostPassword(String recipientEmail) {
        try {
            Message message = new MimeMessage(SESSION);
            message.setFrom(new InternetAddress(EMAIL_ADDRESS, "Speech"));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
            message.setSubject("Восстановление пароля - Speech");

            // Создаем многочастное сообщение для HTML
            MimeMultipart multipart = new MimeMultipart("related");

            // HTML часть
            BodyPart messageBodyPart = new MimeBodyPart();
            String htmlText = loadPasswordResetTemplate(
                    "https://metanit.com/java/javafx/3.2.php", recipientEmail);
            messageBodyPart.setContent(htmlText, "text/html; charset=utf-8");
            multipart.addBodyPart(messageBodyPart);

            message.setContent(multipart);

            // Отправляем сообщение
            Transport.send(message);
            return true;

        } catch (MessagingException | UnsupportedEncodingException e) {
            System.err.println("Ошибка отправки письма восстановления: " + e.getMessage());
            return false;
        } catch (IOException | SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
