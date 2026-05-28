package com.example.speech.util;

import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import static org.assertj.core.api.Assertions.*;

class NavigateListenerTest {

    @BeforeAll
    static void initJavaFX() {
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException e) { }
    }

    @Test
    void testEnterPressedOnButton() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            Button btn = new Button("OK");
            VBox parent = new VBox(btn);
            NavigateListener.setEnterPressed(btn);
            final boolean[] fired = {false};
            btn.setOnAction(e -> fired[0] = true);
            // Обработчик должен быть установлен на родителе
            var handler = parent.getOnKeyPressed();
            assertThat(handler).isNotNull();
            // Создаём корректное событие KeyEvent
            KeyEvent enterEvent = new KeyEvent(
                    KeyEvent.KEY_PRESSED, "", "", KeyCode.ENTER,
                    false, false, false, false
            );
            handler.handle(enterEvent);
            assertThat(fired[0]).isTrue();
            latch.countDown();
        });
        assertThat(latch.await(2, TimeUnit.SECONDS)).isTrue();
    }
}