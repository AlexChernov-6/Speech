package com.example.speech.control;

import com.example.speech.model.*;
import com.example.speech.service.*;
import com.example.speech.util.FileUtils;
import com.example.speech.util.HelpfulClass;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.*;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.skin.VirtualFlow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;

import javafx.scene.control.IndexedCell;

import static com.example.speech.control.EntranceController.EMOJI_LIST;
import static com.example.speech.util.HelpfulStylingClass.applyPromptWithTF;
import static com.example.speech.util.HelpfulStylingClass.setupFullScreenListener;

public class SpeechBaseController {
    private Stage stage;
    private User currentUser;

    @FXML
    public ListView<ChannelUser> chatsView;

    public ObservableList<ChannelUser> userChats;

    private ListView<File> fileListView;

    private HBox topSearchModeHB;

    private SearchChannelWindow searchChannelWindow;

    private final ChannelUserService channelUserService = new ChannelUserService();
    private final MessageService messageService = new MessageService();

    private FilteredList<Message> filteredList;

    private List<Node> hiddenList = new ArrayList<>();

    private boolean searchModeActive = false;

    private List<Message> resultList;

    private int currInd = 0;

    private Label countPinnedMessages;

    private Button unpinnedAllMessagesBtn;

    public ObservableList<File> selectedFile;

    private HBox bottomSearchModeHB;

    private VBox buttonsVB;

    @FXML
    private StackPane rightSP;

    @FXML
    private Label channelName, channelStatus;
    @FXML
    private AnchorPane rootAnchorPane;
    @FXML
    private TextArea messageTA;
    @FXML
    private VBox selectedChatVB;
    @FXML
    private Button sendMessageBtn, emojiBtn;
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
    private ImageView HintIV;
    @FXML
    private Label HintLB;
    @FXML
    private AnchorPane channelStateAP;
    @FXML
    private HBox pinnedMessagesHB;
    @FXML
    private Label contentPinnedMessageLB;

    private ContextPopUpBar contextPopUpBar;

    private Button backBtn;

    protected enum ContextPopUpBar {
        CHANGE_MESSAGE,
        REPLY_MESSAGE,
        FORWARD_MESSAGE
    }

    private int countLinesOldValue;

    private MessageCellCreator messageCellCreator;

    private Message lastPinnedMessage;

    private ChannelUser selectedChannelUser;

    private ScrollPane emojiSP;

    public int firstVisible;

    public boolean flag = true;

    private ObservableList<Message> forwardMessages = FXCollections.observableArrayList();

    private boolean selectionModeActive = false;
    private final List<Message> selectedMessages = new ArrayList<>();

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

    private MessageListener messageListener;

    private EventHandler<MouseEvent> removeControlsWindow;

    @FXML
    private Button createControlsWindowBtn;

    public void initializeData(Stage stage, User currentUser) {
        this.stage = stage;
        this.currentUser = currentUser;
        applyPromptWithTF(messageTA);
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

        HelpfulClass.setImageWithButton(sendMessageBtn, "imageSendButton.png");

        HelpfulClass.setImageWithButton(emojiBtn, "imageEmojiButton.png");

        filteredList = new FilteredList<>(messages, m -> true);

        messagesLV.setItems(filteredList);
        setupFullScreenListener(stage, rootAnchorPane);
        initializeListViewChats();
        setupMessageTextAreaListener();

        messageListener = new MessageListener("jdbc:postgresql://192.168.1.103:5432/speechdb"
                , currentUser.getNameUser(), currentUser.getPasswordUser(),
                messageID -> {Platform.runLater(() -> messagesListViewRefresh(messageID));}, userChats, this);

        Thread listenerThread = new Thread(messageListener, "pg-listener");
        listenerThread.setDaemon(true);
        listenerThread.start();

        messagesSP.widthProperty().addListener((ch, oldValue, newValue) -> {
            double newWidth = newValue.doubleValue() - 300;

            if (newWidth <= 450) {
                if (selectedChatVB.isVisible()) {
                    AnchorPane.setLeftAnchor(rightSP, 5.0);
                    leftVB.setVisible(false);
                }
            } else {
                AnchorPane.setLeftAnchor(rightSP, 300.0);
                leftVB.setVisible(true);
            }

            messagesLV.setPrefWidth(newWidth);
        });
        messagesLV.setSelectionModel(null);
        messageCellCreator = new MessageCellCreator(this);
        messagesLV.setCellFactory(messageCellCreator);
        messagesLV.getStyleClass().add("no-horizontal-scroll");
        stackPaneListener();

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
                        if (currentFlow.getFirstVisibleCell() != null) {
                            firstVisible = currentFlow.getFirstVisibleCell().getIndex();
                            if (flag && !searchModeActive) setPinnedMessagesHBVisible(firstVisible);
                        }
                    });
                }
            }
        });

        messageTA.setOnKeyReleased(e -> {
            if(e.isShiftDown() && e.getCode() == KeyCode.ENTER) {
                int caretPos = messageTA.getCaretPosition();
                messageTA.insertText(caretPos, "\n");
                return;
            }

            if(e.getCode() == KeyCode.ENTER && sendMessageBtn.isVisible())
                sendMessageBtn.fire();
        });

        selectedFile = FXCollections.observableArrayList();
        fileListView = new ListView<>();
        fileListView.setMaxHeight(70);
        fileListView.setMinHeight(70);
        fileListView.setPrefHeight(70);
        fileListView.setVisible(false);
        fileListView.setManaged(false);
        fileListView.setSelectionModel(null);
        fileListView.setOrientation(Orientation.HORIZONTAL);
        fileListView.getStyleClass().add("no-vertical-scroll");
        fileListView.prefWidthProperty().bind(selectedChatVB.widthProperty());
        fileListView.setCellFactory(f -> new FileCell(this));
        fileListView.setItems(selectedFile);
        selectedChatVB.getChildren().add(4, fileListView);

        // Слушатель размера списка
        selectedFile.addListener((ListChangeListener<File>) change -> {
            while (change.next()) { }
            int size = selectedFile.size();
            Platform.runLater(() -> {

                    if (size > 0) {
                        AnchorPane.setRightAnchor(messageTA, 102.0);
                        AnchorPane.setRightAnchor(emojiBtn, 50.0);
                        sendMessageBtn.setVisible(true);
                        if (fileListView != null) {
                            fileListView.setVisible(true);
                            fileListView.setManaged(true);
                        }
                    } else {
                        if(!updateMessageHB.isVisible() || (updateMessageHB.isVisible() && contextPopUpBar != ContextPopUpBar.FORWARD_MESSAGE)) {
                            if (messageTA != null && (messageTA.getText() == null || messageTA.getText().isEmpty() || messageTA.getText().equals("Сообщение..."))) {
                                AnchorPane.setRightAnchor(messageTA, 51.0);
                                AnchorPane.setRightAnchor(emojiBtn, 0.0);
                                sendMessageBtn.setVisible(false);
                            }
                        }
                        if (fileListView != null) {
                            fileListView.setVisible(false);
                            fileListView.setManaged(false);
                        }
                    }

            });
        });

        messageTA.setOnKeyPressed(e -> {
            if (e.isControlDown() && e.getCode() == KeyCode.V) {
                List<File> files = Clipboard.getSystemClipboard().getFiles();
                if (files != null && !files.isEmpty()) {
                    selectedFile.addAll(files);
                    try {
                        FileUtils.copyFilesToDir(files);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
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
                    if(!selectionModeActive)
                        activateSelectionModeWithMessage(clickedMsg);
                }
                dragSelecting = false;
                dragStartIndex = -1;
                lastProcessedDragIndex = -1;
                baseSelection.clear();
                if (selectedMessages.isEmpty())
                    setSelectionModeActive(false);
                event.consume();
            }
        });
    }

    public void initializeListViewChats() {
        chatsView.setFixedCellSize(60);
        chatsView.setCellFactory(lv -> new ListChannelsCellController(this));
        userChats = FXCollections.observableArrayList(channelUserService.getAllChatsByUser(currentUser));
        chatsView.setItems(userChats);

        chatsView.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue != null) {
                        selectedChannelUser = newValue;
                        loadChannelMessages(newValue);
                    }
                });
    }

    public void loadChannelMessages(ChannelUser selectedChat) {
        selectedChatVB.setVisible(true);
        channelName.setText(selectedChat.getChannel().getChannelName());
        channelStatus.setText(selectedChat.getChannel().getChannelType().getChannelTypeId() == 3 ?
                channelUserService.getInterlocutorStatus(selectedChat.getChannel(), selectedChat.getUser()) :
                String.format("Число участников: %d", selectedChat.getChannel().getChannelCountUser()));
        messageTA.setText("");
        messageCellCreator.clearCache();

        messageTA.requestFocus();

        hideTheListOfPinnedMessages();

        hideEmojiWindow();

        if (searchModeActive) {
            for (Node node : hiddenList) {
                node.setVisible(true);
                node.setManaged(true);
            }
            searchModeActive = false;
            rightSP.getChildren().removeAll(topSearchModeHB, bottomSearchModeHB, buttonsVB);
            filteredList.setPredicate(m -> true);
            messagesLV.scrollTo(messages.getLast());
        }

        updateVisibleChangeMessageHB();

        if (pinnedMessagesHB.isVisible()) {
            pinnedMessagesHB.setVisible(false);
            pinnedMessagesHB.setManaged(false);
        }
        lastPinnedMessage = null;

        setSelectionModeActive(false);

        new Thread(() -> {
            List<Message> messagesList = messageService.getAllMessageInChannel(
                    selectedChannelUser.getChannel().getChannelID());
            List<Message> filtered = messagesList.stream()
                    .filter(message -> !message.getDeletedByUsers()
                            .contains(Long.valueOf(currentUser.getIdUser())))
                    .toList();

            // Обновление UI
            Platform.runLater(() -> {
                messages.setAll(filtered);
                if (!messagesLV.getItems().isEmpty())
                    messagesLV.scrollTo(messagesLV.getItems().size());
                if (firstVisible == 0)
                    setPinnedMessagesHBVisible(messagesLV.getItems().size());
                else
                    setPinnedMessagesHBVisible(firstVisible);
            });
        }).start();
    }

    private void setupMessageTextAreaListener() {
        messageTA.textProperty().addListener((observable, oldValue, newValue) -> {
            boolean hasVisibleText = newValue != null &&
                    !newValue.trim().isEmpty() &&
                    !newValue.matches("^[\\n\\r\\s]*$") &&
                    !newValue.equals("Сообщение...");

            if(!updateMessageHB.isVisible() || (updateMessageHB.isVisible() && contextPopUpBar != ContextPopUpBar.FORWARD_MESSAGE)) {
                if (!hasVisibleText) {
                    if (selectedFile == null || selectedFile.isEmpty()) {
                        sendMessageBtn.setVisible(false);
                        AnchorPane.setRightAnchor(emojiBtn, 0.0);
                        AnchorPane.setRightAnchor(messageTA, 51.0);
                    }
                } else {
                    AnchorPane.setRightAnchor(messageTA, 102.0);
                    AnchorPane.setRightAnchor(emojiBtn, 50.0);
                    sendMessageBtn.setVisible(true);
                }
            }

            adjustTextAreaHeight(newValue);
        });
    }

    private void adjustTextAreaHeight(String newValue) {
        int countLinesNewValue = countTextAreaLines(messageTA);

        int setMinHeightByCountLines = Math.max(countLinesNewValue, 2) * 20;

        if (countLinesNewValue > countLinesOldValue) {
            messageAnchor.setMinHeight(Math.min(setMinHeightByCountLines, 200));
        } else if (countLinesNewValue < countLinesOldValue) {
            messageAnchor.setMinHeight(Math.max(40, Math.min(setMinHeightByCountLines, 200)));
        }

        if (newValue == null || newValue.trim().isEmpty()) {
            messageAnchor.setMinHeight(40);
            messageAnchor.setPrefHeight(40);
            messageTA.setPrefHeight(40);
        }
        countLinesOldValue = countLinesNewValue;
    }

    public int countTextAreaLines(TextArea textArea) {
        String text = textArea.getText();
        if (text == null || text.trim().isEmpty()) {
            return 1;
        }

        Text helper = new Text(text);
        helper.setFont(textArea.getFont());

        double textAreaWidth = textArea.getWidth();
        double leftPadding = textArea.getPadding().getLeft();
        double rightPadding = textArea.getPadding().getRight();
        double maxWidth = Math.max(textAreaWidth - leftPadding - rightPadding - 5, 50);
        helper.setWrappingWidth(maxWidth);

        double textHeight = helper.getLayoutBounds().getHeight();

        Font font = textArea.getFont();
        double lineHeight = font.getSize() * 1.6;

        int lines = (int) Math.ceil(textHeight / lineHeight);
        return Math.max(1, lines);
    }

    @FXML
    private void handleSendMessage() throws IOException {
        String text = messageTA.getText().trim();
        messageTA.setText("");
        messageTA.requestFocus();

        // ----------------------- Обычная отправка -----------------------
        if (((!text.isEmpty() && !text.equals("Сообщение...")) || (selectedFile != null && !selectedFile.isEmpty()))
                && chatsView.getSelectionModel().getSelectedItem() != null
                && (!updateMessageHB.isVisible() || contextPopUpBar == ContextPopUpBar.REPLY_MESSAGE)) {

            ChannelUser selectedChat = chatsView.getSelectionModel().getSelectedItem();

            Message messageToSave = new Message();

            addContentsForMessage(messageToSave, text);

            messageToSave.setChannelUser(selectedChat);
            messageToSave.setMessageDatetime(LocalDateTime.now());
            if (updateMessageHB.isVisible() && contextPopUpBar == ContextPopUpBar.REPLY_MESSAGE) {
                messageToSave.setMessageIdReplyTo(messageIdReplyTo);
            }

            Platform.runLater(() -> {
                messages.add(messageToSave);
                messagesLV.scrollTo(messagesLV.getItems().size());
            });

            Thread sendThread = new Thread(() -> {
                try {
                    boolean saved = messageService.save(messageToSave);
                    if (saved) {
                        Message savedMessage = messageService.getRowById(messageToSave.getMessageId());
                        savedMessage.setMessageStatus("отправлено");
                        messageService.update(savedMessage);
                        chatsView.refresh();
                        Platform.runLater(() -> {
                            int index = messages.indexOf(messageToSave);
                            if (index >= 0) {
                                messages.set(index, savedMessage);
                            }
                        });
                    }
                } catch (Exception e) {
                    messageToSave.setMessageStatus("ошибка отправки");
                }
            });
            sendThread.setDaemon(true);
            sendThread.start();

            updateVisibleChangeMessageHB();
            return;
        }

        // ----------------------- ИЗМЕНЕНИЕ СООБЩЕНИЯ -----------------------
        if (((!text.isEmpty() && !text.equals("Сообщение...")) || (selectedFile != null && !selectedFile.isEmpty()))
                && chatsView.getSelectionModel().getSelectedItem() != null
                && updateMessageHB.isVisible()
                && contextPopUpBar == ContextPopUpBar.CHANGE_MESSAGE) {

            String oldText = updateMessage.getMessageString();
            List<File> oldMessageContentList = updateMessage.getMessageContent().stream()
                    .map(mC -> FileUtils.getFileFromDefaultDir(mC.getMessageContentFileName()))
                    .sorted((f1, f2) -> f1.getName().compareTo(f2.getName()))
                    .toList();

            List<File> newMessageContentList = selectedFile.stream()
                    .sorted((f1, f2) -> f1.getName().compareTo(f2.getName()))
                    .toList();

            boolean newContent = false;

            if (oldMessageContentList.size() != newMessageContentList.size())
                newContent = true;
            else {
                for (int i = 0; i < oldMessageContentList.size(); i++) {
                    File oldFile = oldMessageContentList.get(i);
                    File newFile = newMessageContentList.get(i);
                    if (!oldFile.getName().equals(newFile.getName()) || oldFile.length() != newFile.length()) {
                        newContent = true;
                        break;
                    }
                }
            }

            if (!text.equals(oldText) || newContent) {
                final Message messageToUpdate = updateMessage;

                addContentsForMessage(messageToUpdate, text);

                messageToUpdate.setModifiedMessage(true);

                // Асинхронное сохранение
                Thread sendThread = new Thread(() -> {
                    try {
                        messageService.update(messageToUpdate);
                        chatsView.refresh();
                        Platform.runLater(() -> {
                            // Успешно – обнуляем глобальную переменную, скрываем панель
                            updateMessage = null;
                            updateVisibleChangeMessageHB();
                        });
                    } catch (Exception e) {

                    }
                });
                sendThread.setDaemon(true);
                sendThread.start();

                messageTA.requestFocus();
                messageTA.setText("");
                messagesLV.refresh();
                return;
            } else {
                messageTA.requestFocus();
                updateVisibleChangeMessageHB();
            }
        }

        // ----------------------- Пересылка -----------------------
        if (chatsView.getSelectionModel().getSelectedItem() != null
                && updateMessageHB.isVisible()
                && contextPopUpBar == ContextPopUpBar.FORWARD_MESSAGE) {

            List<Message> tempMessages = new ArrayList<>();

            // Создаём временные сообщения для немедленного отображения
            for (Message original : forwardMessages) {
                Message temp = new Message();
                temp.setMessageDatetime(LocalDateTime.now());
                temp.setMessageString(original.getMessageString());
                temp.setMessageContent(new ArrayList<>());  // пустой список, чтобы не тащить оригинальные объекты
                temp.setChannelUser(selectedChannelUser);
                temp.setMessageStatus("загружается");
                temp.setForwardedFrom((long) original.getChannelUser().getUser().getIdUser());
                messages.add(temp);
                tempMessages.add(temp);
            }

            Platform.runLater(() -> messagesLV.scrollTo(messagesLV.getItems().size()));

            // Асинхронное сохранение
            Thread sendThread = new Thread(() -> {
                for (int i = 0; i < forwardMessages.size(); i++) {
                    Message original = forwardMessages.get(i);
                    Message tempMsg = tempMessages.get(i);
                    try {
                        // Копируем контент, создавая новые объекты
                        List<MessageContent> newContents = new ArrayList<>();
                        if (original.getMessageContent() != null) {
                            for (MessageContent origContent : original.getMessageContent()) {
                                String fileName = origContent.getMessageContentFileName();
                                Path localFile = FileUtils.DEFAULT_STORAGE_DIR.resolve(fileName);
                                byte[] bytes;
                                if (Files.exists(localFile)) {
                                    bytes = FileUtils.readFileToByteArrayStream(localFile.toFile());
                                } else {
                                    bytes = new MessageContentService().getContentBytes(origContent.getMessageContentId());
                                }
                                if (bytes != null) {
                                    MessageContent newContent = new MessageContent();
                                    newContent.setMessageContentBytes(bytes);
                                    newContent.setMessageContentFileName(fileName);
                                    newContents.add(newContent);
                                }
                            }
                        }

                        Message newMsg = new Message();
                        newMsg.setMessageContent(newContents);
                        newMsg.setChannelUser(selectedChannelUser);
                        newMsg.setMessageString(original.getMessageString());
                        newMsg.setMessageDatetime(LocalDateTime.now());
                        newMsg.setForwardedFrom((long) original.getChannelUser().getUser().getIdUser());

                        boolean saved = new MessageService().save(newMsg);
                        if (saved) {
                            Message savedMsg = messageService.getRowById(newMsg.getMessageId());
                            savedMsg.setMessageStatus("отправлено");
                            messageService.update(savedMsg);
                            Platform.runLater(() -> {
                                int idx = messages.indexOf(tempMsg);
                                if (idx >= 0) {
                                    messages.set(idx, savedMsg);
                                }
                            });
                        } else {
                            Platform.runLater(() -> tempMsg.setMessageStatus("ошибка отправки"));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Platform.runLater(() -> tempMsg.setMessageStatus("ошибка отправки"));
                    }
                }
                chatsView.refresh();
            });
            sendThread.setDaemon(true);
            sendThread.start();

            messageTA.requestFocus();
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
        if (selectedFile != null)
            selectedFile.clear();
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
            if (pinnedMessagesHB.isVisible()) {
                pinnedMessagesHB.setVisible(false);
                pinnedMessagesHB.setManaged(false);
            }
            lastPinnedMessage = null;
            return;
        }

        Message nextPinnedMessage = null;

        for (Message pinnedMsg : allPinnedMessages) {
            int msgIndex = messagesLV.getItems().indexOf(pinnedMsg);
            if (msgIndex < firstVisibleIndex) {
                nextPinnedMessage = pinnedMsg;
                break;
            }
        }

        lastPinnedMessage = (nextPinnedMessage == null) ? allPinnedMessages.getFirst() : nextPinnedMessage;
        pinnedMessagesHB.setVisible(true);
        pinnedMessagesHB.setManaged(true);
        if ((lastPinnedMessage.getMessageString() != null && !lastPinnedMessage.getMessageString().isEmpty()) ||
                (lastPinnedMessage.getMessageContent() != null && !lastPinnedMessage.getMessageContent().isEmpty())) {
            String contentMessage;

            if (lastPinnedMessage.getMessageString() != null && !lastPinnedMessage.getMessageString().isEmpty())
                contentMessage = lastPinnedMessage.getMessageString();
            else if (lastPinnedMessage.getMessageContent() != null && !lastPinnedMessage.getMessageContent().isEmpty()) {
                int countContents = lastPinnedMessage.getMessageContent().size();
                if (countContents == 1)
                    contentMessage = String.format("%d вложение", countContents);
                else if (countContents >= 2 && countContents <= 4)
                    contentMessage = String.format("%d вложения", countContents);
                else
                    contentMessage = String.format("%d вложений", countContents);
            } else
                contentMessage = "";

            if (contentMessage.length() > 50) {
                contentMessage = contentMessage.substring(0, 47) + "...";
            }
            contentPinnedMessageLB.setText(contentMessage);
        }
    }

    private void scrollToMessage(Message message) {
        messagesLV.scrollTo(message);
        PauseTransition pause = new PauseTransition(Duration.millis(100));
        pause.setOnFinished(e ->
                messageCellCreator.getControllerCache(message).highlightMessageTemporarily()
        );
        pause.play();
    }

    @FXML
    private void showPinnedContent() {
        flag = false;

        filteredList.setPredicate(mes -> mes != null && Boolean.TRUE.equals(mes.getPinMessage()));

        for (Node node : channelStateAP.getChildren()) {
            node.setVisible(false);
            node.setManaged(false);
        }

        if (backBtn == null) {
            backBtn = new Button();
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
        } else if (!backBtn.isVisible()) {
            backBtn.setVisible(true);
            backBtn.setManaged(true);
        }

        if (countPinnedMessages == null) {
            countPinnedMessages = new Label();
            countPinnedMessages.setId("countPinnedMessages");
            countPinnedMessages.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
            AnchorPane.setLeftAnchor(countPinnedMessages, 70.0);
            AnchorPane.setRightAnchor(countPinnedMessages, 0.0);
            AnchorPane.setTopAnchor(countPinnedMessages, 0.0);
            AnchorPane.setBottomAnchor(countPinnedMessages, 0.0);
            channelStateAP.getChildren().add(countPinnedMessages);
        } else if (!countPinnedMessages.isVisible()) {
            countPinnedMessages.setVisible(true);
            countPinnedMessages.setManaged(true);
        }

        countPinnedMessages.setText(String.format("%d закреплённых сообщений", filteredList.size()));

        if (pinnedMessagesHB.isVisible()) {
            pinnedMessagesHB.setVisible(false);
            pinnedMessagesHB.setManaged(false);
        }

        if (updateMessageHB.isVisible()) {
            updateMessageHB.setVisible(false);
            updateMessageHB.setManaged(false);
        }

        for (Node node : messageAnchor.getChildren()) {
            node.setVisible(false);
            node.setManaged(false);
        }

        if (unpinnedAllMessagesBtn == null) {
            unpinnedAllMessagesBtn = new Button();
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
        } else if (!unpinnedAllMessagesBtn.isVisible()) {
            unpinnedAllMessagesBtn.setVisible(true);
            unpinnedAllMessagesBtn.setManaged(true);
        }

        unpinnedAllMessagesBtn.setText("ОТКРЕПИТЬ " + filteredList.size() + " СООБЩЕНИЙ");
    }

    public void updatePinnedMessagesList() {
        filteredList.setPredicate(mes -> true);
        filteredList.setPredicate(mes -> mes != null && Boolean.TRUE.equals(mes.getPinMessage()));
        if (!filteredList.isEmpty()) {
            if (countPinnedMessages != null)
                countPinnedMessages.setText(String.format("%d закреплённых сообщений", filteredList.size()));
            if (unpinnedAllMessagesBtn != null)
                unpinnedAllMessagesBtn.setText("ОТКРЕПИТЬ " + filteredList.size() + " СООБЩЕНИЙ");
        } else
            hideTheListOfPinnedMessages();
    }

    public void hideTheListOfPinnedMessages() {
        flag = true;
        filteredList.setPredicate(m -> true);

        for (Node node : channelStateAP.getChildren()) {
            node.setVisible(true);
            node.setManaged(true);
        }

        setPinnedMessagesHBVisible(firstVisible);

        if (updateMessage != null) {
            updateMessageHB.setVisible(true);
            updateMessageHB.setManaged(true);
        }

        for (Node node : messageAnchor.getChildren()) {
            if (!node.equals(sendMessageBtn)) {
                node.setVisible(true);
                node.setManaged(true);
            }
        }

        messagesLV.scrollTo(messages.size());

        if (backBtn != null) {
            backBtn.setVisible(false);
            backBtn.setManaged(false);
        }

        if (countPinnedMessages != null) {
            countPinnedMessages.setVisible(false);
            countPinnedMessages.setManaged(false);
        }

        if (unpinnedAllMessagesBtn != null) {
            unpinnedAllMessagesBtn.setVisible(false);
            unpinnedAllMessagesBtn.setManaged(false);
        }
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public Button getEmojiBtn() {
        return emojiBtn;
    }

    public Button getSendMessageBtn() {
        return sendMessageBtn;
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

    public MessageListener getMessageListener() {
        return messageListener;
    }

    public void setMessageListener(MessageListener messageListener) {
        this.messageListener = messageListener;
    }

    public void setSelectionModeActive(boolean active) {
        if (this.selectionModeActive == active) return;
        this.selectionModeActive = active;
        if (!active) {
            selectedMessages.clear();
            updateAllMessageCellsSelection();
        }
        updateAllMessageCellsSelectionMode();
        checkSelectedMode(active);
    }

    public boolean isMessageSelected(Message message) {
        return selectedMessages.contains(message);
    }

    public void clearSelection() {
        selectedMessages.clear();
        updateAllMessageCellsSelection();
    }

    private void updateAllMessageCellsSelectionMode() {
        MessageCellCreator creator = getMessageCellCreator();
        for (Map.Entry<Message, TextMessageCellController> entry :
                creator.getControllerCache().entrySet()) {
            TextMessageCellController controller = entry.getValue();
            controller.setSelectionModeActive(selectionModeActive);
        }
    }

    // Update a single cell's selection state
    private void updateMessageCellSelection(Message message) {
        TextMessageCellController controller =
                getMessageCellCreator().getControllerCache().get(message);
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
                forwardMessages.setAll(selectedMessages);
                new ChatSelectionController(this, forwardMessages);
                setSelectionModeActive(false);
            });
            selectionForwardBtn.setPrefWidth(120);
            AnchorPane.setLeftAnchor(selectionForwardBtn, 10.0);
            AnchorPane.setTopAnchor(selectionForwardBtn, 0.0);
            AnchorPane.setBottomAnchor(selectionForwardBtn, 0.0);

            selectionDeleteBtn = new Button("УДАЛИТЬ");
            selectionDeleteBtn.getStyleClass().add("login-button");
            selectionDeleteBtn.setOnAction(e -> {
                new ConfirmationOfMessageDeletion().initializeShape(channelName.getText(), this
                        , selectedMessages);
            });
            selectionDeleteBtn.setPrefWidth(120);
            AnchorPane.setLeftAnchor(selectionDeleteBtn, 140.0);
            AnchorPane.setTopAnchor(selectionDeleteBtn, 0.0);
            AnchorPane.setBottomAnchor(selectionDeleteBtn, 0.0);

            selectionCancelBtn = new Button("ОТМЕНА");
            selectionCancelBtn.getStyleClass().add("login-button");
            selectionCancelBtn.setOnAction(e -> {
                setSelectionModeActive(false);
            });
            selectionCancelBtn.setPrefWidth(120);
            AnchorPane.setRightAnchor(selectionCancelBtn, 10.0);
            AnchorPane.setTopAnchor(selectionCancelBtn, 0.0);
            AnchorPane.setBottomAnchor(selectionCancelBtn, 0.0);
            if(selectedChannelUser.getChannel().isDisable_sharing()) {
                AnchorPane.setLeftAnchor(selectionDeleteBtn, 10.0);
                AnchorPane.setTopAnchor(selectionDeleteBtn, 10.0);
                AnchorPane.setBottomAnchor(selectionDeleteBtn, 10.0);
                channelStateAP.getChildren().addAll(selectionDeleteBtn, selectionCancelBtn);
            }
            else
                channelStateAP.getChildren().addAll(selectionForwardBtn, selectionDeleteBtn, selectionCancelBtn);
        } else {
            channelStateAP.getChildren().removeAll(selectionForwardBtn, selectionDeleteBtn, selectionCancelBtn);
            for (Node node : channelStateAP.getChildren()) {
                if (node.getId() != null && !node.equals(backBtn) && !node.equals(countPinnedMessages)) {
                    node.setVisible(true);
                    node.setManaged(true);
                }
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

        topSearchModeHB = new HBox();
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
        searchFromChatTF.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER)
                searchContainsStrFromMessage.fire();
        });

        ImageView searchModeIV = new ImageView(
                new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/example/speech/image/imageSearchButton.png"))));
        searchModeIV.setFitWidth(15);
        searchModeIV.setFitHeight(15);
        searchModeIV.setPreserveRatio(true);

        searchContainsStrFromMessage.setGraphic(searchModeIV);

        bottomSearchModeHB = new HBox();
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

        buttonsVB = new VBox(10);//Нужно скрывать местами
        StackPane.setAlignment(buttonsVB, Pos.BOTTOM_RIGHT);
        buttonsVB.setVisible(false);
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
            Platform.runLater(() -> {
                PauseTransition pauseTransition = new PauseTransition(Duration.millis(100));
                pauseTransition.setOnFinished(e1 -> {
                    System.out.println("Показываем выделение");
                    messageCellCreator.getControllerCache().get(resultList.get(resultList.size() - currInd)).highlightText(searchFromChatTF.getText());
                });
                pauseTransition.playFromStart();
            });
        });
        upBtn.setOnAction(e -> {
            //Кнопка вверх
            currInd += 1;
            scrollToMessage(resultList.get(resultList.size() - currInd));
            upBtn.setDisable(currInd == resultList.size());
            downBtn.setDisable(currInd == 1);
            countResult.setText(currInd + "/" + resultList.size());
            Platform.runLater(() -> {
                PauseTransition pauseTransition = new PauseTransition(Duration.millis(100));
                pauseTransition.setOnFinished(e1 -> {
                    System.out.println("Показываем выделение");
                    messageCellCreator.getControllerCache().get(resultList.get(resultList.size() - currInd)).highlightText(searchFromChatTF.getText());
                });
                pauseTransition.playFromStart();
            });
        });

        ImageView downIV = new ImageView(
                new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/example/speech/image/down.png"))));
        downIV.setFitWidth(15);
        downIV.setFitHeight(15);
        downIV.setPreserveRatio(true);

        downBtn.setGraphic(downIV);

        listOfBtn.setOnAction(e -> {
            //Вывод только нужных сообщений
            if (listOfBtn.getText().equals("Списком")) {
                filteredList.setPredicate(message -> message.getMessageString().toLowerCase()
                        .contains(searchFromChatTF.getText().trim().toLowerCase()));
                listOfBtn.setText("Чатом");
                countResult.setText("Всего записей: " + resultList.size());
                buttonsVB.setVisible(false);
            } else {
                filteredList.setPredicate(message -> true);
                listOfBtn.setText("Списком");
                scrollToMessage(resultList.getLast());
                currInd = 1;
                countResult.setText(currInd + "/" + resultList.size());
                buttonsVB.setVisible(true);
                Platform.runLater(() -> {
                    PauseTransition pauseTransition = new PauseTransition(Duration.millis(100));
                    pauseTransition.setOnFinished(e1 -> {
                        System.out.println("Показываем выделение");
                        messageCellCreator.getControllerCache().get(resultList.getLast()).highlightText(searchFromChatTF.getText());
                    });
                    pauseTransition.playFromStart();
                });
            }
        });

        searchContainsStrFromMessage.setOnAction(e -> {
            //Кнопка поиска вхождений подстроки в сообщения
            if (searchFromChatTF.getText() != null && !searchFromChatTF.getText().isEmpty()) {
                resultList = messages.stream().filter(message ->
                        message.getMessageString().toLowerCase().contains(searchFromChatTF.getText().trim().toLowerCase())).toList();

                if (resultList.isEmpty()) {
                    countResult.setText("Нету результатов");
                    upBtn.setDisable(true);
                    downBtn.setDisable(true);
                    listOfBtn.setDisable(true);
                    buttonsVB.setVisible(false);
                } else {
                    countResult.setText("1/" + resultList.size());
                    buttonsVB.setVisible(true);
                    listOfBtn.setDisable(false);
                    scrollToMessage(resultList.getLast());
                    currInd = 1;
                    upBtn.setDisable(currInd == resultList.size());
                    downBtn.setDisable(currInd == 1);
                    Platform.runLater(() -> {
                        PauseTransition pauseTransition = new PauseTransition(Duration.millis(100));
                        pauseTransition.setOnFinished(e1 -> {
                            System.out.println("Показываем выделение");
                            messageCellCreator.getControllerCache().get(resultList.getLast()).highlightText(searchFromChatTF.getText());
                        });
                        pauseTransition.playFromStart();
                    });
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
            if (messages != null && !messages.isEmpty())
                messagesLV.scrollTo(messages.getLast());
        });
    }

    public ObservableList<Message> getMessages() {
        return messages;
    }

    public void setMessages(ObservableList<Message> messages) {
        this.messages = messages;
    }

    @FXML
    private void actionAddFileBtn() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Выбор файлов (не более 10)");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.gif"),
                new FileChooser.ExtensionFilter("Text Files", "*.txt"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );

        List<File> chosenFiles = fileChooser.showOpenMultipleDialog(rootAnchorPane.getScene().getWindow());
        if (chosenFiles == null || chosenFiles.isEmpty()) return;

        selectedFile.addAll(chosenFiles);

        try {
            FileUtils.copyFilesToDir(chosenFiles);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addContentsForMessage(Message message, String actualTextInTextArea) throws IOException {
        List<MessageContent> contents = new ArrayList<>();
        if (selectedFile != null && !selectedFile.isEmpty()) {
            List<File> delFiles = new ArrayList<>();
            for (File f : selectedFile) {
                if (f.length() <= 2 * 1024 * 1024) {
                    MessageContent newMC = new MessageContent();
                    newMC.setMessageContentBytes(FileUtils.readFileToByteArrayStream(f));
                    newMC.setMessageContentFileName(f.getName());
                    contents.add(newMC);
                }
                delFiles.add(f);
            }
            contents = contents.stream()
                    .sorted((f1, f2) -> f1.getMessageContentBytes().length - f2.getMessageContentBytes().length)
                    .limit(10)
                    .toList();
            if (contents.size() != selectedFile.size())
                System.out.println("Часть сообщений не будет отправлена");
            selectedFile.removeAll(delFiles);
        }
        if (!actualTextInTextArea.equals("Сообщение..."))
            message.setMessageString(actualTextInTextArea);
        else
            message.setMessageString(null);

        message.setMessageContent(contents);
    }

    @FXML
    private void showSearchChannelWindow() {
        if (searchChannelWindow == null)
            searchChannelWindow = new SearchChannelWindow(this);

        searchChannelWindow.show();
    }

    @FXML
    private void openEmojiWindow() {
        if (emojiSP == null)
            createEmojiWidow();
        if (emojiSP.isVisible())
            hideEmojiWindow();
        else
            showEmojiWindow();
    }

    private void createEmojiWidow() {
        emojiSP = new ScrollPane();
        emojiSP.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        emojiSP.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        emojiSP.setMaxWidth(220);
        emojiSP.setVisible(false);
        StackPane.setAlignment(emojiSP, Pos.TOP_RIGHT);

        // Содержимое (без изменений)
        VBox emojiVB = new VBox(5);
        emojiVB.setStyle("-fx-background-color: rgba(245, 245, 245);");
        emojiVB.setMaxWidth(260);
        emojiVB.setAlignment(Pos.TOP_RIGHT);
        emojiSP.setContent(emojiVB);

        Label frequentlyUsed = new Label("Доступные эмодзи");
        frequentlyUsed.getStyleClass().add("emoji-label");
        emojiVB.getChildren().add(frequentlyUsed);

        TilePane pane = new TilePane();
        pane.setHgap(5);
        pane.setVgap(5);
        pane.setPrefColumns(4);
        emojiVB.getChildren().add(pane);

        for (String emojiChar : EMOJI_LIST) {
            Button emojiBtn = new Button(emojiChar);
            emojiBtn.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-font-size: 19px;");
            emojiBtn.setOnAction(e -> {
                if (messageTA.getText().equals("Сообщение..."))
                    messageTA.setText(emojiChar);
                else
                    messageTA.setText(messageTA.getText() + emojiChar);
                messageTA.requestFocus(); // возвращаем фокус после выбора
            });
            pane.getChildren().add(emojiBtn);
        }

        rightSP.getChildren().add(emojiSP);

        // Слушатели для обновления позиции
        messageAnchor.heightProperty().addListener((obs, o, n) -> {
            if (emojiSP.isVisible()) updateEmojiWindowPosition();
        });
        pinnedMessagesHB.visibleProperty().addListener((obs, o, n) -> {
            if (emojiSP.isVisible()) updateEmojiWindowPosition();
        });
        updateMessageHB.visibleProperty().addListener((obs, o, n) -> {
            if (emojiSP.isVisible()) updateEmojiWindowPosition();
        });
        rightSP.heightProperty().addListener((obs, o, n) -> {
            if (emojiSP.isVisible()) updateEmojiWindowPosition();
        });
        // При изменении высоты самой панели (после заполнения содержимым)
        emojiSP.heightProperty().addListener((obs, o, n) -> {
            if (emojiSP.isVisible()) updateEmojiWindowPosition();
        });

        fileListView.visibleProperty().addListener((obs, o, n) -> {
            updateEmojiWindowPosition();
        });

        // Цвет кнопки при открытии/закрытии
        emojiSP.visibleProperty().addListener((ob, oldV, newV) -> {
            if (newV) {
                updateEmojiWindowPosition();
                HelpfulClass.setImageWithButton(emojiBtn, "imageEmojiButtonBlue.png");
            } else {
                HelpfulClass.setImageWithButton(emojiBtn, "imageEmojiButton.png");
            }
        });
    }

    private void showEmojiWindow() {
        if (emojiSP != null)
            emojiSP.setVisible(true);
    }

    private void hideEmojiWindow() {
        if (emojiSP != null)
            emojiSP.setVisible(false);
    }

    private void messagesListViewRefresh(Long messageID) {
        Message message = messageService.getRowById(messageID);
        if(message == null)
            messages.removeIf(m -> m.getMessageId() == messageID);
        else {
            if(selectedChannelUser != null && message.getChannelUser().getChannel().equals(selectedChannelUser.getChannel())) {
                if (messages.contains(message)) {
                    messages.set(messages.indexOf(message), message);
                    setPinnedMessagesHBVisible(messages.size());
                } else {
                    if (message.getForwardedFrom() == null || (message.getForwardedFrom() != null && !message.getChannelUser().getUser().equals(currentUser)) &&
                            message.getDeletedByUsers() == null || (message.getDeletedByUsers() != null && !message.getDeletedByUsers().contains(Long.valueOf(currentUser.getIdUser())))) {

                        messages.add(message);
                    }
                    if (!message.getChannelUser().getUser().equals(currentUser))
                        Toolkit.getDefaultToolkit().beep();
                }
            }
        }
        messagesLV.refresh();
        chatsView.refresh();
    }

    @FXML
    private void createControlsWidow() {
        double vBoxWidth = 200;

        VBox controlsWindowRootVB = new VBox();
        controlsWindowRootVB.setMaxWidth(vBoxWidth);
        controlsWindowRootVB.setMaxHeight(Region.USE_PREF_SIZE);
        controlsWindowRootVB.setUserData("controlsWindowRootVB");
        StackPane.setAlignment(controlsWindowRootVB, Pos.TOP_RIGHT);
        StackPane.setMargin(controlsWindowRootVB, new Insets(41, 10, 0, 0));
        controlsWindowRootVB.getStyleClass().add("working-with-a-message-root-pane");
        rightSP.getChildren().add(controlsWindowRootVB);

        CustomButton searchBtn = new CustomButton(
                new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/example/speech/image/imageSearchButton.png")))
                , "Поиск");
        searchBtn.setPrefWidth(vBoxWidth);
        searchBtn.setPrefHeight(40);
        VBox.setMargin(searchBtn, new Insets(5, 0, 0, 0));
        searchBtn.setOnAction(e -> {
            actionSearchBtn();
        });
        controlsWindowRootVB.getChildren().add(searchBtn);

        CustomButton editBtn = new CustomButton(
                new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/example/speech/image/change.png")))
                , "Ввод");
        editBtn.setPrefWidth(vBoxWidth);
        editBtn.setPrefHeight(40);
        editBtn.setOnAction(e -> {
            messageTA.requestFocus();
            messageTA.positionCaret(messageTA.getText().length());
        });
        controlsWindowRootVB.getChildren().add(editBtn);

        CustomButton muteBtn = new CustomButton(
                new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/example/speech/image/mute.png")))
                , "Отключить звук");
        muteBtn.setPrefWidth(vBoxWidth);
        muteBtn.setPrefHeight(40);
        muteBtn.setOnAction(e -> {
            System.out.println("Нажата кнопка без звука");
        });
        controlsWindowRootVB.getChildren().add(muteBtn);

        CustomButton selectMessagesBtn = new CustomButton(
                new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/example/speech/image/select.png")))
                , "Выбрать сообщения");
        selectMessagesBtn.setPrefWidth(vBoxWidth);
        selectMessagesBtn.setPrefHeight(40);
        selectMessagesBtn.setOnAction(e -> {
            setSelectionModeActive(true);
        });
        controlsWindowRootVB.getChildren().add(selectMessagesBtn);

        if (selectedChannelUser.getChannel().isDisable_sharing()) {
            CustomButton addSharingBtn = new CustomButton(
                    new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/example/speech/image/forward.png")))
                    , "Разрешить пересылку");
            addSharingBtn.setPrefWidth(vBoxWidth);
            addSharingBtn.setPrefHeight(40);
            addSharingBtn.setOnAction(e -> {
                Channel channel = selectedChannelUser.getChannel();
                channel.setDisable_sharing(false);
                new ChannelService().update(channel);
            });
            controlsWindowRootVB.getChildren().add(addSharingBtn);
        } else {
            CustomButton disableSharingBtn = new CustomButton(
                    new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/example/speech/image/disable_sharing.png")))
                    , "Запретить пересылку");
            disableSharingBtn.setPrefWidth(vBoxWidth);
            disableSharingBtn.setPrefHeight(40);
            disableSharingBtn.setOnAction(e -> {
                Channel channel = selectedChannelUser.getChannel();
                channel.setDisable_sharing(true);
                new ChannelService().update(channel);
            });
            controlsWindowRootVB.getChildren().add(disableSharingBtn);
        }

        CustomButton deleteChatBtn = new CustomButton(
                new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/example/speech/image/delete.png")))
                , "Отчистить переписку");
        deleteChatBtn.setPrefWidth(vBoxWidth);
        deleteChatBtn.setPrefHeight(40);
        VBox.setMargin(deleteChatBtn, new Insets(0, 0, 5, 0));
        deleteChatBtn.setOnAction(e -> {
            List<Message> allMessage = messageService.getAllMessageInChannel(
                    selectedChannelUser.getChannel().getChannelID());
            if(allMessage != null && !allMessage.isEmpty())
                new ConfirmationOfMessageDeletion().initializeShape(this, allMessage);
        });
        controlsWindowRootVB.getChildren().add(deleteChatBtn);

        if(removeControlsWindow == null) {
            removeControlsWindow = e -> {
                    rightSP.getChildren().removeIf(node -> node.getUserData() != null && node.getUserData().equals("controlsWindowRootVB"));
            };

            messagesSP.addEventFilter(MouseEvent.MOUSE_PRESSED, removeControlsWindow);
        }
    }

    private void updateEmojiWindowPosition() {
        if (emojiSP == null) return;

        double top = 40;
        double bottom = 0;
        double right = 0;
        double left = 0;

        double messageAnchorHeight = messageAnchor.getHeight();

        if (pinnedMessagesHB.isVisible()) {
            top += 40;
        }

        if (updateMessageHB.isVisible()) {
            bottom += 40;
        }

        if(fileListView.isVisible())
            bottom += 70;

        bottom += messageAnchorHeight;

        Insets newMargin = new Insets(top, right, bottom, left);
        StackPane.setMargin(emojiSP, newMargin);

        emojiSP.maxHeightProperty().bind(rightSP.heightProperty().subtract(top + bottom));
    }

    public void activateSelectionModeWithMessage(Message msg) {
        if (selectionModeActive) {
            toggleMessageSelection(msg);
            return;
        }
        selectedMessages.clear();
        selectedMessages.add(msg);
        setSelectionModeActive(true);
        updateAllMessageCellsSelection();
    }

    public void handleMessageClick(Message msg, MouseEvent event) {
        if (selectionModeActive) {
            toggleMessageSelection(msg);
        } else {
            activateSelectionModeWithMessage(msg);
        }
        event.consume();
    }

    public void toggleMessageSelection(Message message) {
        if (!selectionModeActive) return;
        if (selectedMessages.contains(message)) {
            selectedMessages.remove(message);
            if (selectedMessages.isEmpty()) {
                setSelectionModeActive(false);
            }
        } else
            selectedMessages.add(message);
        TextMessageCellController controller = messageCellCreator.getControllerCache().get(message);
        if (controller != null)
            controller.setSelected(selectedMessages.contains(message));
    }

    private void updateAllMessageCellsSelection() {
        for (Map.Entry<Message, TextMessageCellController> entry :
                messageCellCreator.getControllerCache().entrySet()) {
            entry.getValue().setSelected(selectedMessages.contains(entry.getKey()));
        }
    }
}