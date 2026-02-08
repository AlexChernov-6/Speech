package com.example.speech.control;

import com.example.speech.model.ChannelUser;
import com.example.speech.model.Message;
import com.example.speech.model.User;
import com.example.speech.service.ChannelUserService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.rmi.server.LogStream;

public class ChatSelectionController extends Pane {

    public ChatSelectionController(SpeechBaseController speechBaseController, Message... messages) {
        StackPane stackPane = speechBaseController.getMessagesSP();
        this.setStyle("-fx-background-color: rgba(0, 0, 0, 0.6);");

        double with = 400;
        double height = stackPane.getHeight() - 60;

        VBox rootVB = new VBox(15);
        rootVB.setPrefWidth(with);
        rootVB.setPrefHeight(height);
        rootVB.setAlignment(Pos.CENTER_LEFT);
        rootVB.getStyleClass().add("working-with-a-message-root-pane");
        rootVB.setLayoutX(stackPane.getScene().getWindow().getWidth() / 2 - with / 2);
        rootVB.setLayoutY(stackPane.getScene().getWindow().getHeight() / 2 - height / 2 - 60);
        rootVB.setPadding(new Insets(15));

        Label headerLB = new Label("Переслать...");
        headerLB.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        headerLB.setPrefWidth(with);

        //ImageView add
        TextField searchTF = new TextField();
        searchTF.setPromptText("Поиск");
        searchTF.setPrefWidth(with);

        ListView<ChannelUser> chatsView = new ListView<>();
        chatsView.setFixedCellSize(60);
        chatsView.setCellFactory(lv -> {
            ListChannelsCellController cellController = new ListChannelsCellController();
            cellController.notVisibleDelBtn();
            return cellController;
        });
        chatsView.getItems().addAll(new ChannelUserService().getAllChatsByUser(speechBaseController.getCurrentUser()));
        chatsView.getStyleClass().add("list-view");
        chatsView.setStyle("-fx-hbar-policy: never;");
        VBox.setVgrow(chatsView, Priority.ALWAYS);

        chatsView.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {

                });

        Button cancellationButton = new Button();
        cancellationButton.setText("Отмена");
        cancellationButton.setOnAction(e -> {
            stackPane.getChildren().remove(this);
        });
        cancellationButton.getStyleClass().add("login-button");

        rootVB.getChildren().addAll(headerLB, searchTF, chatsView, cancellationButton);
        this.getChildren().add(rootVB);

        this.setOnMousePressed(event -> {
            if (rootVB.getBoundsInParent().contains(event.getX(), event.getY()))
                event.consume();
            else
                stackPane.getChildren().remove(this);
        });

        stackPane.getChildren().add(this);
    }
}
