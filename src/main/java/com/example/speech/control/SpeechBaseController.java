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

        // Настройте ListView перед загрузкой данных
        initializeListViewChats();
    }

    public void initializeListViewChats() {
        // Очистите и настройте ListView
        chatsView.getItems().clear();

        // Установите фиксированную высоту ячеек
        chatsView.setFixedCellSize(60);

        // Установите фабрику ячеек
        chatsView.setCellFactory(lv -> new ListChannelsCellController());

        // Загрузите данные
        List<ChannelUser> userChats = channelUserService.getAllChatsByUser(user);
        System.out.println("Found chats: " + userChats.size());

        // Добавьте данные
        chatsView.getItems().addAll(userChats);

        // Обновите стили для видимости
        chatsView.setStyle("-fx-background-color: white; -fx-border-color: lightgray;");
    }
}
