package com.example.speech.control;

import com.example.speech.model.ChannelUser;
import com.example.speech.model.Message;
import com.example.speech.service.ChannelUserService;
import com.example.speech.service.MessageService;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class ChannelCellController {
    @FXML
    private HBox rootContainer;
    @FXML
    private ImageView channelPhotoIV;
    @FXML
    private Label channelNameLb, stateLb, timeLastMessageLB, countUnreadMessages;
    @FXML
    private Button deleteChanelBtn;
    private ChannelUserService channelUserService = new ChannelUserService();

    private SpeechBaseController speechBaseController;
    private ChannelUser channelUser;

    public void initialize(ChannelUser channelUser, SpeechBaseController speechBaseController) {
        this.speechBaseController = speechBaseController;
        this.channelUser = channelUser;

        if (channelUser.getChannel().getChannelLogo() != null && channelUser.getChannel().getChannelLogo().length > 0)
            channelPhotoIV.setImage(channelUser.getChannel().getPhotoImage());
        channelNameLb.setText(channelUser.getChannel().getChannelName());
        List<Message> listMessage = new MessageService().getAllMessageInChannel(channelUser.getChannel().getChannelID());
        if(listMessage == null || listMessage.isEmpty()) {
            stateLb.setText(channelUser.getChannel().getChannelType().getChannelTypeId() == 3 ?
                    channelUserService.getInterlocutorStatus(channelUser.getChannel(), channelUser.getUser()) :
                    String.format("Число участников: %d", channelUser.getChannel().getChannelCountUser()));

            timeLastMessageLB.setVisible(false);
            countUnreadMessages.setVisible(false);
        } else {
            Message lastMessage = listMessage.getLast();
            String resultStr;
            if(lastMessage.getChannelUser().getUser().equals(speechBaseController.getCurrentUser())) {
                if (lastMessage.getMessageString() != null && !lastMessage.getMessageString().isEmpty()) {
                    resultStr = "Вы: " + lastMessage.getMessageString();
                } else {
                    int countMessageContent = lastMessage.getMessageContent().size();
                    if(countMessageContent == 1)
                        resultStr = "Вы: 1 вложение";
                    else if (countMessageContent > 1 && countMessageContent < 5)
                        resultStr = String.format("Вы: %d вложения", countMessageContent);
                    else
                        resultStr = String.format("Вы: %d вложений", countMessageContent);
                }
            } else {
                String senderName = lastMessage.getChannelUser().getUser().getNameUser();
                if (lastMessage.getMessageString() != null && !lastMessage.getMessageString().isEmpty()) {
                    resultStr = senderName + ": " + lastMessage.getMessageString();
                } else {
                    int countMessageContent = lastMessage.getMessageContent().size();
                    if(countMessageContent == 1)
                        resultStr = senderName + ": 1 вложение";
                    else if (countMessageContent > 1 && countMessageContent < 5)
                        resultStr = senderName + String.format(": %d вложения", countMessageContent);
                    else
                        resultStr = senderName + String.format(": %d вложений", countMessageContent);
                }
            }
            if(resultStr.length() < 20)
                stateLb.setText(resultStr);
            else
                stateLb.setText(resultStr.substring(0, 20));

            timeLastMessageLB.setVisible(true);
            timeLastMessageLB.setText(lastMessage.getMessageDatetime().toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm")));
        }
        rootContainer.setOnMouseEntered(event -> {
            deleteChanelBtn.setVisible(true);
        });

        rootContainer.setOnMouseExited(event -> {
            deleteChanelBtn.setVisible(false);
        });
    }

    @FXML
    private void onDeleteChanelBtn() {
        new ChannelUserService().delete(channelUser);
        speechBaseController.userChats.remove(channelUser);
    }

    public void notVisibleDelBtn() {
        deleteChanelBtn.setVisible(false);
        deleteChanelBtn.setManaged(false);
    }
}
