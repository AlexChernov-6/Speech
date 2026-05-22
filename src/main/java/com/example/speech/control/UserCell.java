package com.example.speech.control;

import com.example.speech.model.User;
import com.example.speech.util.ImageUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;

public class UserCell extends ListCell<User> {
    private final HBox rootHB;
    private ImageView logoCell;
    private Label modelNameLB, modelStateLB;

    public UserCell() {
        getStyleClass().setAll("chats-user-list-view");
        rootHB = new HBox(10);
        rootHB.setPadding(new Insets(5));
        rootHB.setPrefHeight(60);
        rootHB.setAlignment(Pos.CENTER_LEFT);

        logoCell = new ImageView();
        logoCell.setFitWidth(45);
        logoCell.setFitHeight(45);
        logoCell.setPreserveRatio(true);
        ImageUtils.round(logoCell, (double) 45 / 2);
        rootHB.getChildren().add(logoCell);

        Circle clip = new Circle();
        clip.setRadius(22.5);
        clip.setCenterX(22.5);
        clip.setCenterY(22.5);
        logoCell.setClip(clip);

        VBox rightVB = new VBox(5);
        HBox.setHgrow(rightVB, Priority.ALWAYS);
        rootHB.getChildren().add(rightVB);

        modelNameLB = new Label();
        modelNameLB.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");
        rightVB.getChildren().add(modelNameLB);

        modelStateLB = new Label();
        modelStateLB.setStyle("-fx-text-fill: gray; -fx-font-size: 10px;");
        rightVB.getChildren().add(modelStateLB);
    }

    @Override
    protected void updateItem(User user, boolean empty) {
        super.updateItem(user, empty);

        if(empty || user == null) {
            setGraphic(null);
            setText(null);
        } else {
            setDataInCell(user);
            setGraphic(rootHB);
        }
    }

    private void setDataInCell(User user) {
        logoCell.setImage(user.getPhotoImage());
        modelNameLB.setText(user.getNameUser());
        modelStateLB.setText(user.getStatusUser());
    }
}
