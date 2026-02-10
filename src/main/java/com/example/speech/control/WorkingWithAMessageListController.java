package com.example.speech.control;

import com.example.speech.model.Message;
import com.example.speech.service.MessageService;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

import com.example.speech.control.SpeechBaseController.ContextPopUpBar;

public class WorkingWithAMessageListController extends Pane {

    private static final Image replyI = new Image(Objects.requireNonNull
            (WorkingWithAMessageListController.class.getResourceAsStream("/com/example/speech/image/reply.png")));
    private static final Image pinI = new Image(Objects.requireNonNull
            (WorkingWithAMessageListController.class.getResourceAsStream("/com/example/speech/image/pin.png")));
    private static final Image unPinI = new Image(Objects.requireNonNull
            (WorkingWithAMessageListController.class.getResourceAsStream("/com/example/speech/image/unpin.png")));
    private static final Image copyI = new Image(Objects.requireNonNull
            (WorkingWithAMessageListController.class.getResourceAsStream("/com/example/speech/image/copy.png")));
    protected static final Image forwardI = new Image(Objects.requireNonNull
            (WorkingWithAMessageListController.class.getResourceAsStream("/com/example/speech/image/forward.png")));
    private static final Image deleteI = new Image(Objects.requireNonNull
            (WorkingWithAMessageListController.class.getResourceAsStream("/com/example/speech/image/delete.png")));
    private static final Image selectI = new Image(Objects.requireNonNull
            (WorkingWithAMessageListController.class.getResourceAsStream("/com/example/speech/image/select.png")));
    private static final Image changeI = new Image(Objects.requireNonNull
            (WorkingWithAMessageListController.class.getResourceAsStream("/com/example/speech/image/change.png")));

    private final Clipboard clipboard = Clipboard.getSystemClipboard();
    private final ClipboardContent content = new ClipboardContent();
    private final MessageService messageService = new MessageService();

    public void initializeShape(double xPos, double yPos, SpeechBaseController speechBaseController, Message message) {
        boolean isCurrentUserMessage = (Objects.equals(speechBaseController.getCurrentUser().getIdUser()
                , message.getChannelUser().getUser().getIdUser()));

        String contentMessage = new String(message.getMessageContent(), StandardCharsets.UTF_8);

        StackPane messagesSP = speechBaseController.getMessagesSP();
        String channelName = speechBaseController.getChannelName().getText();
        ListView<Message> messagesLV = speechBaseController.getMessagesLV();
        TextArea messageTA = speechBaseController.getMessageTA();
        Long currentUserId = Long.valueOf(speechBaseController.getCurrentUser().getIdUser());
        HBox updateMessageHB = speechBaseController.getUpdateMessageHB();
        Label contentUpdateMessageLB = speechBaseController.getContentUpdateMessageLB();
        ImageView HintIV = speechBaseController.getHintIV();
        Label HintLB = speechBaseController.getHintLB();

        double vBoxWidth = 200;
        double vBoxHeight = (isCurrentUserMessage) ? 300 : 250;

        VBox rootVB = new VBox();
        rootVB.setMaxWidth(vBoxWidth);
        rootVB.setMaxHeight(vBoxHeight);
        rootVB.getStyleClass().add("working-with-a-message-root-pane");

        double stackPaneWidth = messagesSP.getWidth();
        double stackPaneHeight = messagesSP.getHeight();

        double finalX = xPos;
        if (xPos + vBoxWidth > stackPaneWidth) {
            finalX = xPos - vBoxWidth;
        }

        double finalY = yPos;
        if (yPos + vBoxHeight > stackPaneHeight) {
            finalY = yPos - vBoxHeight;
        }

        if (finalX < 0) {
            finalX = 0;
        }

        if (finalY < 0) {
            finalY = 0;
        }

        rootVB.setLayoutX(finalX);
        rootVB.setLayoutY(finalY);

        CustomButton reply = new CustomButton(replyI, "Ответить");
        reply.setPrefWidth(vBoxWidth);
        reply.setPrefHeight(40);
        VBox.setMargin(reply, new Insets(5, 0, 0, 0));
        reply.setOnAction(e -> {
            HintIV.setImage(replyI);
            HintLB.setText("В ответ " + message.getChannelUser().getUser().getNameUser());
            speechBaseController.setContextPopUpBar(ContextPopUpBar.REPLY_MESSAGE);
            contentUpdateMessageLB.setText(contentMessage);
            updateMessageHB.setVisible(true);
            updateMessageHB.setManaged(true);
            speechBaseController.setMessageIdReplyTo(message.getMessageId());
            messagesSP.getChildren().remove(this);
            Platform.runLater(() -> {
                messageTA.requestFocus();
            });
        });

        CustomButton pin;
        if (message.getPinMessage()) {
            pin = new CustomButton(unPinI, "Открепить");
            pin.setOnAction(e -> {
                messagesSP.getChildren().remove(this);
                message.setPinMessage(false);
                messageService.update(message);
                speechBaseController.setPinnedMessagesHBVisible(messagesLV.getItems().size());
            });
        } else {
            pin = new CustomButton(pinI, "Закрепить");
            pin.setOnAction(e -> {
                messagesSP.getChildren().remove(this);
                message.setPinMessage(true);
                messageService.update(message);
                speechBaseController.setPinnedMessagesHBVisible(messagesLV.getItems().size());
            });
        }
        pin.setPrefWidth(vBoxWidth);
        pin.setPrefHeight(40);


        CustomButton copy = new CustomButton(copyI, "Копировать текст");
        copy.setPrefWidth(vBoxWidth);
        copy.setPrefHeight(40);
        copy.setOnAction(e -> {
            content.putString(contentMessage);
            clipboard.setContent(content);
            messagesSP.getChildren().remove(this);
        });

        CustomButton forward = new CustomButton(forwardI, "Переслать");
        forward.setPrefWidth(vBoxWidth);
        forward.setPrefHeight(40);
        forward.setOnAction(e -> {
            messagesSP.getChildren().remove(this);
            new ChatSelectionController(speechBaseController, message);
        });

        CustomButton delete = new CustomButton(deleteI, "Удалить");
        delete.setPrefWidth(vBoxWidth);
        delete.setPrefHeight(40);
        delete.setOnAction(e -> {
            messagesSP.getChildren().remove(this);
            new ConfirmationOfMessageDeletion().initializeShape(channelName, message, messagesSP, messagesLV
                    , currentUserId);
        });


        CustomButton select = new CustomButton(selectI, "Выделить");
        select.setPrefWidth(vBoxWidth);
        select.setPrefHeight(40);
        VBox.setMargin(select, new Insets(0, 0, 5, 0));

        if(isCurrentUserMessage) {
            CustomButton change = new CustomButton(changeI, "Изменить");
            change.setPrefWidth(vBoxWidth);
            change.setPrefHeight(40);
            change.setOnAction(e -> {
                messageTA.setText(contentMessage);
                HintIV.setImage(changeI);
                HintLB.setText("Редактирование");
                speechBaseController.setContextPopUpBar(ContextPopUpBar.CHANGE_MESSAGE);
                contentUpdateMessageLB.setText(contentMessage);
                updateMessageHB.setVisible(true);
                updateMessageHB.setManaged(true);
                speechBaseController.setUpdateMessage(message);
                messagesSP.getChildren().remove(this);
                Platform.runLater(() -> {
                    messageTA.requestFocus();
                    messageTA.positionCaret(messageTA.getText().length());
                });
            });

            rootVB.getChildren().addAll(reply, change, pin, copy, forward, delete, select);
        } else {
            rootVB.getChildren().addAll(reply, pin, copy, forward, delete, select);
        }

        this.getChildren().add(rootVB);

        this.setLayoutX(0);
        this.setLayoutY(0);
    }
}