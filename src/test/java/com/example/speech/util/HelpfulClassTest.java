package com.example.speech.util;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import org.junit.jupiter.api.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import static org.assertj.core.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class HelpfulClassTest {

    @BeforeAll
    static void initJavaFX() {
        try { Platform.startup(() -> {}); } catch (IllegalStateException e) {}
    }

    @Test
    void testOpenWebPage_ValidUrl_DoesNotThrow() {
        assertThatCode(() -> HelpfulClass.openWebPage("https://example.com"))
                .doesNotThrowAnyException();
    }

    // ==================== getLocalDate ====================
    @Test
    void testGetLocalDate_ValidInput() {
        String result = HelpfulClass.getLocalDate(15, "Март", 2025);
        assertThat(result).isEqualTo("15.03.2025");
    }

    @Test
    void testGetLocalDate_InvalidMonth_ReturnsNull() {
        String result = HelpfulClass.getLocalDate(1, "InvalidMonth", 2025);
        assertThat(result).isNull();
    }

    @Test
    void testGetLocalDate_LeapYear() {
        String result = HelpfulClass.getLocalDate(29, "Февраль", 2020);
        assertThat(result).isEqualTo("29.02.2020");
    }

    @Test
    void testGetLocalDate_InvalidDay_ReturnsNull() {
        String result = HelpfulClass.getLocalDate(31, "Апрель", 2025);
        assertThat(result).isNull();
    }

    // ==================== loadTemplate ====================
    @Test
    void testLoadTemplate_TemplateExists() throws IOException {
        Map<String, String> vars = new HashMap<>();
        vars.put("userName", "John");
        vars.put("resetLink", "http://example.com/reset");

        String result = HelpfulClass.loadTemplate("test-template.html", vars);
        assertThat(result).isEqualTo("Hello John, your link: http://example.com/reset");
    }

    @Test
    void testLoadTemplate_TemplateNotFound_ThrowsIOException() {
        Map<String, String> vars = new HashMap<>();
        assertThatThrownBy(() -> HelpfulClass.loadTemplate("missing.html", vars))
                .isInstanceOf(IOException.class)
                .hasMessageContaining("Template not found");
    }

    @Test
    void testLoadTemplate_NullVariable_ReplacesWithEmptyString() throws IOException {
        Map<String, String> vars = new HashMap<>();
        vars.put("userName", null);
        vars.put("resetLink", "link");

        String result = HelpfulClass.loadTemplate("test-template.html", vars);
        assertThat(result).isEqualTo("Hello , your link: link");
    }

    // ==================== setImageWithButton ====================
    @Test
    void testSetImageWithButton_DefaultSize() {
        Platform.runLater(() -> {
            Button btn = new Button();
            HelpfulClass.setImageWithButton(btn, "test-icon.png");
            assertThat(btn.getStyleClass()).containsExactly("icon-button");
            assertThat(btn.getPrefHeight()).isEqualTo(40);
            assertThat(btn.getPrefWidth()).isEqualTo(40);
            assertThat(btn.getGraphic()).isNotNull().isInstanceOf(ImageView.class);
            ImageView iv = (ImageView) btn.getGraphic();
            assertThat(iv.getFitHeight()).isEqualTo(20);
            assertThat(iv.getFitWidth()).isEqualTo(20);
            assertThat(btn.getAlignment()).isEqualTo(Pos.CENTER);
        });
        waitForFXEvents();
    }

    @Test
    void testSetImageWithButton_CustomSizeAndStyle() {
        Platform.runLater(() -> {
            Button btn = new Button();
            HelpfulClass.setImageWithButton(btn, "test-icon.png", "custom-style", 60, 60);
            assertThat(btn.getStyleClass()).containsExactly("custom-style");
            assertThat(btn.getPrefHeight()).isEqualTo(60);
            assertThat(btn.getPrefWidth()).isEqualTo(60);
            assertThat(btn.getGraphic()).isNotNull();
            ImageView iv = (ImageView) btn.getGraphic();
            assertThat(iv.getFitHeight()).isEqualTo(30);
            assertThat(iv.getFitWidth()).isEqualTo(30);
            assertThat(btn.getStyle()).contains("-fx-cursor: hand;");
        });
        waitForFXEvents();
    }

    private void waitForFXEvents() {
        try { Thread.sleep(200); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }
}