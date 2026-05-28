package com.example.speech.util;

import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import static org.assertj.core.api.Assertions.*;

class InputControlMaskFormatterTest {

    @BeforeAll
    static void initJavaFX() {
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException e) { /* already started */ }
    }

    @Test
    void testFormatterIsSetForDate() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            TextField tf = new TextField();
            Label hint = new Label();
            tf.setUserData(hint);
            InputControlMaskFormatter formatter = new InputControlMaskFormatter();
            formatter.apply(tf, InputControlMaskFormatter.MaskContext.DATE_MASK);
            assertThat(tf.getTextFormatter()).isNotNull();
            latch.countDown();
        });
        assertThat(latch.await(2, TimeUnit.SECONDS)).isTrue();
    }

    @Test
    void testFormatterIsSetForPhone() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            TextField tf = new TextField();
            Label hint = new Label();
            tf.setUserData(hint);
            InputControlMaskFormatter formatter = new InputControlMaskFormatter();
            formatter.apply(tf, InputControlMaskFormatter.MaskContext.PHONE_MASK);
            assertThat(tf.getTextFormatter()).isNotNull();
            latch.countDown();
        });
        assertThat(latch.await(2, TimeUnit.SECONDS)).isTrue();
    }

    // Полная проверка маски требует эмуляции ввода. В дипломной работе это тестируется вручную.
}