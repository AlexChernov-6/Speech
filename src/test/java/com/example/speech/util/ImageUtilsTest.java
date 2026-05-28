package com.example.speech.util;

import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Method;
import static org.assertj.core.api.Assertions.*;

class ImageUtilsTest {

    @BeforeAll
    static void initJavaFX() {
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            // already started
        }
    }

    // ==================== round (4 параметра) ====================
    @Test
    void testRound_FourCorners() {
        ImageView view = new ImageView(createTestImage(100, 100));
        view.setFitWidth(100);
        view.setFitHeight(100);
        view.setPreserveRatio(false);

        double tl = 10, tr = 20, br = 30, bl = 40;
        ImageUtils.round(view, tl, tr, br, bl);

        assertThat(view.getClip()).isInstanceOf(Group.class);
        Group clip = (Group) view.getClip();
        assertThat(clip.getChildren()).hasSize(4);

        Rectangle topLeft = (Rectangle) clip.getChildren().get(0);
        Rectangle topRight = (Rectangle) clip.getChildren().get(1);
        Rectangle bottomRight = (Rectangle) clip.getChildren().get(2);
        Rectangle bottomLeft = (Rectangle) clip.getChildren().get(3);

        assertThat(topLeft.getArcWidth()).isEqualTo(tl);
        assertThat(topLeft.getArcHeight()).isEqualTo(tl);
        assertThat(topRight.getArcWidth()).isEqualTo(tr);
        assertThat(topRight.getArcHeight()).isEqualTo(tr);
        assertThat(bottomRight.getArcWidth()).isEqualTo(br);
        assertThat(bottomRight.getArcHeight()).isEqualTo(br);
        assertThat(bottomLeft.getArcWidth()).isEqualTo(bl);
        assertThat(bottomLeft.getArcHeight()).isEqualTo(bl);
    }

    // ==================== round (один радиус) ====================
    @Test
    void testRound_SingleCornerRadius() {
        ImageView view = new ImageView(createTestImage(100, 100));
        view.setFitWidth(100);
        view.setFitHeight(100);
        view.setPreserveRatio(false);

        double radius = 25;
        ImageUtils.round(view, radius);

        assertThat(view.getClip()).isInstanceOf(Group.class);
        Group clip = (Group) view.getClip();
        assertThat(clip.getChildren()).hasSize(4);

        for (Node rectNode : clip.getChildren()) {
            Rectangle rect = (Rectangle) rectNode;
            assertThat(rect.getArcWidth()).isEqualTo(radius);
            assertThat(rect.getArcHeight()).isEqualTo(radius);
        }
    }

    // ==================== setCircularImage ====================
    @Test
    void testSetCircularImage_ValidImage() {
        Image original = createTestImage(200, 150); // 200x150, центр 150x150
        ImageView imageView = new ImageView();
        double size = 80;

        ImageUtils.setCircularImage(imageView, original, size);

        // Проверяем viewport (должен вырезать квадрат 150x150 из центра)
        assertThat(imageView.getViewport()).isNotNull();
        double cropSize = Math.min(original.getWidth(), original.getHeight()); // 150
        double expectedX = (original.getWidth() - cropSize) / 2; // (200-150)/2 = 25
        double expectedY = (original.getHeight() - cropSize) / 2; // (150-150)/2 = 0
        assertThat(imageView.getViewport().getMinX()).isEqualTo(expectedX);
        assertThat(imageView.getViewport().getMinY()).isEqualTo(expectedY);
        assertThat(imageView.getViewport().getWidth()).isEqualTo(cropSize);
        assertThat(imageView.getViewport().getHeight()).isEqualTo(cropSize);

        // Размеры ImageView
        assertThat(imageView.getFitWidth()).isEqualTo(size);
        assertThat(imageView.getFitHeight()).isEqualTo(size);
        assertThat(imageView.isPreserveRatio()).isFalse();

        // Клип – круг
        assertThat(imageView.getClip()).isInstanceOf(Circle.class);
        Circle clip = (Circle) imageView.getClip();
        assertThat(clip.getCenterX()).isEqualTo(size / 2);
        assertThat(clip.getCenterY()).isEqualTo(size / 2);
        assertThat(clip.getRadius()).isEqualTo(size / 2);
    }

    @Test
    void testSetCircularImage_NullImage() {
        ImageView imageView = new ImageView();
        ImageUtils.setCircularImage(imageView, null, 50);
        assertThat(imageView.getImage()).isNull();
        assertThat(imageView.getClip()).isNull();
    }

    // ==================== isAncestor (приватный метод) ====================
    @Test
    void testIsAncestor_Reflection() throws Exception {
        Method method = ImageUtils.class.getDeclaredMethod("isAncestor", Node.class, Node.class);
        method.setAccessible(true);

        // Правильный родитель – Pane (может содержать дочерние узлы)
        javafx.scene.layout.Pane parent = new javafx.scene.layout.Pane();
        ImageView child = new ImageView();
        parent.getChildren().add(child);

        boolean result = (boolean) method.invoke(null, parent, child);
        assertThat(result).isTrue();

        ImageView other = new ImageView();
        result = (boolean) method.invoke(null, parent, other);
        assertThat(result).isFalse();

        result = (boolean) method.invoke(null, null, child);
        assertThat(result).isFalse();
    }

    // ==================== просмотр изображений (viewingImages) не тестируется автоматически ====================
    // В дипломе указывается, что метод протестирован вручную из-за сложности UI.

    // ==================== Вспомогательные методы для генерации изображений ====================
    private Image createTestImage(int width, int height) {
        try {
            BufferedImage buffered = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(buffered, "png", baos);
            byte[] bytes = baos.toByteArray();
            return new Image(new ByteArrayInputStream(bytes));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}