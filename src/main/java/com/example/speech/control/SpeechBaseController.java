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
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.skin.VirtualFlow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;

import javafx.scene.control.IndexedCell;

import static com.example.speech.util.HelpfulStylingClass.applyPromptWithTF;
import static com.example.speech.util.HelpfulStylingClass.setupFullScreenListener;

public class SpeechBaseController {
    private Stage stage;
    private User currentUser;

    @FXML
    private ListView<ChannelUser> chatsView;

    private final ChannelUserService channelUserService = new ChannelUserService();
    private final MessageService messageService = new MessageService();

    private TextField searchFromChatTF;

    private FilteredList<Message> filteredList;

    private List<Node> hiddenList = new ArrayList<>();

    private boolean searchModeActive = false;

    private List<Message> resultList;

    private int currInd = 0;

    @FXML
    private StackPane rightSP;

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

    private ChannelUser selectedChannelUser;

    private int firstVisible;

    private boolean flag = true;

    private ObservableList<Message> forwardMessages = FXCollections.observableArrayList();

    private boolean selectionModeActive = false;
    private List<Message> selectedMessages = new ArrayList<>();

    private Button selectionForwardBtn;
    private Button selectionDeleteBtn;
    private Button selectionCancelBtn;

    private boolean dragSelecting = false;
    private int dragStartIndex = -1;
    private double dragStartX, dragStartY;
    private static final double DRAG_DISTANCE_THRESHOLD = 5.0; // пикселей

    public boolean isDragSelecting() {
        return dragSelecting;
    }

    private List<Message> baseSelection = new ArrayList<>();

    private VirtualFlow<?> currentFlow;

    private int lastProcessedDragIndex = -1;
    private boolean dragStartSelected = false;

    private ObservableList<Message> messages = FXCollections.observableArrayList();

    public void initializeData(Stage stage, User currentUser) {
        this.stage = stage;
        this.currentUser = currentUser;
        messages.addListener((ListChangeListener<Message>) change -> {
            while (change.next()) {
                if (change.wasAdded() || change.wasRemoved()) {
                    Platform.runLater(() -> {
                        if (!messagesLV.getItems().isEmpty()) {
                            messagesLV.scrollTo(messagesLV.getItems().size());
                        }
                    });
                }
            }
        });

        filteredList = new FilteredList<>(messages, m -> true);

        messagesLV.setItems(filteredList);
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
            if (!newValue && (text == null || text.isEmpty()))
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
                try {
                    Field field = newSkin.getClass().getDeclaredField("flow");
                    field.setAccessible(true);
                    currentFlow = (VirtualFlow<?>) field.get(newSkin);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                ScrollBar scrollBar = (ScrollBar) messagesLV.lookup(".scroll-bar:vertical");
                if (scrollBar != null) {
                    scrollBar.valueProperty().addListener((observable, oldValue, newValue) -> {
                        firstVisible = currentFlow.getFirstVisibleCell().getIndex();
                        if (flag && !searchModeActive) setPinnedMessagesHBVisible(firstVisible);
                    });
                }
            }
        });

        messagesLV.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ESCAPE && isSelectionModeActive()) {
                setSelectionModeActive(false);
            }
        });

        messagesLV.setOnMousePressed(event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                Message clickedMsg = findMessageAt(event.getScreenX(), event.getScreenY());
                if (clickedMsg != null) {
                    dragStartIndex = messagesLV.getItems().indexOf(clickedMsg);
                    dragStartSelected = selectedMessages.contains(messagesLV.getItems().get(dragStartIndex));
                    dragStartX = event.getScreenX();
                    dragStartY = event.getScreenY();
                    dragSelecting = false;
                    lastProcessedDragIndex = -1;
                } else {
                    dragStartIndex = -1;
                }
                event.consume();
            }
        });

        messagesLV.setOnMouseDragged(event -> {
            if (event.getButton() == MouseButton.PRIMARY && dragStartIndex != -1) {
                double distance = Math.hypot(event.getScreenX() - dragStartX, event.getScreenY() - dragStartY);
                if (!dragSelecting && distance > DRAG_DISTANCE_THRESHOLD) {
                    dragSelecting = true;
                    // Сохраняем текущее выделение как базовое
                    baseSelection = new ArrayList<>(selectedMessages);
                    // Устанавливаем начальный диапазон (только стартовый элемент)
                    updateDragSelection(dragStartIndex, dragStartIndex);
                    if (!selectionModeActive) {
                        setSelectionModeActive(true);
                    }
                }
                if (dragSelecting) {
                    IndexedCell<Message> cell = findCellAt(event.getScreenX(), event.getScreenY());
                    if (cell != null) {
                        int currentIndex = cell.getIndex();
                        if (currentIndex != lastProcessedDragIndex) {
                            updateDragSelection(dragStartIndex, currentIndex);
                            lastProcessedDragIndex = currentIndex;
                        }
                    }
                }
                event.consume();
            }
        });

        messagesLV.setOnMouseReleased(event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                if (!dragSelecting && dragStartIndex != -1) {
                    Message clickedMsg = messagesLV.getItems().get(dragStartIndex);
                    if (selectionModeActive)
                        toggleMessageSelection(clickedMsg);
                }
                dragSelecting = false;
                dragStartIndex = -1;
                lastProcessedDragIndex = -1;
                baseSelection.clear(); // очищаем после завершения
                if (selectedMessages.isEmpty())
                    setSelectionModeActive(false);
                event.consume();
            }
        });
    }

    public void initializeListViewChats() {
        chatsView.setFixedCellSize(60);
        chatsView.setCellFactory(lv -> new ListChannelsCellController());
        ObservableList<ChannelUser> userChats = FXCollections.observableArrayList(channelUserService.getAllChatsByUser(currentUser));
        chatsView.getItems().setAll(userChats);

        chatsView.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue != null) {
                        selectedChannelUser = newValue;
                        hideTheListOfPinnedMessages();
                        loadChannelMessages(newValue);
                        setPinnedMessagesHBVisible(messagesLV.getItems().size());
                        setSelectionModeActive(false);
                    }
                });
    }

    public void loadChannelMessages(ChannelUser selectedChat) {

        selectedChatVB.setVisible(true);

        channelName.setText(selectedChat.getChannel().getChannelName());
        channelStatus.setText(selectedChat.getChannel().getChannelCountUser() == 2 ?
                channelUserService.getInterlocutorStatus(selectedChat.getChannel(), selectedChat.getUser()) :
                String.format("Число участников: %d", selectedChat.getChannel().getChannelCountUser()));

        messageTA.setText("");

        List<Message> messagesList = messageService.getAllMessageInChannel(
                selectedChannelUser.getChannel().getChannelID()
        );

        messages.setAll(messagesList.stream().filter(message ->
                !message.getDeletedByUsers().contains(Long.valueOf(currentUser.getIdUser()))).toList());

        Platform.runLater(() -> {
            if (!messagesLV.getItems().isEmpty()) {
                messagesLV.scrollTo(messagesLV.getItems().size());
            }
        });

        messageCellCreator.clearCache();
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

            Message tempMessage = new Message();
            tempMessage.setMessageDatetime(LocalDateTime.now());
            tempMessage.setMessageContent(text.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            tempMessage.setChannelUser(selectedChat);
            tempMessage.setMessageStatus("загружается");

            messagesLV.getItems().add(tempMessage);

            Platform.runLater(() -> {
                messagesLV.scrollTo(messagesLV.getItems().size());
            });

            messageTA.setText("");

            new Thread(() -> {
                try {
                    Message messageToSave = new Message();
                    messageToSave.setMessageContent(text.getBytes(StandardCharsets.UTF_8));
                    messageToSave.setChannelUser(selectedChat);
                    if (updateMessageHB.isVisible() && contextPopUpBar == ContextPopUpBar.REPLY_MESSAGE)
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
        } else if (chatsView.getSelectionModel().getSelectedItem() != null
                && updateMessageHB.isVisible()
                && contextPopUpBar == ContextPopUpBar.FORWARD_MESSAGE) {

            // Список временных сообщений, которые уже показаны в UI
            List<Message> tempMessages = new ArrayList<>();

            for (Message original : forwardMessages) {
                Message temp = new Message();
                temp.setMessageDatetime(LocalDateTime.now());
                temp.setMessageContent(original.getMessageContent());
                temp.setChannelUser(selectedChannelUser);
                temp.setMessageStatus("загружается");
                temp.setForwardedFrom(Long.valueOf(original.getChannelUser().getUser().getIdUser()));

                messagesLV.getItems().add(temp);
                tempMessages.add(temp); // запоминаем ссылку на временное сообщение
            }

            // Прокрутка вниз после добавления всех временных сообщений
            Platform.runLater(() ->
                    messagesLV.scrollTo(messagesLV.getItems().size())
            );

            new Thread(() -> {
                for (int i = 0; i < forwardMessages.size(); i++) {
                    Message original = forwardMessages.get(i);
                    Message tempMsg = tempMessages.get(i);

                    try {
                        // ✅ 1. Создаём НОВЫЙ объект для вставки в БД
                        Message newMsg = new Message();
                        newMsg.setMessageContent(original.getMessageContent());
                        newMsg.setChannelUser(selectedChannelUser);
                        newMsg.setMessageDatetime(LocalDateTime.now());
                        newMsg.setMessageStatus("отправлено");

                        // ✅ 2. Устанавливаем поле forwardedFrom (ID отправителя оригинала)
                        newMsg.setForwardedFrom(Long.valueOf(original.getChannelUser().getUser().getIdUser()));

                        // ✅ 3. Сохраняем новый объект
                        MessageService service = new MessageService();
                        boolean saved = service.save(newMsg);

                        if (saved) {
                            // Получаем сохранённую сущность с присвоенным ID
                            Message savedMsg = service.getRowById(newMsg.getMessageId());
                            final Message finalSaved = savedMsg;

                            Platform.runLater(() -> {
                                int idx = messagesLV.getItems().indexOf(tempMsg);
                                if (idx >= 0) {
                                    // Заменяем временное сообщение на сохранённое
                                    messagesLV.getItems().set(idx, finalSaved);
                                    messagesLV.refresh();
                                }
                            });
                        } else {
                            // Ошибка сохранения
                            Platform.runLater(() -> {
                                tempMsg.setMessageStatus("ошибка отправки");
                                messagesLV.refresh();
                            });
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Platform.runLater(() -> {
                            tempMsg.setMessageStatus("ошибка отправки");
                            messagesLV.refresh();
                        });
                    }
                }
            }).start();

            // Скрываем панель пересылки
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
        if (contextPopUpBar == ContextPopUpBar.CHANGE_MESSAGE)
            messageTA.setText("");
    }

    public void setPinnedMessagesHBVisible(int firstVisibleIndex) {
        // Получаем ВСЕ закрепленные сообщения
        List<Message> allPinnedMessages = messagesLV.getItems().stream()
                .filter(mes -> mes != null && mes.getPinMessage())
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
            int msgIndex = messagesLV.getItems().indexOf(pinnedMsg);
            if (msgIndex < firstVisibleIndex) {
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
            if (isMessageFullyVisible(message)) {
                messageCellCreator.getControllerCache(message).highlightMessageTemporarily();
            } else {
                messagesLV.scrollTo(message);
                PauseTransition pause = new PauseTransition(Duration.millis(100));
                pause.setOnFinished(e ->
                        messageCellCreator.getControllerCache(message).highlightMessageTemporarily()
                );
                pause.play();
            }
        });
    }

    private boolean isMessageFullyVisible(Message message) {
        int index = messagesLV.getItems().indexOf(message);
        if (index < 0) return false;

        VirtualFlow<?> flow = (VirtualFlow<?>) messagesLV.lookup(".virtual-flow");
        if (flow == null) return false;

        ListCell<?> cell = (ListCell<?>) flow.getCell(index);
        if (cell == null) return false;

        Bounds cellBoundsInLV = messagesLV.sceneToLocal(
                cell.localToScene(cell.getBoundsInLocal())
        );

        double viewportHeight = messagesLV.getHeight();
        return cellBoundsInLV.getMinY() >= 0 && cellBoundsInLV.getMaxY() <= viewportHeight;
    }

    @FXML
    private void showPinnedContent() {
        flag = false;

        List<Message> allPinnedMessages = messagesLV.getItems().stream()
                .filter(mes -> mes != null && Boolean.TRUE.equals(mes.getPinMessage()))
                .toList();

        for (Node node : channelStateAP.getChildren()) {
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

        if (pinnedMessagesHB.isVisible()) {
            pinnedMessagesHB.setVisible(false);
            pinnedMessagesHB.setManaged(false);
        }

        messagesLV.getItems().clear();
        messagesLV.getItems().addAll(allPinnedMessages);

        if (updateMessageHB.isVisible()) {
            updateMessageHB.setVisible(false);
            updateMessageHB.setManaged(false);
        }

        for (Node node : messageAnchor.getChildren()) {
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
                .filter(node -> node.getId() != null && (node.getId().equals("backBtn")
                        || node.getId().equals("countPinnedMessages"))).toList();
        channelStateAP.getChildren().removeAll(toRemove);

        for (Node node : channelStateAP.getChildren()) {
            node.setVisible(true);
            node.setManaged(true);
        }

        if (messageTA.isVisible())
            setPinnedMessagesHBVisible(firstVisible);

        messages.setAll(allMessages);

        if (updateMessage != null) {
            updateMessageHB.setVisible(true);
            updateMessageHB.setManaged(true);
        }

        List<Node> toRemove2 = messageAnchor.getChildren().stream()
                .filter(node -> node.getId() != null && node.getId().equals("unpinnedAllMessagesBtn")).toList();
        messageAnchor.getChildren().removeAll(toRemove2);

        for (Node node : messageAnchor.getChildren()) {
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

    public void setForwardMessages(ObservableList<Message> forwardMessages) {
        this.forwardMessages = forwardMessages;
    }

    public boolean isSelectionModeActive() {
        return selectionModeActive;
    }

    public void setSelectionModeActive(boolean active) {
        if (this.selectionModeActive == active) return;
        this.selectionModeActive = active;
        if (!active) {
            selectedMessages.clear();
        }
        updateAllMessageCellsSelectionMode();
        checkSelectedMode(active);
    }

    public void toggleMessageSelection(Message message) {
        if (!selectionModeActive) return;
        if (selectedMessages.contains(message)) {
            selectedMessages.remove(message);
        } else {
            selectedMessages.add(message);
        }

        TextMessageCellController controller = messageCellCreator.getControllerCache().get(message);
        if (controller != null) {
            controller.setSelected(selectedMessages.contains(message));
        }
    }

    public boolean isMessageSelected(Message message) {
        return selectedMessages.contains(message);
    }

    public void clearSelection() {
        selectedMessages.clear();
        updateAllMessageCellsSelection();
    }

    // Update all cached cells with current selection mode and selected state
    private void updateAllMessageCellsSelectionMode() {
        MessageCellCreator creator = getMessageCellCreator(); // assume getter exists
        for (Map.Entry<Message, TextMessageCellController> entry :
                creator.getControllerCache().entrySet()) {
            TextMessageCellController controller = entry.getValue();
            controller.setSelectionModeActive(selectionModeActive);
            if (selectionModeActive) {
                controller.setSelected(selectedMessages.contains(entry.getKey()));
            } else {
                controller.setSelected(false);
            }
        }
    }

    // Update a single cell's selection state
    private void updateMessageCellSelection(Message message) {
        TextMessageCellController controller =
                getMessageCellCreator().getControllerCache().get(message);
        if (controller != null) {
            controller.setSelected(selectedMessages.contains(message));
        }
    }

    // Update only the selected state of all cells (mode unchanged)
    private void updateAllMessageCellsSelection() {
        for (Map.Entry<Message, TextMessageCellController> entry :
                getMessageCellCreator().getControllerCache().entrySet()) {
            entry.getValue().setSelected(selectedMessages.contains(entry.getKey()));
        }
    }

    private void checkSelectedMode(boolean active) {
        if (active) {
            for (Node node : channelStateAP.getChildren()) {
                node.setVisible(false);
                node.setManaged(false);
            }

            selectionForwardBtn = new Button("ПЕРЕСЛАТЬ");
            selectionForwardBtn.getStyleClass().add("login-button");
            selectionForwardBtn.setOnAction(e -> {
                System.out.println(selectedMessages.size());
                System.out.println(forwardMessages.size());
                forwardMessages.setAll(selectedMessages);
                System.out.println(selectedMessages.size());
                System.out.println(forwardMessages.size());
                new ChatSelectionController(this, forwardMessages);
                setSelectionModeActive(false);
            });
            selectionForwardBtn.setPrefWidth(120);
            selectionForwardBtn.setPrefHeight(20);
            AnchorPane.setLeftAnchor(selectionForwardBtn, 10.0);
            AnchorPane.setTopAnchor(selectionForwardBtn, 10.0);
            AnchorPane.setBottomAnchor(selectionForwardBtn, 10.0);

            selectionDeleteBtn = new Button("УДАЛИТЬ");
            selectionDeleteBtn.getStyleClass().add("login-button");
            selectionDeleteBtn.setOnAction(e -> {
                new ConfirmationOfMessageDeletion().initializeShape(channelName.getText(), this
                        , selectedMessages);
            });
            selectionDeleteBtn.setPrefWidth(120);
            selectionDeleteBtn.setPrefHeight(20);
            AnchorPane.setLeftAnchor(selectionDeleteBtn, 140.0);
            AnchorPane.setTopAnchor(selectionDeleteBtn, 10.0);
            AnchorPane.setBottomAnchor(selectionDeleteBtn, 10.0);

            selectionCancelBtn = new Button("ОТМЕНА");
            selectionCancelBtn.getStyleClass().add("login-button");
            selectionCancelBtn.setOnAction(e -> {
                setSelectionModeActive(false);
            });
            selectionCancelBtn.setPrefWidth(120);
            selectionCancelBtn.setPrefHeight(20);
            AnchorPane.setRightAnchor(selectionCancelBtn, 10.0);
            AnchorPane.setTopAnchor(selectionCancelBtn, 10.0);
            AnchorPane.setBottomAnchor(selectionCancelBtn, 10.0);
            channelStateAP.getChildren().addAll(selectionForwardBtn, selectionDeleteBtn, selectionCancelBtn);
        } else {
            channelStateAP.getChildren().removeAll(selectionForwardBtn, selectionDeleteBtn, selectionCancelBtn);
            for (Node node : channelStateAP.getChildren()) {
                node.setVisible(true);
                node.setManaged(true);
            }
        }
    }

    private Message findMessageAt(double screenX, double screenY) {
        for (Map.Entry<Message, TextMessageCellController> entry :
                messageCellCreator.getControllerCache().entrySet()) {
            if (entry.getValue().isHit(screenX, screenY)) {
                return entry.getKey();
            }
        }
        return null;
    }

    private void selectRangeReplace(int start, int end) {
        int low = Math.min(start, end);
        int high = Math.max(start, end);
        List<Message> items = messagesLV.getItems();
        selectedMessages.clear();
        for (int i = low; i <= high; i++) {
            selectedMessages.add(items.get(i));
        }
        updateVisibleCellsSelection();
    }

    public List<Message> getSelectedMessages() {
        return selectedMessages;
    }

    private IndexedCell<Message> findCellAt(double screenX, double screenY) {
        if (currentFlow == null) return null;
        Point2D localPoint = messagesLV.screenToLocal(screenX, screenY);
        if (localPoint == null) return null;
        int first = currentFlow.getFirstVisibleCell().getIndex();
        int last = currentFlow.getLastVisibleCell().getIndex();
        for (int i = first; i <= last; i++) {
            IndexedCell<?> cell = currentFlow.getCell(i);
            if (cell != null && cell.getBoundsInParent().contains(localPoint)) {
                return (IndexedCell<Message>) cell;
            }
        }
        return null;
    }

    private void updateVisibleCellsSelection() {
        if (currentFlow == null) return;
        int first = currentFlow.getFirstVisibleCell().getIndex();
        int last = currentFlow.getLastVisibleCell().getIndex();
        for (int i = first; i <= last; i++) {
            IndexedCell<?> cell = currentFlow.getCell(i);
            if (cell != null) {
                Message msg = (Message) cell.getItem();
                TextMessageCellController controller = messageCellCreator.getControllerCache().get(msg);
                if (controller != null) {
                    controller.setSelected(selectedMessages.contains(msg));
                }
            }
        }
    }

    private void updateDragSelection(int start, int end) {
        if (start == end) {
            Set<Message> newSelection = new HashSet<>(baseSelection);
            Set<Message> currentSet = new HashSet<>(selectedMessages);
            if (!newSelection.equals(currentSet)) {
                selectedMessages.clear();
                selectedMessages.addAll(newSelection);
                updateVisibleCellsSelection();
            }
            return;
        }
        int low = Math.min(start, end);
        int high = Math.max(start, end);
        List<Message> items = messagesLV.getItems();

        Set<Message> newSelection = new HashSet<>(baseSelection);
        for (int i = low; i <= high; i++) {
            Message msg = items.get(i);
            if (dragStartSelected) {
                newSelection.remove(msg);
            } else {
                newSelection.add(msg);
            }
        }

        Set<Message> currentSet = new HashSet<>(selectedMessages);
        if (!newSelection.equals(currentSet)) {
            selectedMessages.clear();
            selectedMessages.addAll(newSelection);
            updateVisibleCellsSelection();
        }
    }

    @FXML
    private void actionSearchBtn() {
        hiddenList.clear();
        searchModeActive = true;
        for (Node node : selectedChatVB.getChildren()) {
            if (!(node instanceof ListView<?>) && node.isVisible()) {
                node.setVisible(false);
                if (node.getId() != null && (node.getId().equals("pinnedMessagesHB") || node.getId().equals("updateMessageHB")))
                    node.setManaged(false);
                hiddenList.add(node);
            }
        }

        HBox topSearchModeHB = new HBox();
        topSearchModeHB.setStyle("-fx-background-color: white;");
        StackPane.setAlignment(topSearchModeHB, Pos.TOP_LEFT);
        topSearchModeHB.setPrefHeight(40);
        topSearchModeHB.setMaxHeight(40);
        topSearchModeHB.setAlignment(Pos.CENTER_LEFT);
        rightSP.getChildren().add(topSearchModeHB);

        Button closeSearchModeBtn = new Button();
        topSearchModeHB.getChildren().add(closeSearchModeBtn);

        ImageView closeSearchModeIV = new ImageView(
                new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/example/speech/image/arrow.png"))));
        closeSearchModeIV.setFitWidth(15);
        closeSearchModeIV.setFitHeight(15);
        closeSearchModeIV.setPreserveRatio(true);

        closeSearchModeBtn.setGraphic(closeSearchModeIV);

        TextField searchFromChatTF = new TextField();
        searchFromChatTF.setPromptText("Поиск...");
        applyPromptWithTF(searchFromChatTF);
        HBox.setHgrow(searchFromChatTF, Priority.ALWAYS);
        topSearchModeHB.getChildren().add(searchFromChatTF);

        Button searchContainsStrFromMessage = new Button();
        topSearchModeHB.getChildren().add(searchContainsStrFromMessage);

        ImageView searchModeIV = new ImageView(
                new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/example/speech/image/imageSearchButton.png"))));
        searchModeIV.setFitWidth(15);
        searchModeIV.setFitHeight(15);
        searchModeIV.setPreserveRatio(true);

        searchContainsStrFromMessage.setGraphic(searchModeIV);

        HBox bottomSearchModeHB = new HBox();
        bottomSearchModeHB.setStyle("-fx-background-color: white;");
        StackPane.setAlignment(bottomSearchModeHB, Pos.BOTTOM_LEFT);
        bottomSearchModeHB.setPrefHeight(40);
        bottomSearchModeHB.setMaxHeight(40);
        bottomSearchModeHB.setAlignment(Pos.CENTER_LEFT);
        rightSP.getChildren().add(bottomSearchModeHB);

        ImageView listSearchModeIV = new ImageView(
                new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/example/speech/image/task.png"))));
        listSearchModeIV.setFitWidth(25);
        listSearchModeIV.setFitHeight(25);
        listSearchModeIV.setPreserveRatio(true);
        bottomSearchModeHB.getChildren().add(listSearchModeIV);

        TextField countResult = new TextField();
        countResult.setDisable(true);
        countResult.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(countResult, Priority.ALWAYS);
        bottomSearchModeHB.getChildren().add(countResult);

        Button listOfBtn = new Button("Списком");
        listOfBtn.setDisable(true);
        bottomSearchModeHB.getChildren().add(listOfBtn);
        listOfBtn.setOnAction(e -> {
            //Вывод только нужных сообщений
            if(listOfBtn.getText().equals("Списком")) {
                filteredList.setPredicate(message -> (new String(message.getMessageContent(), StandardCharsets.UTF_8))
                        .toLowerCase().contains(searchFromChatTF.getText().trim().toLowerCase()));
                listOfBtn.setText("Чатом");
            } else {
                filteredList.setPredicate(message -> true);
                listOfBtn.setText("Списком");
                scrollToMessage(resultList.getLast());
            }
        });

        VBox buttonsVB = new VBox(10);//Нужно скрывать местами
        StackPane.setAlignment(buttonsVB, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(buttonsVB, new Insets(0, 10, 10 + 40, 0));
        buttonsVB.setPrefWidth(40);
        buttonsVB.setMaxWidth(40);
        buttonsVB.setMaxHeight(Region.USE_PREF_SIZE);
        rightSP.getChildren().add(buttonsVB);

        Button upBtn = new Button();
        buttonsVB.getChildren().add(upBtn);
        upBtn.setDisable(true);

        ImageView upIV = new ImageView(
                new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/example/speech/image/up.png"))));
        upIV.setFitWidth(15);
        upIV.setFitHeight(15);
        upIV.setPreserveRatio(true);

        upBtn.setGraphic(upIV);

        Button downBtn = new Button();
        buttonsVB.getChildren().add(downBtn);
        downBtn.setDisable(true);
        downBtn.setOnAction(e -> {
            //Кнопка вниз
            currInd -= 1;
            scrollToMessage(resultList.get(resultList.size() - currInd));
            upBtn.setDisable(currInd == resultList.size());
            downBtn.setDisable(currInd == 1);
            countResult.setText(currInd + "/" + resultList.size());
        });
        upBtn.setOnAction(e -> {
            //Кнопка вверх
            currInd += 1;
            scrollToMessage(resultList.get(resultList.size() - currInd));
            upBtn.setDisable(currInd == resultList.size());
            downBtn.setDisable(currInd == 1);
            countResult.setText(currInd + "/" + resultList.size());
        });

        ImageView downIV = new ImageView(
                new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/example/speech/image/down.png"))));
        downIV.setFitWidth(15);
        downIV.setFitHeight(15);
        downIV.setPreserveRatio(true);

        downBtn.setGraphic(downIV);


        searchContainsStrFromMessage.setOnAction(e -> {
            //Кнопка поиска вхождений подстроки в сообщения
            if (searchFromChatTF.getText() != null && !searchFromChatTF.getText().isEmpty()) {
                resultList = messages.stream().filter(message ->
                        (new String(message.getMessageContent(), StandardCharsets.UTF_8)).toLowerCase().contains(searchFromChatTF.getText().trim().toLowerCase())).toList();

                if (resultList.isEmpty()) {
                    countResult.setText("Нету результатов");
                    upBtn.setDisable(true);
                    downBtn.setDisable(true);
                    listOfBtn.setDisable(true);
                } else {
                    countResult.setText("1/" + resultList.size());
                    listOfBtn.setDisable(false);
                    scrollToMessage(resultList.getLast());
                    currInd = 1;
                    upBtn.setDisable(currInd == resultList.size());
                    downBtn.setDisable(currInd == 1);
                }
            }
        });

        closeSearchModeBtn.setOnAction(e -> {
            for (Node node : hiddenList) {
                node.setVisible(true);
                node.setManaged(true);
            }
            searchModeActive = false;
            rightSP.getChildren().removeAll(topSearchModeHB, bottomSearchModeHB, buttonsVB);
            filteredList.setPredicate(m -> true);
        });
    }
}