package com.example.speech.control;

import com.example.speech.model.ChannelUser;
import com.example.speech.model.User;
import com.example.speech.service.ChannelUserService;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.stage.Stage;

import java.util.List;

public class SpeechBaseController {
    private Stage stage;
    private User user;

    @FXML
    private ListView<ChannelUser> chatsView;

    private ChannelUserService channelUserService = new ChannelUserService();

    public void initializeData(Stage stage, User currentUser) {
        this.stage = stage;
        this.user = currentUser;

        initializeListViewChats();
    }

    public void initializeListViewChats() {
        chatsView.getItems().clear();
        chatsView.setFixedCellSize(60);
        chatsView.setCellFactory(lv -> new ListChannelsCellController());
        List<ChannelUser> userChats = channelUserService.getAllChatsByUser(user);
        chatsView.getItems().addAll(userChats);
    }
}
