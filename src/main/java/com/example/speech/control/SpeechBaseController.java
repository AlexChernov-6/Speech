package com.example.speech.control;

import com.example.speech.model.ChannelUser;
import com.example.speech.model.Message;
import com.example.speech.model.User;
import com.example.speech.service.ChannelUserService;
import com.example.speech.service.MessageService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.time.format.DateTimeFormatter;
import java.util.List;

import static com.example.speech.util.HelpfulStylingClass.setupFullScreenListener;

public class SpeechBaseController {
    private Stage stage;
    private User user;

    @FXML
    private ListView<ChannelUser> chatsView;

    private final ChannelUserService channelUserService = new ChannelUserService();
    private final MessageService messageService = new MessageService();

    @FXML
    private Label channelName, channelStatus;
    @FXML
    private AnchorPane rootAnchorPane;
    @FXML
    private TextArea messageTA;
    @FXML
    private VBox selectedChatVB, emojiVB, sendVB;
    @FXML
    private ListView<Message> messagesLV;
    @FXML
    private AnchorPane messageAnchor;

    public void initializeData(Stage stage, User currentUser) {
        this.stage = stage;
        this.user = currentUser;
        setupFullScreenListener(stage, rootAnchorPane);
        initializeListViewChats();
        setupMessageTextAreaListener();
        selectedChatVB.widthProperty().addListener((ch, oldValue, newValue) -> {
            messagesLV.setPrefWidth((Double) newValue);
            messagesLV.setCellFactory(cellData -> new ListTextMessageCellController());
        });
        messagesLV.getStyleClass().add("no-horizontal-scroll");
    }

    public void initializeListViewChats() {
        chatsView.getItems().clear();
        chatsView.setFixedCellSize(60);
        chatsView.setCellFactory(lv -> new ListChannelsCellController());
        List<ChannelUser> userChats = channelUserService.getAllChatsByUser(user);
        chatsView.getItems().addAll(userChats);

        chatsView.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue != null) {
                        loadChannelMessages(newValue);
                    }
                });
    }

    private void loadChannelMessages(ChannelUser selectedChat) {
        messagesLV.getItems().clear();

        channelName.setText(selectedChat.getChannel().getChannelName());
        channelStatus.setText(selectedChat.getChannel().getChannelCountUser() == 2 ?
                channelUserService.getInterlocutorStatus(selectedChat.getChannel(), selectedChat.getUser()) :
                String.format("Число участников: %d", selectedChat.getChannel().getChannelCountUser()));

        messageTA.setText("");

        List<Message> messages = messageService.getAllMessageInChannel(selectedChat.getChannel().getChannelID());

        Message previousMessage = null;
        for (Message message : messages) {

            if(previousMessage == null || !previousMessage.getMessageDatetime().toLocalDate()
                    .isEqual(message.getMessageDatetime().toLocalDate())) {
                Label label = new Label(message.getMessageDatetime().toLocalDate()
                        .format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
                label.setStyle("-fx-background-color: rgba(50,50,50,0.5); -fx-text-fill: white;");
                label.setAlignment(Pos.CENTER);
                label.setPrefWidth(selectedChatVB.getWidth());
                selectedChatVB.widthProperty().addListener((observable, oldValue, newValue) -> {
                    label.setMaxWidth(newValue.doubleValue());
                });
                //messagesLV.getItems().add(label);
            }


            previousMessage = message;
        }
        messagesLV.getItems().addAll(FXCollections.observableArrayList(messages));

        //long startTime = System.currentTimeMillis();
        //Заменить на конечный скрол
        //long endTime = System.currentTimeMillis();
        //long duration = endTime - startTime;
        //System.out.println("Время выполнения: " + duration + " мс");
    }

    private void setupMessageTextAreaListener() {
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

            adjustTextAreaHeight(newValue, oldValue);
        });
    }

    private void adjustTextAreaHeight(String newValue, String oldValue) {
        if (countLines(newValue) > countLines(oldValue) && messageAnchor.getHeight() <= 180) {
            messageAnchor.setMinHeight(messageAnchor.getHeight() + 20);
        } else if (countLines(newValue) < countLines(oldValue) && messageAnchor.getHeight() >= 60
                && !checkScrollVisibility(messageTA)) {
            messageAnchor.setMinHeight(messageAnchor.getHeight() - 20);
        }
    }

    private int countLines(String text) {
        if (text == null || text.isEmpty()) {
            return 1;
        }

        int lineCount = 1;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '\n') {
                lineCount++;
            } else if (c == '\r') {
                lineCount++;
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

    @FXML
    private void handleSendMessage() {
        String text = messageTA.getText().trim();
        if (!text.isEmpty() && chatsView.getSelectionModel().getSelectedItem() != null) {
            ChannelUser selectedChat = chatsView.getSelectionModel().getSelectedItem();

            Message newMessage = new Message();
            newMessage.setMessageContent(text.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            newMessage.setChannelUser(selectedChat);

            messageService.save(newMessage);

            loadChannelMessages(selectedChat);

            messageTA.clear();
        }
    }
}