package com.example.speech.control;

import com.example.speech.model.Message;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


public class TextMessageCellController {
    @FXML
    public ImageView userPhotoIV;
    @FXML
    private Label messageLabel, timeLabel;
    @FXML
    private GridPane contentGP;

    public GridPane initializeMessage(Message message) {
        userPhotoIV.setImage(message.getChannelUser().getUser().getPhotoImage());
        messageLabel.setText(new String(message.getMessageContent(), StandardCharsets.UTF_8));
        timeLabel.setText(message.getMessageDatetime().toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm")));
        return contentGP;
    }

    public void setMaxWidth(double v) {
        contentGP.setMaxWidth(v);
    }
}
