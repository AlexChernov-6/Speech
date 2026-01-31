package com.example.speech.control;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

public class CustomButton extends Button {

    public CustomButton(Image image, String text) {
        super();

        HBox hBox = new HBox(10);
        hBox.setAlignment(Pos.CENTER_LEFT);

        ImageView imageView = new ImageView(image);
        imageView.setFitHeight(20);
        imageView.setFitWidth(20);
        imageView.setPreserveRatio(true);
        hBox.getChildren().add(imageView);

        Label label = new Label(text);
        label.setStyle("-fx-font-size: 14px; -fx-text-fill: black;");
        hBox.getChildren().add(label);

        setGraphic(hBox);

        getStyleClass().add("working-with-a-message-button");
    }
}
