package com.example.speech.control;

import com.example.speech.model.ChannelUser;
import com.example.speech.model.Message;
import com.example.speech.model.User;
import com.example.speech.service.ChannelUserService;
import com.example.speech.service.MessageService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static com.example.speech.util.HelpfulStylingClass.setupFullScreenListener;

public class SpeechBaseController {
    private Stage stage;
    private User currentUser;

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
    @FXML
    private StackPane messagesSP;
    @FXML
    private VBox leftVB;
    @FXML
    private HBox updateMessageHB;
    @FXML
    private Label contentUpdateMessageLB;

    private Message updateMessage;

    private Long messageIdReplyTo;

    @FXML
    private StackPane textAreaSP;
    @FXML
    private Label promptTextTA;
    @FXML
    private ImageView HintIV;
    @FXML
    private Label HintLB;

    private ContextPopUpBar contextPopUpBar;

    protected enum ContextPopUpBar {
        CHANGE_MESSAGE,
        REPLY_MESSAGE
    }

    private int countLinesOldValue;

    private MessageCellCreator messageCellCreator;

    public void initializeData(Stage stage, User currentUser) {
        this.stage = stage;
        this.currentUser = currentUser;
        setupFullScreenListener(stage, rootAnchorPane);
        initializeListViewChats();
        setupMessageTextAreaListener();
        messagesSP.widthProperty().addListener((ch, oldValue, newValue) -> {
            double newWidth = newValue.doubleValue() - 300;

            if (newWidth <= 400) {
                AnchorPane.setLeftAnchor(selectedChatVB, 5.0);
                leftVB.setVisible(false);
                chatsView.setVisible(false);
            }
            if (newWidth >= 400) {
                AnchorPane.setLeftAnchor(selectedChatVB, 300.0);
                leftVB.setVisible(true);
                chatsView.setVisible(true);
            }

            messagesLV.setPrefWidth(newWidth);
        });
        messagesLV.setSelectionModel(null);
        messageCellCreator = new MessageCellCreator(this);
        messagesLV.setCellFactory(messageCellCreator);
        messagesLV.getStyleClass().add("no-horizontal-scroll");
        stackPaneListener();

        textAreaSP.widthProperty().addListener((ch, oldValue, newValue) -> {
            promptTextTA.setPrefWidth(newValue.doubleValue());
        });

        messageTA.focusedProperty().addListener((ch, oldValue, newValue) -> {
            String text = messageTA.getText();
            if(!newValue && (text == null || text.isEmpty()))
                textAreaSP.getChildren().add(promptTextTA);
            else
                textAreaSP.getChildren().remove(promptTextTA);
        });

        updateMessageHB.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                messageCellCreator.getControllerCache(updateMessage).highlightMessageTemporarily();
            }
        });
    }

    public void initializeListViewChats() {
        chatsView.getItems().clear();
        chatsView.setFixedCellSize(60);
        chatsView.setCellFactory(lv -> new ListChannelsCellController());
        List<ChannelUser> userChats = channelUserService.getAllChatsByUser(currentUser);
        chatsView.getItems().addAll(userChats);

        Platform.runLater(() -> {
            if (!messagesLV.getItems().isEmpty()) {
                messagesLV.scrollTo(messagesLV.getItems().size() - 1);
            }
        });

        messagesLV.getItems().addListener(new ListChangeListener<Message>() {
            @Override
            public void onChanged(Change<? extends Message> change) {
                while (change.next()) {
                    if (change.wasAdded() || change.wasRemoved()) {
                        Platform.runLater(() -> {
                            if (!messagesLV.getItems().isEmpty()) {
                                messagesLV.scrollTo(messagesLV.getItems().size());
                            }
                        });
                    }
                }
            }
        });

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

        List<Message> messages = messageService.getAllMessageInChannel(
                selectedChat.getChannel().getChannelID()
        );

        messagesLV.getItems().addAll(FXCollections.observableArrayList(messages).stream()
                .filter(message -> !message.getDeletedByUsers().contains(Long.valueOf(currentUser.getIdUser()))).toList());
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
                AnchorPane.setRightAnchor(textAreaSP, 51.0);
            } else {
                AnchorPane.setRightAnchor(textAreaSP, 102.0);
                AnchorPane.setRightAnchor(emojiVB, 50.0);
                sendVB.setVisible(true);
            }
            messageTA.setPrefWidth(textAreaSP.getWidth());

            adjustTextAreaHeight(newValue);
        });
    }

    private void adjustTextAreaHeight(String newValue) {
        int countLinesNewValue = countTextAreaLines(messageTA);

        int setMinHeightByCountLines = countLinesNewValue * 20;

        if (countLinesNewValue > countLinesOldValue)
            messageAnchor.setMinHeight(Math.min(setMinHeightByCountLines, 200));
        if (countLinesNewValue < countLinesOldValue)
            messageAnchor.setMinHeight(Math.min(Math.max(setMinHeightByCountLines, 40), 200));

        if (newValue == null || newValue.trim().isEmpty()) {
            messageAnchor.setMinHeight(40);
            messageAnchor.setPrefHeight(40);
            messageTA.setPrefHeight(40);
        }
        countLinesOldValue = countLinesNewValue;
    }

    public int countTextAreaLines(TextArea original) {
        String text = original.getText();
        if (text == null || text.trim().isEmpty()) {
            return 1;
        }

        double textHeight = getTextHeightFromTextArea(original);

        return (int) Math.max(1, Math.round(textHeight / 20)) + 1;
    }

    private double getTextHeightFromTextArea(TextArea textArea) {
        Text textNode = (Text) textArea.lookup(".text");
        if (textNode != null) {
            return textNode.getLayoutBounds().getHeight();
        }
        return 20;
    }

    @FXML
    private void handleSendMessage() {
        String text = messageTA.getText().trim();
        if (!text.isEmpty() && chatsView.getSelectionModel().getSelectedItem() != null && (!updateMessageHB.isVisible()
                || contextPopUpBar == ContextPopUpBar.REPLY_MESSAGE)) {
            ChannelUser selectedChat = chatsView.getSelectionModel().getSelectedItem();

            Message tempMessage  = new Message();
            tempMessage.setMessageDatetime(LocalDateTime.now());
            tempMessage.setMessageContent(text.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            tempMessage.setChannelUser(selectedChat);
            tempMessage.setMessageStatus("загружается");

            messagesLV.getItems().add(tempMessage);

            Platform.runLater(() -> {
                messagesLV.scrollTo(messagesLV.getItems().size() - 1);
            });

            messageTA.setText("");

            new Thread(() -> {
                try {
                    Message messageToSave = new Message();
                    messageToSave.setMessageContent(text.getBytes(StandardCharsets.UTF_8));
                    messageToSave.setChannelUser(selectedChat);
                    if(updateMessageHB.isVisible() && contextPopUpBar == ContextPopUpBar.REPLY_MESSAGE)
                        messageToSave.setMessageIdReplyTo(messageIdReplyTo);
                    boolean saved = messageService.save(messageToSave);
                    if (saved) {
                        Message savedMessage = messageService.getRowById(messageToSave.getMessageId());
                        Platform.runLater(() -> {
                            int index = messagesLV.getItems().indexOf(tempMessage);
                            if (index >= 0) {
                                messagesLV.getItems().set(index, savedMessage);
                                messagesLV.refresh();
                            }
                        });
                    }
                    updateVisibleChangeMessageHB();
                } catch (Exception e) {
                    e.printStackTrace();
                    Platform.runLater(() -> {
                        //Обработать неудачную отправку
                        tempMessage.setMessageStatus("ошибка отправки");
                        messagesLV.refresh();
                    });
                }
            }).start();
        } else if (!text.isEmpty() && chatsView.getSelectionModel().getSelectedItem() != null && updateMessageHB.isVisible()
                && contextPopUpBar == ContextPopUpBar.CHANGE_MESSAGE) {
            if (!text.equals(new String(updateMessage.getMessageContent(), StandardCharsets.UTF_8))) {
                updateMessage.setMessageContent(text.getBytes());
                updateMessage.setModifiedMessage(true);
                messageService.update(updateMessage);
                messagesLV.refresh();
            }
            updateVisibleChangeMessageHB();
        }
    }

    private void stackPaneListener() {
        messagesSP.setOnMouseClicked(event -> {
            messagesSP.getChildren().removeIf(node -> node instanceof WorkingWithAMessageListController);
        });
    }

    @FXML
    private void updateVisibleChangeMessageHB() {
        updateMessageHB.setVisible(false);
        updateMessageHB.setManaged(false);
        if(contextPopUpBar == ContextPopUpBar.CHANGE_MESSAGE)
            messageTA.setText("");
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public MessageService getMessageService() {
        return messageService;
    }

    public TextArea getMessageTA() {
        return messageTA;
    }

    public StackPane getMessagesSP() {
        return messagesSP;
    }

    public ListView<Message> getMessagesLV() {
        return messagesLV;
    }

    public Stage getStage() {
        return stage;
    }

    public ListView<ChannelUser> getChatsView() {
        return chatsView;
    }

    public ChannelUserService getChannelUserService() {
        return channelUserService;
    }

    public Label getChannelName() {
        return channelName;
    }

    public Label getChannelStatus() {
        return channelStatus;
    }

    public AnchorPane getRootAnchorPane() {
        return rootAnchorPane;
    }

    public VBox getSelectedChatVB() {
        return selectedChatVB;
    }

    public VBox getEmojiVB() {
        return emojiVB;
    }

    public VBox getSendVB() {
        return sendVB;
    }

    public AnchorPane getMessageAnchor() {
        return messageAnchor;
    }

    public VBox getLeftVB() {
        return leftVB;
    }

    public HBox getUpdateMessageHB() {
        return updateMessageHB;
    }

    public Label getContentUpdateMessageLB() {
        return contentUpdateMessageLB;
    }

    public Message getUpdateMessage() {
        return updateMessage;
    }

    public void setUpdateMessage(Message updateMessage) {
        this.updateMessage = updateMessage;
    }

    public ContextPopUpBar getContextPopUpBar() {
        return contextPopUpBar;
    }

    public void setContextPopUpBar(ContextPopUpBar contextPopUpBar) {
        this.contextPopUpBar = contextPopUpBar;
    }

    public ImageView getHintIV() {
        return HintIV;
    }

    public Label getHintLB() {
        return HintLB;
    }

    public Long getMessageIdReplyTo() {
        return messageIdReplyTo;
    }

    public void setMessageIdReplyTo(Long messageIdReplyTo) {
        this.messageIdReplyTo = messageIdReplyTo;
    }

    public MessageCellCreator getMessageCellCreator() {
        return messageCellCreator;
    }
}