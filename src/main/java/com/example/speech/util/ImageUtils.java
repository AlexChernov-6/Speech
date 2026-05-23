package com.example.speech.util;

import com.example.speech.control.EntranceController;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class ImageUtils {
    public static void round(Node node,
                             double topLeft,
                             double topRight,
                             double bottomRight,
                             double bottomLeft) {

        double addWidth = 30;
        double addHeight = 30;

        double width = node.getLayoutBounds().getWidth() / 2 + addWidth;
        double height = node.getLayoutBounds().getHeight() / 2 + addHeight;

        Rectangle topLeftRect = new Rectangle(0, 0, width, height);
        Rectangle topRightRect = new Rectangle(width - addWidth * 2, 0, width, height);
        Rectangle bottomRightRect = new Rectangle(width - addWidth * 2, height - addHeight * 2, width, height);
        Rectangle bottomLeftRect = new Rectangle(0, height - addHeight * 2, width, height);

        topLeftRect.setArcWidth(topLeft);
        topLeftRect.setArcHeight(topLeft);
        topRightRect.setArcWidth(topRight);
        topRightRect.setArcHeight(topRight);
        bottomRightRect.setArcWidth(bottomRight);
        bottomRightRect.setArcHeight(bottomRight);
        bottomLeftRect.setArcWidth(bottomLeft);
        bottomLeftRect.setArcHeight(bottomLeft);

        Group clipGroup = new Group(
                topLeftRect,
                topRightRect,
                bottomRightRect,
                bottomLeftRect
        );

        node.setClip(clipGroup);
    }

    public static void round(Node node, double cornerRadius) {

        double addWidth = 30;
        double addHeight = 30;

        double width = node.getLayoutBounds().getWidth() / 2 + addWidth;
        double height = node.getLayoutBounds().getHeight() / 2 + addHeight;

        Rectangle topLeftRect = new Rectangle(0, 0, width, height);
        Rectangle topRightRect = new Rectangle(width - addWidth * 2, 0, width, height);
        Rectangle bottomRightRect = new Rectangle(width - addWidth * 2, height - addHeight * 2, width, height);
        Rectangle bottomLeftRect = new Rectangle(0, height - addHeight * 2, width, height);

        topLeftRect.setArcWidth(cornerRadius);
        topLeftRect.setArcHeight(cornerRadius);
        topRightRect.setArcWidth(cornerRadius);
        topRightRect.setArcHeight(cornerRadius);
        bottomRightRect.setArcWidth(cornerRadius);
        bottomRightRect.setArcHeight(cornerRadius);
        bottomLeftRect.setArcWidth(cornerRadius);
        bottomLeftRect.setArcHeight(cornerRadius);

        Group clipGroup = new Group(
                topLeftRect,
                topRightRect,
                bottomRightRect,
                bottomLeftRect
        );

        node.setClip(clipGroup);
    }

    public static void viewingImages(StackPane stackPane, List<Image> imageList) {
        Pane shadowPane = new Pane();
        shadowPane.setStyle("-fx-background-color: rgba(0,0,0,0.7);");
        stackPane.getChildren().add(shadowPane);

        AtomicInteger currInd = new AtomicInteger();

        StackPane whiteStackPane = new StackPane();
        StackPane.setAlignment(whiteStackPane, Pos.CENTER);
        stackPane.getChildren().add(whiteStackPane);
        whiteStackPane.setStyle("-fx-background-color: white;");

        ImageView centralImageView = new ImageView(imageList.getFirst());
        centralImageView.setPreserveRatio(true);
        centralImageView.toFront();
        centralImageView.fitHeightProperty().bind(stackPane.heightProperty().subtract(100));
        StackPane.setAlignment(centralImageView, Pos.CENTER);
        round(centralImageView, 30);
        whiteStackPane.getChildren().add(centralImageView);

        whiteStackPane.maxWidthProperty().bind(centralImageView.fitWidthProperty());
        whiteStackPane.maxHeightProperty().bind(centralImageView.fitHeightProperty());

        Button prevImage = new Button();
        prevImage.setMaxHeight(30);
        prevImage.setMaxWidth(30);
        prevImage.getStyleClass().addAll("image-btn", "paging-btn");
        StackPane.setAlignment(prevImage, Pos.CENTER_LEFT);
        StackPane.setMargin(prevImage, new Insets(0, 0, 0, 10));
        stackPane.getChildren().add(prevImage);

        ImageView leftArrow = new ImageView(
                new Image(Objects.requireNonNull(EntranceController.class.getResourceAsStream("/com/example/speech/image/left-arrow.png"))));
        leftArrow.setFitHeight(15);
        leftArrow.setFitWidth(15);
        leftArrow.setPreserveRatio(true);

        prevImage.setGraphic(leftArrow);

        Button nextImage = new Button();
        nextImage.setMaxHeight(30);
        nextImage.setMaxWidth(30);
        nextImage.getStyleClass().addAll("image-btn", "paging-btn");
        StackPane.setAlignment(nextImage, Pos.CENTER_RIGHT);
        StackPane.setMargin(nextImage, new Insets(0, 10, 0, 0));
        stackPane.getChildren().add(nextImage);

        ImageView rightArrow = new ImageView(
                new Image(Objects.requireNonNull(EntranceController.class.getResourceAsStream("/com/example/speech/image/right-arrow.png"))));
        rightArrow.setFitHeight(15);
        rightArrow.setFitWidth(15);
        rightArrow.setPreserveRatio(true);

        nextImage.setGraphic(rightArrow);

        if(currInd.get() == 0)
            prevImage.setVisible(false);
        if (imageList.size() <= 1)
            nextImage.setVisible(false);

        prevImage.setOnAction(e -> {
            currInd.updateAndGet(i -> i - 1);
            centralImageView.setImage(imageList.get(currInd.get()));

            if(currInd.get() == 0)
                prevImage.setVisible(false);

            nextImage.setVisible(true);
        });

        nextImage.setOnAction(e -> {
            currInd.updateAndGet(i -> i + 1);
            centralImageView.setImage(imageList.get(currInd.get()));

            if(currInd.get() == imageList.size() - 1)
                nextImage.setVisible(false);

            prevImage.setVisible(true);
        });

        Button delBtn = new Button();
        delBtn.setMaxHeight(30);
        delBtn.setMaxWidth(30);
        delBtn.getStyleClass().add("image-btn");
        StackPane.setAlignment(delBtn, Pos.TOP_RIGHT);
        StackPane.setMargin(delBtn, new Insets(10, 10, 0, 0));
        delBtn.setOnAction(e -> {
            stackPane.getChildren().removeAll(shadowPane, whiteStackPane, prevImage, nextImage, delBtn);
        });

        stackPane.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> {
            if (isAncestor(prevImage, (Node) e.getTarget()) || isAncestor(nextImage, (Node) e.getTarget())) {
                return;
            }

            delBtn.fire();
        });

        ImageView btnImageViewNotFocused = new ImageView(
                new Image(Objects.requireNonNull(ImageUtils.class.getResourceAsStream("/com/example/speech/image/del-file-not-focused.png"))));
        btnImageViewNotFocused.setFitHeight(30);
        btnImageViewNotFocused.setFitWidth(30);
        btnImageViewNotFocused.setPreserveRatio(true);

        ImageView btnImageViewFocused = new ImageView(
                new Image(Objects.requireNonNull(ImageUtils.class.getResourceAsStream("/com/example/speech/image/del-file-focused.png"))));
        btnImageViewFocused.setFitHeight(30);
        btnImageViewFocused.setFitWidth(30);
        btnImageViewFocused.setPreserveRatio(true);

        delBtn.setGraphic(btnImageViewNotFocused);

        delBtn.setOnMouseEntered(e ->{
            delBtn.setGraphic(btnImageViewFocused);
        });

        delBtn.setOnMouseExited(e ->{
            delBtn.setGraphic(btnImageViewNotFocused);
        });

        stackPane.getChildren().add(delBtn);
    }

    private static boolean isAncestor(Node ancestor, Node node) {
        while (node != null) {
            if (node == ancestor) return true;
            node = node.getParent();
        }
        return false;
    }
}
