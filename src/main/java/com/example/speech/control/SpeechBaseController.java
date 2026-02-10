package com.example.speech.control;

import com.example.speech.model.ChannelUser;
import com.example.speech.model.Message;
import com.example.speech.model.User;
import com.example.speech.service.ChannelUserService;
import com.example.speech.service.MessageService;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableArray;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.skin.VirtualFlow;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;

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
    @FXML
    private AnchorPane channelStateAP;
    @FXML
    private HBox pinnedMessagesHB, channelStateHB;
    @FXML
    private Label contentPinnedMessageLB;
    @FXML
    private VBox channelStateVB;

    private ContextPopUpBar contextPopUpBar;

    protected enum ContextPopUpBar {
        CHANGE_MESSAGE,
        REPLY_MESSAGE,
        FORWARD_MESSAGE
    }

    private int countLinesOldValue;

    private MessageCellCreator messageCellCreator;

    private Message lastPinnedMessage;

    private List<Message> pinnedMessages;

    private Map<Message, Integer> mapMessageInd = new HashMap<>();

    private ChannelUser selectedChannelUser;

    private int firstVisible;

    private boolean flag = true;

    private List<Message> forwardMessages;

    public void initializeData(Stage stage, User currentUser) {
        this.stage = stage;
        this.currentUser = currentUser;
        messagesLV.getItems().addListener((ListChangeListener<Message>) change -> {
            while (change.next()) {
                if (change.wasAdded()) {
                    int addIndex = change.getFrom();

                    for (Message msg : mapMessageInd.keySet()) {
                        int idx = mapMessageInd.get(msg);
                        if (idx >= addIndex) {
                            mapMessageInd.put(msg, idx + change.getAddedSize());
                        }
                    }

                    for (int i = 0; i < change.getAddedSize(); i++) {
                        Message msg = change.getAddedSubList().get(i);
                        mapMessageInd.put(msg, addIndex + i);
                    }

                    Platform.runLater(() -> {
                        if (!messagesLV.getItems().isEmpty()) {
                            messagesLV.scrollTo(messagesLV.getItems().size());
                        }
                    });
                }

                if (change.wasRemoved()) {
                    int removeIndex = change.getFrom();

                    for (Message msg : change.getRemoved()) {
                        mapMessageInd.remove(msg);
                    }

                    for (Message msg : mapMessageInd.keySet()) {
                        int idx = mapMessageInd.get(msg);
                        if (idx > removeIndex) {
                            mapMessageInd.put(msg, idx - change.getRemovedSize());
                        }
                    }

                    Platform.runLater(() -> {
                        if (!messagesLV.getItems().isEmpty()) {
                            messagesLV.scrollTo(messagesLV.getItems().size());
                        }
                    });
                }
            }
        });
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
                scrollToMessage(updateMessage);
            }
        });

        pinnedMessagesHB.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                scrollToMessage(lastPinnedMessage);
            }
        });

        messagesLV.skinProperty().addListener((obs, oldSkin, newSkin) -> {
            if (newSkin != null) {
                ScrollBar scrollBar = (ScrollBar) messagesLV.lookup(".scroll-bar:vertical");
                if (scrollBar != null) {
                    scrollBar.valueProperty().addListener((observable, oldValue, newValue) -> {
                        try {
                            Field field = newSkin.getClass().getDeclaredField("flow");
                            field.setAccessible(true);
                            VirtualFlow<?> flow = (VirtualFlow<?>) field.get(newSkin);

                            firstVisible = flow.getFirstVisibleCell().getIndex();

                            if(flag)
                                setPinnedMessagesHBVisible(firstVisible);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                }
            }
        });
    }

    public void initializeListViewChats() {
        chatsView.getItems().clear();
        chatsView.setFixedCellSize(60);
        chatsView.setCellFactory(lv -> new ListChannelsCellController());
        List<ChannelUser> userChats = channelUserService.getAllChatsByUser(currentUser);
        chatsView.getItems().addAll(userChats);

        chatsView.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue != null) {
                        selectedChannelUser = newValue;
                        hideTheListOfPinnedMessages();
                        loadChannelMessages(newValue);
                        setPinnedMessagesHBVisible(messagesLV.getItems().size());
                    }
                });
    }

    public void loadChannelMessages(ChannelUser selectedChat) {
        messagesLV.getItems().clear();

        selectedChatVB.setVisible(true);

        channelName.setText(selectedChat.getChannel().getChannelName());
        channelStatus.setText(selectedChat.getChannel().getChannelCountUser() == 2 ?
                channelUserService.getInterlocutorStatus(selectedChat.getChannel(), selectedChat.getUser()) :
                String.format("Число участников: %d", selectedChat.getChannel().getChannelCountUser()));

        messageTA.setText("");

        List<Message> messages = messageService.getAllMessageInChannel(
                selectedChannelUser.getChannel().getChannelID()
        );

        messagesLV.getItems().addAll(FXCollections.observableArrayList(messages).stream()
                .filter(message -> !message.getDeletedByUsers().contains(Long.valueOf(currentUser.getIdUser()))).toList());

        Platform.runLater(() -> {
            if (!messagesLV.getItems().isEmpty()) {
                messagesLV.scrollTo(messagesLV.getItems().size() - 1);
            }
        });
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
                updateMessage = null;
            }
            updateVisibleChangeMessageHB();
        } else if (chatsView.getSelectionModel().getSelectedItem() != null && updateMessageHB.isVisible()
                && contextPopUpBar == ContextPopUpBar.FORWARD_MESSAGE) {
            for(Message message : forwardMessages) {
                Message tempMessage = new Message();
                tempMessage.setMessageDatetime(LocalDateTime.now());
                tempMessage.setMessageContent(message.getMessageContent());
                tempMessage.setChannelUser(chatsView.getSelectionModel().getSelectedItem());
                tempMessage.setMessageStatus("загружается");

                messagesLV.getItems().add(tempMessage);

                Platform.runLater(() -> {
                    messagesLV.scrollTo(messagesLV.getItems().size() - 1);
                });

                new Thread(() -> {
                    try {
                        Message messageToSave = new Message();
                        messageToSave.setMessageContent(message.getMessageContent());
                        messageToSave.setChannelUser(chatsView.getSelectionModel().getSelectedItem());
                        messageToSave.setForwardedFrom(Long.valueOf(message.getChannelUser().getUser().getIdUser()));
                        MessageService messageService = new MessageService();
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
                    } catch (Exception e) {
                        e.printStackTrace();
                        Platform.runLater(() -> {
                            tempMessage.setMessageStatus("ошибка отправки");
                            messagesLV.refresh();
                        });
                    }
                }).start();
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
        updateMessage = null;
        updateMessageHB.setVisible(false);
        updateMessageHB.setManaged(false);
        if(contextPopUpBar == ContextPopUpBar.CHANGE_MESSAGE)
            messageTA.setText("");
    }

    public void setPinnedMessagesHBVisible(int firstVisibleIndex) {
        // Получаем ВСЕ закрепленные сообщения
        List<Message> allPinnedMessages = messagesLV.getItems().stream()
                .filter(mes -> mes != null && Boolean.TRUE.equals(mes.getPinMessage()))
                .sorted((m1, m2) -> m2.getMessageDatetime().compareTo(m1.getMessageDatetime()))
                .toList();

        if (allPinnedMessages.isEmpty()) {
            // Нет закрепленных сообщений - скрываем панель
            if (pinnedMessagesHB.isVisible()) {
                pinnedMessagesHB.setVisible(false);
                pinnedMessagesHB.setManaged(false);
            }
            lastPinnedMessage = null;
            return;
        }

        // Находим ПЕРВОЕ закрепленное сообщение, которое НЕ видно (его индекс >= firstVisibleIndex)
        // Или последнее закрепленное сообщение, если все видно
        Message nextPinnedMessage = null;

        for (Message pinnedMsg : allPinnedMessages) {
            Integer msgIndex = mapMessageInd.get(pinnedMsg);
            if (msgIndex != null && msgIndex < firstVisibleIndex) {
                // Нашли закрепленное сообщение, которое еще не видно (или только стало видно)
                nextPinnedMessage = pinnedMsg;
                break;
            }
        }

        lastPinnedMessage = (nextPinnedMessage == null) ? allPinnedMessages.getFirst() : nextPinnedMessage;
        pinnedMessagesHB.setVisible(true);
        pinnedMessagesHB.setManaged(true);
        String messageText = new String(lastPinnedMessage.getMessageContent(), StandardCharsets.UTF_8);
        if (messageText.length() > 50) {
            messageText = messageText.substring(0, 47) + "...";
        }
        contentPinnedMessageLB.setText(messageText);
    }

    private void scrollToMessage(Message message) {
        Platform.runLater(() -> {
            messagesLV.scrollTo(message);
            PauseTransition pause = new PauseTransition(Duration.millis(100));
            pause.setOnFinished(e -> {
                messageCellCreator.getControllerCache(message).highlightMessageTemporarily();
            });
            pause.play();
        });
    }

    @FXML
    private void showPinnedContent() {
        flag = false;

        List<Message> allPinnedMessages = messagesLV.getItems().stream()
                .filter(mes -> mes != null && Boolean.TRUE.equals(mes.getPinMessage()))
                .toList();

        for(Node node : channelStateAP.getChildren()) {
            node.setVisible(false);
            node.setManaged(false);
        }

        Button backBtn = new Button();
        backBtn.setId("backBtn");
        backBtn.getStyleClass().addAll("window-control-button", "back-button");
        backBtn.setOnAction(e -> {
            hideTheListOfPinnedMessages();
        });
        backBtn.setPrefWidth(50.0);
        AnchorPane.setLeftAnchor(backBtn, 10.0);
        AnchorPane.setTopAnchor(backBtn, 5.0);
        AnchorPane.setBottomAnchor(backBtn, 5.0);
        channelStateAP.getChildren().add(backBtn);

        Label countPinnedMessages = new Label(String.format("%d закреплённых сообщений", allPinnedMessages.size()));
        countPinnedMessages.setId("countPinnedMessages");
        countPinnedMessages.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        AnchorPane.setLeftAnchor(countPinnedMessages, 70.0);
        AnchorPane.setRightAnchor(countPinnedMessages, 0.0);
        AnchorPane.setTopAnchor(countPinnedMessages, 0.0);
        AnchorPane.setBottomAnchor(countPinnedMessages, 0.0);

        channelStateAP.getChildren().add(countPinnedMessages);

        if(pinnedMessagesHB.isVisible()) {
            pinnedMessagesHB.setVisible(false);
            pinnedMessagesHB.setManaged(false);
        }

        messagesLV.getItems().clear();
        messagesLV.getItems().addAll(allPinnedMessages);

        if(updateMessageHB.isVisible()) {
            updateMessageHB.setVisible(false);
            updateMessageHB.setManaged(false);
        }

        for(Node node : messageAnchor.getChildren()) {
            node.setVisible(false);
            node.setManaged(false);
        }

        Button unpinnedAllMessagesBtn = new Button("ОТКРЕПИТЬ " + allPinnedMessages.size() + " СООБЩЕНИЙ");
        unpinnedAllMessagesBtn.setId("unpinnedAllMessagesBtn");
        unpinnedAllMessagesBtn.setStyle("-fx-font-size: 16px; -fx-text-fill: blue;");
        AnchorPane.setLeftAnchor(unpinnedAllMessagesBtn, 0.0);
        AnchorPane.setRightAnchor(unpinnedAllMessagesBtn, 0.0);
        AnchorPane.setTopAnchor(unpinnedAllMessagesBtn, 0.0);
        AnchorPane.setBottomAnchor(unpinnedAllMessagesBtn, 0.0);
        unpinnedAllMessagesBtn.setOnAction(e -> {
            new ConfirmationOfMessageDeletion().initializeShape(this);
        });

        messageAnchor.getChildren().add(unpinnedAllMessagesBtn);
    }

    public void hideTheListOfPinnedMessages() {
        flag = true;
        ObservableList<Message> allMessages = FXCollections.observableArrayList(messageService.getAllMessageInChannel(
                        selectedChannelUser.getChannel().getChannelID()).stream()
                .filter(message -> !message.getDeletedByUsers().contains(Long.valueOf(currentUser.getIdUser())))
                .toList());

        List<Node> toRemove = channelStateAP.getChildren().stream()
                .filter(node -> node.getId() != null &&(node.getId().equals("backBtn")
                        || node.getId().equals("countPinnedMessages"))).toList();
        channelStateAP.getChildren().removeAll(toRemove);

        for(Node node : channelStateAP.getChildren()) {
            node.setVisible(true);
            node.setManaged(true);
        }

        if(messageTA.isVisible())
            setPinnedMessagesHBVisible(firstVisible);

        messagesLV.getItems().clear();
        messagesLV.getItems().addAll(allMessages);

        if(updateMessage != null) {
            updateMessageHB.setVisible(true);
            updateMessageHB.setManaged(true);
        }

        List<Node> toRemove2 = messageAnchor.getChildren().stream()
                .filter(node -> node.getId() != null && node.getId().equals("unpinnedAllMessagesBtn")).toList();
        messageAnchor.getChildren().removeAll(toRemove2);

        for(Node node : messageAnchor.getChildren()) {
            node.setVisible(true);
            node.setManaged(true);
        }
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

    public Message getLastPinnedMessage() {
        return lastPinnedMessage;
    }

    public void setLastPinnedMessage(Message lastPinnedMessage) {
        this.lastPinnedMessage = lastPinnedMessage;
    }

    public ChannelUser getSelectedChannelUser() {
        return selectedChannelUser;
    }

    public void setSelectedChannelUser(ChannelUser selectedChannelUser) {
        this.selectedChannelUser = selectedChannelUser;
    }

    public void setForwardMessages(List<Message> forwardMessages) {
        this.forwardMessages = forwardMessages;
    }
}