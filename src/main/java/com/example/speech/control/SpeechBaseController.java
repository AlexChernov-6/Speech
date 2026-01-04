package com.example.speech.control;

import com.example.speech.model.ChannelUser;
import com.example.speech.model.User;
import com.example.speech.service.ChannelUserService;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;

import static com.example.speech.util.HelpfulStylingClass.setupFullScreenListener;

public class SpeechBaseController {
    private Stage stage;
    private User user;

    @FXML
    private ListView<ChannelUser> chatsView;

    private ChannelUserService channelUserService = new ChannelUserService();

    @FXML
    private Label channelName, channelStatus;
    @FXML
    private AnchorPane rootAnchorPane;
    @FXML
    private TextArea messageTA;
    @FXML
    private VBox emojiVB, sendVB;
    @FXML
    private AnchorPane messageAnchor;

    public void initializeData(Stage stage, User currentUser) {
        this.stage = stage;
        this.user = currentUser;
        setupFullScreenListener(stage, rootAnchorPane);
        initializeListViewChats();
    }

    public void initializeListViewChats() {
        chatsView.getItems().clear();
        chatsView.setFixedCellSize(60);
        chatsView.setCellFactory(lv -> new ListChannelsCellController());
        List<ChannelUser> userChats = channelUserService.getAllChatsByUser(user);
        chatsView.getItems().addAll(userChats);

        chatsView.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    channelName.setText(newValue.getChannel().getChannelName());
                    channelStatus.setText(newValue.getChannel().getChannelCountUser() == 2 ?
                            channelUserService.getInterlocutorStatus(newValue.getChannel(), newValue.getUser()) :
                            String.format("Число участников: %d", newValue.getChannel().getChannelCountUser()));
                    messageTA.setText("");
                });

        messageTA.textProperty().addListener((observable, oldValue, newValue) -> {
            boolean hasVisibleText = newValue != null &&
                    !newValue.trim().isEmpty() &&
                    !newValue.matches("^[\\n\\r\\s]*$");

            if (!hasVisibleText) {
                sendVB.setVisible(false);
                AnchorPane.setRightAnchor(emojiVB, 0.0);
                AnchorPane.setRightAnchor(messageTA, 51.0);
            } else {
                AnchorPane.setRightAnchor(messageTA, 102.0);
                AnchorPane.setRightAnchor(emojiVB, 50.0);
                sendVB.setVisible(true);
            }
            if(countLines(newValue) > countLines(oldValue) && messageAnchor.getHeight() <= 180) {
                messageAnchor.setMinHeight(messageAnchor.getHeight() + 20);
            } else if (countLines(newValue) < countLines(oldValue) && messageAnchor.getHeight() >= 60
                    && !checkScrollVisibility(messageTA)) {
                messageAnchor.setMinHeight(messageAnchor.getHeight() - 20);
            }
        });
    }

    private int countLines(String text) {
        if (text == null || text.isEmpty()) {
            return 1; // Пустая TextArea всё равно имеет одну строку
        }

        // Считаем все переносы: \n, \r\n, \r
        int lineCount = 1;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '\n') {
                lineCount++;
            } else if (c == '\r') {
                lineCount++;
                // Пропускаем следующий символ если это \r\n
                if (i + 1 < text.length() && text.charAt(i + 1) == '\n') {
                    i++;
                }
            }
        }
        return lineCount;
    }

    private boolean checkScrollVisibility(TextArea textArea) {
        javafx.scene.control.ScrollPane scrollPane =
                (javafx.scene.control.ScrollPane) textArea.lookup(".scroll-pane");

        if (scrollPane != null) {
            javafx.scene.control.ScrollBar vScrollBar =
                    (javafx.scene.control.ScrollBar) scrollPane.lookup(".scroll-bar:vertical");

            return vScrollBar != null && vScrollBar.isVisible();
        }

        return false;
    }
}
