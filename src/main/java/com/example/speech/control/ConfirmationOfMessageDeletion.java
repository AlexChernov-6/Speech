package com.example.speech.control;

import com.example.speech.model.Message;
import com.example.speech.service.MessageService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.control.Label;
import javafx.scene.text.TextFlow;

public class ConfirmationOfMessageDeletion extends Pane {
    private static final MessageService MESSAGE_SERVICE = new MessageService();

    public void initializeShape(String nameOfTheInterlocutor, Message message, StackPane stackPane
            , ListView<Message> messagesLV, Long currentUserId) {
        this.setStyle("-fx-background-color: rgba(0, 0, 0, 0.6);");

        double vBoxWidth = 350;
        double vBoxHeight = 120;

        VBox rootVB = new VBox(15);
        rootVB.setPrefWidth(vBoxWidth);
        rootVB.setPrefHeight(vBoxHeight);
        rootVB.setAlignment(Pos.CENTER_LEFT);
        rootVB.getStyleClass().add("working-with-a-message-root-pane");
        rootVB.setLayoutX(stackPane.getScene().getWindow().getWidth() / 2 - vBoxWidth / 2);
        rootVB.setLayoutY(stackPane.getScene().getWindow().getHeight() / 2 - vBoxHeight / 2);
        rootVB.setPadding(new Insets(15));

        Label questionLabel = new Label("Удалить это сообщение?");
        questionLabel.setStyle("-fx-font-size: 14px");

        HBox centralHB = new HBox(10);

        CheckBox checkBox = new CheckBox();

        TextFlow informationTextFlow = new TextFlow();
        Label startLabel = new Label("Также удалить для «");
        Label centralLabel = new Label(nameOfTheInterlocutor);
        centralLabel.setStyle("-fx-font-weight: bold;");
        Label endLabel = new Label("»");
        informationTextFlow.getChildren().addAll(startLabel, centralLabel, endLabel);
        informationTextFlow.setStyle("-fx-font-size: 14px");

        centralHB.getChildren().addAll(checkBox, informationTextFlow);

        HBox bottomHB = new HBox(10);
        bottomHB.setAlignment(Pos.CENTER_RIGHT);

        Button cancellationButton = new Button();
        cancellationButton.setText("Отмена");
        cancellationButton.setOnAction(e -> {
            stackPane.getChildren().remove(this);
        });
        cancellationButton.getStyleClass().add("login-button");

        Button deleteButton = new Button();
        deleteButton.setText("Удалить");
        deleteButton.setOnAction(e -> {
            if (checkBox.isSelected())
                MESSAGE_SERVICE.delete(message);
            else {
                message.getDeletedByUsers().add(currentUserId);
                MESSAGE_SERVICE.update(message);
            }

            messagesLV.getItems().remove(message);
            stackPane.getChildren().remove(this);
        });
        deleteButton.getStyleClass().add("login-button");
        bottomHB.getChildren().addAll(cancellationButton, deleteButton);

        rootVB.getChildren().addAll(questionLabel, centralHB, bottomHB);
        this.getChildren().add(rootVB);

        this.setOnMousePressed(event -> {
            if(rootVB.getBoundsInParent().contains(event.getX(), event.getY()))
                event.consume();
            else
                stackPane.getChildren().remove(this);
        });

        stackPane.getChildren().add(this);
    }
}
