package com.example.speech.control;

import com.example.speech.model.ChannelUser;
import com.example.speech.model.HiddenChannelUser;
import com.example.speech.model.Message;
import com.example.speech.service.ChannelUserService;
import com.example.speech.service.HiddenChannelUserService;
import com.example.speech.service.MessageService;
import com.example.speech.util.HelpfulClass;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

import java.time.format.DateTimeFormatter;
import java.util.List;

import static com.example.speech.util.ImageUtils.setCircularImage;

public class ChannelCellController {
    @FXML
    private HBox rootContainer;
    @FXML
    private ImageView channelPhotoIV;
    @FXML
    private Label channelNameLb, stateLb, timeLastMessageLB, channelNameUniqueLb;
    @FXML
    private Button deleteChanelBtn;
    private ChannelUserService channelUserService = new ChannelUserService();

    private SpeechBaseController speechBaseController;
    private ChannelUser channelUser;

    public void initialize(ChannelUser channelUser, SpeechBaseController speechBaseController) {
        this.speechBaseController = speechBaseController;
        this.channelUser = channelUser;

        if (channelUser.getChannel().getChannelLogo() != null && channelUser.getChannel().getChannelLogo().length > 0)
            setCircularImage(channelPhotoIV, channelUser.getPhotoImage(), 45);
        if(channelUser.getVisibleNameChat().length() > 17)
            channelNameLb.setText(channelUser.getVisibleNameChat().substring(0, 17));
        else
            channelNameLb.setText(channelUser.getVisibleNameChat());

        if(channelUser.getChannel().getChannelType().getChannelTypeId() == 3)
            channelNameUniqueLb.setText(channelUserService
                    .getInterlocutorUserChannelInChannel(channelUser.getChannel().getChannelID(), channelUser.getUser().getIdUser())
                    .getUser().getNameUser());
        else
            channelNameUniqueLb.setText(channelUser.getChannel().getChannel_name_unique());

        List<Message> listMessage = new MessageService().getAllMessageInChannel(channelUser.getChannel().getChannelID());
        if(listMessage == null || listMessage.isEmpty()) {
            String stateLBText;
            if(channelUser.getChannel().getChannelType().getChannelTypeId() == 3) {
                stateLBText = channelUser.getStatusOfTheInterlocutor();
                if(stateLBText == null) {
                    stateLBText = channelUser.getUser().getStatusUser();
                    String finalStateLBText = stateLBText;
                    Thread updateThread = new Thread(() -> {
                        channelUser.setStatusOfTheInterlocutor(finalStateLBText);
                        channelUserService.update(channelUser);
                    });
                    updateThread.setDaemon(true);
                    updateThread.start();
                }
                if(stateLBText.equals("в сети"))
                    stateLb.setStyle("-fx-text-fill: #00C49A;");
                else
                    stateLb.setStyle("");
            } else stateLBText = String.format("Число участников: %d", channelUser.getChannel().getChannelCountUser());
            stateLb.setText(stateLBText);

            timeLastMessageLB.setVisible(false);
        } else {
            stateLb.setStyle("");
            Message lastMessage = listMessage.getLast();
            String resultStr;
            if(lastMessage.getChannelUser().getUser().equals(speechBaseController.getCurrentUser())) {
                if (lastMessage.getMessageString() != null && !lastMessage.getMessageString().isEmpty()) {
                    resultStr = "Вы: " + lastMessage.getMessageString();
                } else if (lastMessage.getChannelInvitations() != null) {
                    resultStr = "Вы: Приглашение";
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
                } else if (lastMessage.getChannelInvitations() != null) {
                    resultStr = senderName + ": Приглашение";
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

        double deleteChanelBtnSize = 22;

        HelpfulClass.setImageWithButton(deleteChanelBtn,
                "image-delete-chat-not-focused.png", "delete-chat-btn", deleteChanelBtnSize, deleteChanelBtnSize);

        rootContainer.setOnMouseEntered(event -> {
            deleteChanelBtn.setVisible(true);
        });

        rootContainer.setOnMouseExited(event -> {
            deleteChanelBtn.setVisible(false);
        });

        deleteChanelBtn.setOnMouseEntered(event -> {
            HelpfulClass.setImageWithButton(deleteChanelBtn,
                    "image-delete-chat-focused.png", "delete-chat-btn", deleteChanelBtnSize, deleteChanelBtnSize);
        });

        deleteChanelBtn.setOnMouseExited(event -> {
            HelpfulClass.setImageWithButton(deleteChanelBtn,
                    "image-delete-chat-not-focused.png", "delete-chat-btn", deleteChanelBtnSize ,deleteChanelBtnSize);
        });
    }

    @FXML
    private void onDeleteChanelBtn() {
        HiddenChannelUser hiddenChannelUser = new HiddenChannelUser();
        hiddenChannelUser.setUser(speechBaseController.getCurrentUser());
        hiddenChannelUser.setChannel(channelUser.getChannel());
        new HiddenChannelUserService().save(hiddenChannelUser);
        speechBaseController.userChats.remove(channelUser);
    }

    public void notVisibleDelBtn() {
        deleteChanelBtn.setVisible(false);
        deleteChanelBtn.setManaged(false);
    }
}
