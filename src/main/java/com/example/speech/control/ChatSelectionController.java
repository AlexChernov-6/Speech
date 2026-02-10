package com.example.speech.control;

import com.example.speech.model.ChannelUser;
import com.example.speech.model.Message;
import com.example.speech.service.ChannelUserService;
import com.example.speech.service.MessageService;
import javafx.application.Platform;
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

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.example.speech.control.WorkingWithAMessageListController.forwardI;

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
                    if (newValue != null) {
                        stackPane.getChildren().remove(this);
                        speechBaseController.setContextPopUpBar(SpeechBaseController.ContextPopUpBar.FORWARD_MESSAGE);
                        speechBaseController.setForwardMessages(List.of(messages));
                        speechBaseController.getHintIV().setImage(forwardI);
                        List<String> senders = new ArrayList<>();
                        for(Message message : messages) {
                            if(!senders.contains(message.getChannelUser().getUser().getNameUser()))
                                senders.add(message.getChannelUser().getUser().getNameUser());
                        }
                        if(messages.length > 1)
                            speechBaseController.getContentUpdateMessageLB().setText(messages.length + " пересланных сообщения");
                        else
                            speechBaseController.getContentUpdateMessageLB().setText(new String(messages[0].getMessageContent()
                                    , StandardCharsets.UTF_8));

                        if(senders.size() == 1)
                            speechBaseController.getHintLB().setText(senders.getFirst());
                        else if(senders.size() == 2)
                            speechBaseController.getHintLB().setText(senders.getFirst() + " и " + senders.getLast());
                        else if (senders.size() > 2)
                            speechBaseController.getHintLB().setText(senders.getLast() + " и " + (senders.size() - 1) + " других");

                        speechBaseController.getUpdateMessageHB().setVisible(true);
                        speechBaseController.getUpdateMessageHB().setManaged(true);
                        Platform.runLater(() -> {
                            speechBaseController.getMessageTA().requestFocus();
                        });
                        speechBaseController.setSelectedChannelUser(newValue);
                        speechBaseController.hideTheListOfPinnedMessages();
                        speechBaseController.loadChannelMessages(newValue);
                        speechBaseController.setPinnedMessagesHBVisible(speechBaseController.getMessagesLV().getItems().size());
                    }
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
