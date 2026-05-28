package com.example.speech.util;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import static org.assertj.core.api.Assertions.*;

class ResizeListenerTest {

    @BeforeAll
    static void initJavaFX() {
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException e) { }
    }

    @Test
    void testResizeLogicUpperLeftCorner() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                Stage stage = new Stage();
                stage.setX(100); stage.setY(100);
                stage.setWidth(800); stage.setHeight(600);
                ResizeListener listener = new ResizeListener(stage);

                // Сохраняем начальное состояние
                Method storeMethod = ResizeListener.class.getDeclaredMethod("storeInitialState", MouseEvent.class);
                storeMethod.setAccessible(true);
                MouseEvent press = new MouseEvent(null, null, null, 0,0,0,0, null, 0,
                        false, false, false, false, false, false, false, false, false, false, null);
                storeMethod.invoke(listener, press);

                // Устанавливаем позицию курсора как UPPER_LEFT_CORNER
                Field cursorField = ResizeListener.class.getDeclaredField("cursorPosition");
                cursorField.setAccessible(true);
                Class<?> enumClass = Class.forName("com.example.speech.util.ResizeListener$ScreenFrame");
                Object upperLeft = Enum.valueOf(enumClass.asSubclass(Enum.class), "UPPER_LEFT_CORNER");
                cursorField.set(listener, upperLeft);

                // Эмулируем драг
                Method dragMethod = ResizeListener.class.getDeclaredMethod("handleDrag", MouseEvent.class);
                dragMethod.setAccessible(true);
                MouseEvent drag = new MouseEvent(null, null, null, -50, -30, 0,0, null, 0,
                        false, false, false, false, false, false, false, false, false, false, null);
                dragMethod.invoke(listener, drag);

                // Проверяем, что размеры и позиция изменились
                assertThat(stage.getWidth()).isEqualTo(850);
                assertThat(stage.getHeight()).isEqualTo(630);
                assertThat(stage.getX()).isEqualTo(50);
                assertThat(stage.getY()).isEqualTo(70);
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                latch.countDown();
            }
        });
        assertThat(latch.await(2, TimeUnit.SECONDS)).isTrue();
    }
}