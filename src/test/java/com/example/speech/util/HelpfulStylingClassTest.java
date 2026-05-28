package com.example.speech.util;

import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.junit.jupiter.api.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import static org.assertj.core.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class HelpfulStylingClassTest {

    @BeforeAll
    static void initJavaFX() {
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            // already started
        }
    }

    // ==================== setStyleSheets ====================
    @Test
    void testSetStyleSheets_addsCss() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Parent root = new VBox();
        Platform.runLater(() -> {
            HelpfulStylingClass.setStyleSheets(root);
            assertThat(root.getStylesheets()).hasSize(1);
            assertThat(root.getStylesheets().get(0)).contains("/com/example/speech/styles.css");
            latch.countDown();
        });
        assertThat(latch.await(5, TimeUnit.SECONDS)).isTrue();
    }

    @Test
    void testSetStyleSheets_multipleParents() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Parent root1 = new VBox();
        Parent root2 = new VBox();
        Platform.runLater(() -> {
            HelpfulStylingClass.setStyleSheets(root1, root2);
            assertThat(root1.getStylesheets()).hasSize(1);
            assertThat(root2.getStylesheets()).hasSize(1);
            latch.countDown();
        });
        assertThat(latch.await(5, TimeUnit.SECONDS)).isTrue();
    }

    // ==================== setRedEndChar ====================
    @Test
    void testSetRedEndChar_normalLabel() {
        Label label = new Label("Hello");
        HelpfulStylingClass.setRedEndChar(label);
        String style = label.getStyle();
        assertThat(style).contains("linear-gradient");
        assertThat(style).contains("80.0%"); // (5-1)/5 = 0.8 -> 80%
    }

    @Test
    void testSetRedEndChar_singleCharLabel() {
        Label label = new Label("A");
        HelpfulStylingClass.setRedEndChar(label);
        String style = label.getStyle();
        assertThat(style).contains("0%"); // (1-1)/1 = 0 -> 0%
    }

    @Test
    void testSetRedEndChar_emptyLabel_doesNothing() {
        Label label = new Label("");
        HelpfulStylingClass.setRedEndChar(label);
        assertThat(label.getStyle()).isNullOrEmpty();
    }

    @Test
    void testSetRedEndChar_nullLabel_doesNotThrow() {
        assertThatCode(() -> HelpfulStylingClass.setRedEndChar((Label) null))
                .doesNotThrowAnyException();
    }

    // ==================== setupFullScreenListener ====================
    @Test
    void testSetupFullScreenListener_initialPadding() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            Stage stage = new Stage();
            Pane root = new Pane();
            HelpfulStylingClass.setupFullScreenListener(stage, root);
            // По умолчанию окно не full screen, padding = 13
            assertThat(root.getPadding().getTop()).isEqualTo(13);
            assertThat(root.getPadding().getRight()).isEqualTo(13);
            assertThat(root.getPadding().getBottom()).isEqualTo(13);
            assertThat(root.getPadding().getLeft()).isEqualTo(13);
            latch.countDown();
        });
        assertThat(latch.await(5, TimeUnit.SECONDS)).isTrue();
    }

    // Тестирование реакции на fullScreen требует реального отображения окна,
    // поэтому пропускаем в автоматических тестах. Вручную проверено.

    // ==================== applyPromptWithTF ====================
    @Test
    void testApplyPromptWithTF_setsTextFormatter() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            TextField tf = new TextField();
            tf.setPromptText("Enter text");
            HelpfulStylingClass.applyPromptWithTF(tf);
            assertThat(tf.getTextFormatter()).isNotNull();
            latch.countDown();
        });
        assertThat(latch.await(5, TimeUnit.SECONDS)).isTrue();
    }

    @Test
    void testApplyPromptWithTF_nullPrompt_doesNotThrow() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            TextField tf = new TextField();
            HelpfulStylingClass.applyPromptWithTF(tf);
            assertThat(tf.getTextFormatter()).isNotNull();
            latch.countDown();
        });
        assertThat(latch.await(5, TimeUnit.SECONDS)).isTrue();
    }

    // Детальная проверка логики работы с текстом (замена, цвет) сложна,
    // требует эмуляции ввода. В дипломе можно отметить ручное тестирование.

    // ==================== scrollPaneAnimation ====================
    @Test
    void testScrollPaneAnimation_nullNode_doesNotThrow() {
        assertThatCode(() -> HelpfulStylingClass.scrollPaneAnimation(null))
                .doesNotThrowAnyException();
    }

    @Test
    void testScrollPaneAnimation_withScrollPane_noException() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        ScrollPane sp = new ScrollPane();
        Platform.runLater(() -> {
            HelpfulStylingClass.scrollPaneAnimation(sp);
            // lookup не найдёт ScrollBar, но метод просто ничего не сделает
            latch.countDown();
        });
        assertThat(latch.await(5, TimeUnit.SECONDS)).isTrue();
    }

    @Test
    void testScrollPaneAnimation_horizontal_noException() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        ScrollPane sp = new ScrollPane();
        Platform.runLater(() -> {
            HelpfulStylingClass.scrollPaneAnimation(sp, true);
            latch.countDown();
        });
        assertThat(latch.await(5, TimeUnit.SECONDS)).isTrue();
    }
}