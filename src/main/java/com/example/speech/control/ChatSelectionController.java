package com.example.speech.control;

import com.example.speech.model.ChannelUser;
import com.example.speech.model.Message;
import com.example.speech.service.ChannelUserService;
import com.example.speech.util.HelpfulStylingClass;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

import static com.example.speech.control.WorkingWithAMessageListController.forwardI;

public class ChatSelectionController extends StackPane {

    public ChatSelectionController(SpeechBaseController speechBaseController, ObservableList<Message> messages) {
        StackPane stackPane = speechBaseController.getMessagesSP();
        this.setStyle("-fx-background-color: rgba(0, 0, 0, 0.7);");

        double with = 400;
        double height = stackPane.getHeight() - 60;

        VBox rootVB = new VBox(15);
        rootVB.setMaxWidth(with);
        rootVB.setMaxHeight(height);
        rootVB.setAlignment(Pos.CENTER);
        rootVB.getStyleClass().add("working-with-a-message-root-pane");
        rootVB.setPadding(new Insets(15));

        Label headerLB = new Label("Переслать...");
        headerLB.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        headerLB.setPrefWidth(with);

        TextField searchTF = new TextField();
        searchTF.setPromptText("Поиск");
        searchTF.setPrefWidth(with);

        FilteredList<ChannelUser> filteredList = new FilteredList<>(
                FXCollections.observableList(new ChannelUserService().getAllChatsByUser(speechBaseController.getCurrentUser())),
                channelUser -> true);

        ListView<ChannelUser> chatsView = new ListView<>();
        chatsView.setFixedCellSize(60);
        chatsView.setCellFactory(lv -> {
            ListChannelsCellController cellController = new ListChannelsCellController(speechBaseController);
            cellController.notVisibleDelBtn();
            return cellController;
        });
        chatsView.setItems(filteredList);
        VBox.setVgrow(chatsView, Priority.ALWAYS);
        Platform.runLater(() -> {HelpfulStylingClass.scrollPaneAnimation(chatsView);});

        chatsView.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue != null) {
                        Platform.runLater(() -> {
                            stackPane.getChildren().remove(this);
                            speechBaseController.getChatsView().getSelectionModel().select(newValue);
                            speechBaseController.setContextPopUpBar(SpeechBaseController.ContextPopUpBar.FORWARD_MESSAGE);
                            speechBaseController.setForwardMessages(messages);
                            speechBaseController.getHintIV().setImage(forwardI);

                            speechBaseController.getSendMessageBtn().setVisible(true);
                            speechBaseController.getSendMessageBtn().setManaged(true);

                            List<String> senders = new ArrayList<>();
                            for (Message message : messages) {
                                if (!senders.contains(message.getChannelUser().getUser().getNameUser()))
                                    senders.add(message.getChannelUser().getUser().getNameUser());
                            }
                            if (messages.size() > 1)
                                speechBaseController.getContentUpdateMessageLB().setText(messages.size() + " пересланных сообщения");
                            else {
                                String contentMessage;
                                Message message = messages.getFirst();
                                if (message.getMessageString() != null && !message.getMessageString().isEmpty())
                                    contentMessage = message.getMessageString();
                                else if (message.getMessageContent() != null && !message.getMessageContent().isEmpty()) {
                                    int countContents = message.getMessageContent().size();
                                    if (countContents == 1)
                                        contentMessage = String.format("%d вложение", countContents);
                                    else if (countContents >= 2 && countContents <= 4)
                                        contentMessage = String.format("%d вложения", countContents);
                                    else
                                        contentMessage = String.format("%d вложений", countContents);
                                } else
                                    contentMessage = "";
                                speechBaseController.getContentUpdateMessageLB().setText(contentMessage);
                            }

                            if (senders.size() == 1)
                                speechBaseController.getHintLB().setText(senders.getFirst());
                            else if (senders.size() == 2)
                                speechBaseController.getHintLB().setText(senders.getFirst() + " и " + senders.getLast());
                            else if (senders.size() > 2)
                                speechBaseController.getHintLB().setText(senders.getLast() + " и " + (senders.size() - 1) + " других");

                            PauseTransition pauseTransition = new PauseTransition(Duration.millis(100));
                            pauseTransition.setOnFinished(e -> {
                                speechBaseController.getUpdateMessageHB().setVisible(true);
                                speechBaseController.getUpdateMessageHB().setManaged(true);
                            });
                            pauseTransition.play();
                        });
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
            Point2D point2D = rootVB.screenToLocal(event.getScreenX(), event.getScreenY());
            if (point2D != null && rootVB.contains(point2D)) {
                event.consume();
                return;
            } else
                stackPane.getChildren().remove(this);
        });

        searchTF.textProperty().addListener((ob, oldV, newV) -> {
            if(newV != null && !newV.isEmpty()) {
                filteredList.setPredicate(channelUser ->
                        (channelUser.getChannel().getChannelName() != null && channelUser.getChannel().getChannelName().toLowerCase().contains(newV.toLowerCase()))
                                || channelUser.getChannel().getChannel_name_unique().toLowerCase().contains(newV.toLowerCase()));
            } else filteredList.setPredicate(channelUser -> true);
        });

        stackPane.getChildren().add(this);
    }
}
