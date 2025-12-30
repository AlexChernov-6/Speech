package com.example.speech.control;

import com.example.speech.model.ChannelUser;
import com.example.speech.service.ChannelUserService;
import com.example.speech.util.HelpfulStylingClass;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;

public class ChannelCellController {
    @FXML
    private ImageView channelPhotoIV;
    @FXML
    private Label channelNameLb, stateLb;
    @FXML
    private Button deleteChanelBtn;
    private ChannelUserService channelUserService = new ChannelUserService();

    public void initialize(ChannelUser channelUser) {
        channelPhotoIV.setImage(HelpfulStylingClass.byteArrayToImage(channelUser.getChannel().getChannelLogo()));
        channelNameLb.setText(channelUser.getChannel().getChannelName());
        stateLb.setText(channelUser.getChannel().getChannelCountUser() == 2 ?
                channelUserService.getInterlocutorStatus(channelUser.getChannel(), channelUser.getUser()) :
                String.format("Число участников: %d", channelUser.getChannel().getChannelCountUser()));
    }
    @FXML
    private void onDeleteChanelBtn() {

    }
}
