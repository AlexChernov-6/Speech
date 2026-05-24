package com.example.speech.control;

import com.example.speech.model.User;
import com.example.speech.util.ImageUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;

import static com.example.speech.util.ImageUtils.setCircularImage;

public class UserCell extends ListCell<User> {
    private final HBox rootHB;
    private ImageView logoCell;
    private Label modelNameLB, modelStateLB;
    private User ownerUser;
    private Label isOwnerLB;
    private SpeechBaseController speechBaseController;

    private double logoSize = 45.0;

    public UserCell() {
        getStyleClass().setAll("chats-user-list-view");
        rootHB = new HBox(10);
        rootHB.setPadding(new Insets(5));
        rootHB.setPrefHeight(60);
        rootHB.setAlignment(Pos.CENTER_LEFT);

        logoCell = new ImageView();
        logoCell.setFitWidth(logoSize);
        logoCell.setFitHeight(logoSize);
        rootHB.getChildren().add(logoCell);

        Circle clip = new Circle();
        clip.setRadius(22.5);
        clip.setCenterX(22.5);
        clip.setCenterY(22.5);
        logoCell.setClip(clip);

        VBox rightVB = new VBox(5);
        rootHB.getChildren().add(rightVB);

        modelNameLB = new Label();
        modelNameLB.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");
        rightVB.getChildren().add(modelNameLB);

        modelStateLB = new Label();
        modelStateLB.setStyle("-fx-text-fill: gray; -fx-font-size: 10px;");
        rightVB.getChildren().add(modelStateLB);

        HBox rightHB = new HBox();
        HBox.setHgrow(rightHB, Priority.ALWAYS);
        rightHB.setAlignment(Pos.TOP_RIGHT);
        rootHB.getChildren().add(rightHB);

        Label isOwnerLB = new Label("Владелец");
        isOwnerLB.setStyle("-fx-padding: 0 5; -fx-background-radius: 15; -fx-text-fill: white; -fx-background-color: blue;");
        isOwnerLB.setVisible(false);
        rightHB.getChildren().add(isOwnerLB);
    }

    public UserCell(User ownerUser, SpeechBaseController speechBaseController) {
        this.ownerUser = ownerUser;
        this.speechBaseController = speechBaseController;
        getStyleClass().setAll("chats-user-list-view");
        rootHB = new HBox(10);
        rootHB.setPadding(new Insets(5));
        rootHB.setPrefHeight(60);
        rootHB.setAlignment(Pos.CENTER_LEFT);

        logoCell = new ImageView();
        logoCell.setFitWidth(logoSize);
        logoCell.setFitHeight(logoSize);
        logoCell.setPreserveRatio(true);
        rootHB.getChildren().add(logoCell);

        Circle clip = new Circle();
        clip.setRadius(22.5);
        clip.setCenterX(22.5);
        clip.setCenterY(22.5);
        logoCell.setClip(clip);

        VBox rightVB = new VBox(5);
        rootHB.getChildren().add(rightVB);

        modelNameLB = new Label();
        modelNameLB.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");
        rightVB.getChildren().add(modelNameLB);

        modelStateLB = new Label();
        modelStateLB.setStyle("-fx-text-fill: gray; -fx-font-size: 10px;");
        rightVB.getChildren().add(modelStateLB);

        HBox rightHB = new HBox();
        HBox.setHgrow(rightHB, Priority.ALWAYS);
        rightHB.setAlignment(Pos.TOP_RIGHT);
        rightHB.setPadding(new Insets(5, 5, 0, 0));
        rootHB.getChildren().add(rightHB);

        isOwnerLB = new Label("Владелец");
        isOwnerLB.setStyle("-fx-padding: 0 10; -fx-background-radius: 15; -fx-text-fill: white; -fx-background-color: rgba(0, 180, 120, 0.6);");
        isOwnerLB.setVisible(false);
        rightHB.getChildren().add(isOwnerLB);
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
        setCircularImage(logoCell, user.getPhotoImage(), logoSize);
        modelNameLB.setText(user.getNameUser());
        modelStateLB.setText(user.getStatusUser());

        isOwnerLB.setVisible(user.equals(ownerUser));

        if(speechBaseController != null && speechBaseController.getCurrentUser().equals(user))
            rootHB.setStyle("-fx-background-color: #E0F2EF");
        else
            rootHB.setStyle("");

    }
}
