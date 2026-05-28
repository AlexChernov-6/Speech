package com.example.speech.util;

import org.junit.jupiter.api.Test;
import java.lang.reflect.Method;
import static org.assertj.core.api.Assertions.*;

class SendingClassTest {

    @Test
    void testGenerateVerificationCode() throws Exception {
        Method method = SendingClass.class.getDeclaredMethod("generateVerificationCode");
        method.setAccessible(true);
        String code = (String) method.invoke(null);
        assertThat(code).matches("\\d{6}");
    }

    @Test
    void testCanSendEmailInitially() {
        // очищаем map через рефлексию или вызываем cleanup, но проще проверить логику
        assertThat(SendingClass.canSendEmail("test@example.com")).isTrue();
    }

    @Test
    void testCleanupExpiredEntries() throws Exception {
        // через рефлексию кладём запись с устаревшим временем
        var lastSentField = SendingClass.class.getDeclaredField("lastSentTime");
        lastSentField.setAccessible(true);
        var map = (java.util.concurrent.ConcurrentHashMap<String, Long>) lastSentField.get(null);
        map.put("old@example.com", System.currentTimeMillis() - 120_000);
        SendingClass.cleanupExpiredEntries();
        assertThat(map.containsKey("old@example.com")).isFalse();
    }

    @Test
    void testGetRemainingTime() {
        // аналогично через рефлексию вставляем запись
        assertThat(SendingClass.getRemainingTime("nonexistent@ex.com")).isZero();
    }
}