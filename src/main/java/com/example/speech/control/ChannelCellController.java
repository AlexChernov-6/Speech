package com.example.speech.control;

import com.example.speech.model.ChannelUser;
import com.example.speech.service.ChannelUserService;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

public class ChannelCellController {
    @FXML
    private HBox rootContainer;
    @FXML
    private ImageView channelPhotoIV;
    @FXML
    private Label channelNameLb, stateLb;
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
        stateLb.setText(channelUser.getChannel().getChannelType().getChannelTypeId() == 3 ?
                channelUserService.getInterlocutorStatus(channelUser.getChannel(), channelUser.getUser()) :
                String.format("Число участников: %d", channelUser.getChannel().getChannelCountUser()));
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
