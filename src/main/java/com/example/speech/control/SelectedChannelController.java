package com.example.speech.control;

import com.example.speech.model.Channel;
import com.example.speech.util.HelpfulStylingClass;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;

public class SelectedChannelController extends Button {
    @FXML
    private ImageView channelPhotoIV;
    @FXML
    private Label channelNameLb, stateLb;
    @FXML
    private Button deleteChanelBtn;

    public void initialize(Channel channel) {
        channelPhotoIV.setImage(HelpfulStylingClass.byteArrayToImage(channel.getChannelLogo()));
        channelNameLb.setText(channel.getChannelName());
    }

    @FXML
    private void onDeleteChanelBtn() {

    }
}
