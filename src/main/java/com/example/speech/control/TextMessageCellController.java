package com.example.speech.control;

import com.example.speech.model.Message;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.util.Duration;

import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.Objects;


public class TextMessageCellController {
    @FXML
    public ImageView userPhotoIV;
    @FXML
    private Label messageLabel, timeLabel;
    @FXML
    private GridPane contentGP;
    @FXML
    public ImageView statusIV;
    @FXML
    private HBox timeStatusHB, rootMessageHB;
    @FXML
    private Label changeStatusLB;
    @FXML
    private ColumnConstraints columnInfo;
    @FXML
    private RowConstraints replyRow;
    @FXML
    private Button replyMessageBtn;

    private SpeechBaseController speechBaseController;
    private Message message;

    private static final Image shipped = new Image(Objects.requireNonNull
            (TextMessageCellController.class.getResourceAsStream("/com/example/speech/image/check.png")));
    private static final Image readIt = new Image(Objects.requireNonNull
            (TextMessageCellController.class.getResourceAsStream("/com/example/speech/image/double_check.png")));
    private static final Image loading = new Image(Objects.requireNonNull
            (TextMessageCellController.class.getResourceAsStream("/com/example/speech/image/clock.png")));

    public GridPane initializeMessage(SpeechBaseController speechBaseController, Message message, boolean drawUserPhoto) {
        this.speechBaseController = speechBaseController;
        this.message = message;
        String messageContent = new String(message.getMessageContent(), StandardCharsets.UTF_8);
        userPhotoIV.setImage(message.getChannelUser().getUser().getPhotoImage());
        messageLabel.setText(messageContent);
        timeLabel.setText(message.getMessageDatetime().toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm")));
        if(message.getMessageStatus() != null && message.getMessageStatus().equals("отправлено"))
            statusIV.setImage(shipped);
        else if(message.getMessageStatus() != null && message.getMessageStatus().equals("прочитано"))
            statusIV.setImage(readIt);
        else
            statusIV.setImage(loading);

        if (Objects.equals(message.getChannelUser().getUser().getIdUser(),speechBaseController.getCurrentUser()
                .getIdUser()))
            contentGP.getStyleClass().add("message-text-grid-pane-my");
        else {
            contentGP.getStyleClass().add("message-text-grid-pane-other");
            timeStatusHB.getChildren().remove(statusIV);
            HBox.setMargin(timeLabel, new Insets(0, 0, 0, 0));
        }

        if (!drawUserPhoto) {
            userPhotoIV.setVisible(false);
            contentGP.setStyle("-fx-background-radius: 15px 15px 15px 15px; -fx-border-radius: 15px 15px 15px 15px;");
        } else
            contentGP.setStyle("");

        setMouseListener();

        if(message.isModifiedMessage()) {
            columnInfo.setPrefWidth(100);
            columnInfo.setMinWidth(100);
            changeStatusLB.setVisible(true);
            changeStatusLB.setManaged(true);
        } else {
            columnInfo.setPrefWidth(60);
            columnInfo.setMinWidth(60);
        }

        if(message.getMessageIdReplyTo() != null) {
            Message replyMessage = speechBaseController.getMessageService().getRowById(message.getMessageIdReplyTo());
            replyRow.setPrefHeight(50);
            replyMessageBtn.setPrefHeight(45);
            replyMessageBtn.setVisible(true);
            replyMessageBtn.setManaged(true);

            String userName = replyMessage.getChannelUser().getUser().getNameUser();
            String messageContentReply = new String(replyMessage.getMessageContent(), StandardCharsets.UTF_8);

            String displayUserName = userName.length() > 20 ? userName.substring(0, 20) + "..." : userName;
            String displayMessage = messageContentReply.length() > 50 ? messageContentReply.substring(0, 50) + "..." : messageContentReply;

            Label nameLabel = new Label(displayUserName);
            nameLabel.getStyleClass().add("reply-user-name");

            Label messageLabel = new Label(displayMessage);
            messageLabel.getStyleClass().add("reply-message-text");

            VBox textContainer = new VBox(nameLabel, messageLabel);
            textContainer.setSpacing(2);
            textContainer.setAlignment(Pos.TOP_LEFT);
            textContainer.setMaxWidth(250);
            textContainer.setMaxHeight(40);

            replyMessageBtn.setGraphic(textContainer);
            replyMessageBtn.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);

            replyMessageBtn.setOnAction(e -> {
                Platform.runLater(() -> {
                    speechBaseController.getMessagesLV().scrollTo(replyMessage);
                    PauseTransition pause = new PauseTransition(Duration.millis(100));
                    pause.setOnFinished(event -> {
                        speechBaseController.getMessageCellCreator()
                                .getControllerCache(replyMessage)
                                .highlightMessageTemporarily();
                    });
                    pause.play();
                });
            });
        }

        return contentGP;
    }

    public void setMaxWidthGP(double v) {
        contentGP.setMaxWidth(v);
    }

    public void setMouseListener() {
        contentGP.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.SECONDARY) {
                WorkingWithAMessageListController controller = new WorkingWithAMessageListController();
                controller.initializeShape(event.getSceneX(), event.getSceneY(), speechBaseController, message);
                speechBaseController.getMessagesSP().getChildren().add(controller);

                event.consume();
            }
        });
    }

    public void highlightMessageTemporarily() {
        String blueColor = "rgba(100, 149, 237, ";

        Timeline highlightAnimation = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(rootMessageHB.opacityProperty(), 1.0),
                        new KeyValue(rootMessageHB.styleProperty(),
                                " -fx-background-color: " + blueColor + "0.0);" +
                                        " -fx-background-radius: 5px;" +
                                        " -fx-border-color: transparent;" +
                                        " -fx-border-width: 0;" +
                                        " -fx-effect: none;")
                ),

                new KeyFrame(Duration.millis(250),
                        new KeyValue(rootMessageHB.opacityProperty(), 1.0),
                        new KeyValue(rootMessageHB.styleProperty(),
                                " -fx-background-color: " + blueColor + "0.045);" + // 25% от пика
                                        " -fx-background-radius: 5px;" +
                                        " -fx-border-color: transparent;" +
                                        " -fx-border-width: 0;" +
                                        " -fx-effect: dropshadow(gaussian, " + blueColor + "0.02), 1, 0.1, 0, 0);") // ИСПРАВЛЕНО
                ),

                new KeyFrame(Duration.millis(500),
                        new KeyValue(rootMessageHB.opacityProperty(), 1.0),
                        new KeyValue(rootMessageHB.styleProperty(),
                                " -fx-background-color: " + blueColor + "0.09);" + // 50% от пика
                                        " -fx-background-radius: 5px;" +
                                        " -fx-border-color: transparent;" +
                                        " -fx-border-width: 0;" +
                                        " -fx-effect: dropshadow(gaussian, " + blueColor + "0.04), 2, 0.15, 0, 0);") // ИСПРАВЛЕНО
                ),

                new KeyFrame(Duration.millis(750),
                        new KeyValue(rootMessageHB.opacityProperty(), 1.0),
                        new KeyValue(rootMessageHB.styleProperty(),
                                " -fx-background-color: " + blueColor + "0.135);" + // 75% от пика
                                        " -fx-background-radius: 5px;" +
                                        " -fx-border-color: transparent;" +
                                        " -fx-border-width: 0;" +
                                        " -fx-effect: dropshadow(gaussian, " + blueColor + "0.06), 2.5, 0.18, 0, 0);") // ИСПРАВЛЕНО
                ),

                new KeyFrame(Duration.millis(1000),
                        new KeyValue(rootMessageHB.opacityProperty(), 1.0),
                        new KeyValue(rootMessageHB.styleProperty(),
                                " -fx-background-color: " + blueColor + "0.18);" + // 100% пик
                                        " -fx-background-radius: 5px;" +
                                        " -fx-border-color: transparent;" +
                                        " -fx-border-width: 0;" +
                                        " -fx-effect: dropshadow(gaussian, " + blueColor + "0.08), 3, 0.2, 0, 0);") // ИСПРАВЛЕНО
                ),

                new KeyFrame(Duration.millis(1500),
                        new KeyValue(rootMessageHB.opacityProperty(), 1.0),
                        new KeyValue(rootMessageHB.styleProperty(),
                                " -fx-background-color: " + blueColor + "0.18);" +
                                        " -fx-background-radius: 5px;" +
                                        " -fx-border-color: transparent;" +
                                        " -fx-border-width: 0;" +
                                        " -fx-effect: dropshadow(gaussian, " + blueColor + "0.08), 3, 0.2, 0, 0);") // ИСПРАВЛЕНО
                ),

                new KeyFrame(Duration.millis(2500),
                        new KeyValue(rootMessageHB.opacityProperty(), 1.0),
                        new KeyValue(rootMessageHB.styleProperty(),
                                " -fx-background-color: " + blueColor + "0.135);" + // 75%
                                        " -fx-background-radius: 5px;" +
                                        " -fx-border-color: transparent;" +
                                        " -fx-border-width: 0;" +
                                        " -fx-effect: dropshadow(gaussian, " + blueColor + "0.06), 2.5, 0.18, 0, 0);") // ИСПРАВЛЕНО
                ),

                new KeyFrame(Duration.millis(3500),
                        new KeyValue(rootMessageHB.opacityProperty(), 1.0),
                        new KeyValue(rootMessageHB.styleProperty(),
                                " -fx-background-color: " + blueColor + "0.09);" + // 50%
                                        " -fx-background-radius: 5px;" +
                                        " -fx-border-color: transparent;" +
                                        " -fx-border-width: 0;" +
                                        " -fx-effect: dropshadow(gaussian, " + blueColor + "0.04), 2, 0.15, 0, 0);") // ИСПРАВЛЕНО
                ),

                new KeyFrame(Duration.millis(4500),
                        new KeyValue(rootMessageHB.opacityProperty(), 1.0),
                        new KeyValue(rootMessageHB.styleProperty(),
                                " -fx-background-color: " + blueColor + "0.045);" + // 25%
                                        " -fx-background-radius: 5px;" +
                                        " -fx-border-color: transparent;" +
                                        " -fx-border-width: 0;" +
                                        " -fx-effect: dropshadow(gaussian, " + blueColor + "0.02), 1, 0.1, 0, 0);") // ИСПРАВЛЕНО
                ),

                new KeyFrame(Duration.millis(6000),
                        new KeyValue(rootMessageHB.opacityProperty(), 1.0),
                        new KeyValue(rootMessageHB.styleProperty(),
                                " -fx-background-color: transparent;" +
                                        " -fx-background-radius: 0px;" +
                                        " -fx-border-color: transparent;" +
                                        " -fx-border-width: 0;" +
                                        " -fx-effect: none;")
                )
        );

        highlightAnimation.setCycleCount(1);
        highlightAnimation.play();
    }
}
