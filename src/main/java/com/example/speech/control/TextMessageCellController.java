package com.example.speech.control;

import com.example.speech.model.ChannelUser;
import com.example.speech.model.Message;
import com.example.speech.model.User;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
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

    private StackPane messagesSP;
    private ListView<Message> messagesLV;
    private Message message;
    private User currentUser;
    private String channelName;

    private static final Image shipped = new Image(Objects.requireNonNull
            (TextMessageCellController.class.getResourceAsStream("/com/example/speech/image/check.png")));
    private static final Image readIt = new Image(Objects.requireNonNull
            (TextMessageCellController.class.getResourceAsStream("/com/example/speech/image/double_check.png")));
    private static final Image loading = new Image(Objects.requireNonNull
            (TextMessageCellController.class.getResourceAsStream("/com/example/speech/image/clock.png")));

    public GridPane initializeMessage(Message message, User currentUser, boolean drawUserPhoto, StackPane messagesSP,
                                      ListView<Message> messagesLV, Label channelName) {
        this.message = message;
        this.messagesSP = messagesSP;
        this.messagesLV = messagesLV;
        this.currentUser = currentUser;
        this.channelName = channelName.getText();
        userPhotoIV.setImage(message.getChannelUser().getUser().getPhotoImage());
        messageLabel.setText(new String(message.getMessageContent(), StandardCharsets.UTF_8));
        timeLabel.setText(message.getMessageDatetime().toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm")));
        if(message.getMessageStatus() != null && message.getMessageStatus().equals("отправлено"))
            statusIV.setImage(shipped);
        else if(message.getMessageStatus() != null && message.getMessageStatus().equals("прочитано"))
            statusIV.setImage(readIt);
        else
            statusIV.setImage(loading);

        if (Objects.equals(message.getChannelUser().getUser().getIdUser(), currentUser.getIdUser()))
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
        return contentGP;
    }

    public void setMaxWidth(double v) {
        contentGP.setMaxWidth(v);
    }

    public void setMouseListener() {
        contentGP.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.SECONDARY) {
                WorkingWithAMessageListController controller = new WorkingWithAMessageListController();
                controller.initializeShape(
                        event.getSceneX(),
                        event.getSceneY(),
                        messagesLV,
                        messagesSP,
                        message,
                        (Objects.equals(currentUser.getIdUser(), message.getChannelUser().getUser().getIdUser())),
                        channelName,
                        Long.valueOf(currentUser.getIdUser())
                );
                messagesSP.getChildren().add(controller);

                event.consume();
            }
        });
    }
}
