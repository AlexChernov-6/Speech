package com.example.speech.control;

import com.example.speech.model.*;
import com.example.speech.service.*;
import com.example.speech.util.FileUtils;
import com.example.speech.util.HelpfulClass;
import com.example.speech.util.HelpfulStylingClass;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.*;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
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
import java.util.concurrent.CompletableFuture;

import javafx.scene.control.IndexedCell;

import static com.example.speech.control.EntranceController.CONFIG_MANAGER;
import static com.example.speech.control.EntranceController.EMOJI_LIST;
import static com.example.speech.util.HelpfulStylingClass.applyPromptWithTF;
import static com.example.speech.util.HelpfulStylingClass.setupFullScreenListener;

public class SpeechBaseController {
    private Stage stage;
    public static User currentUser;

    private final Image defaultBackgroundImage = new Image(Objects.requireNonNull(
            getClass().getResourceAsStream("/com/example/speech/image/messages-list-view-background-default.jpg")));

    @FXML
    public ListView<ChannelUser> chatsView;

    public ObservableList<ChannelUser> userChats;

    private ListView<File> fileListView;

    private HBox topSearchModeHB;

    private SearchChannelWindow searchChannelWindow;

    private final ChannelUserService channelUserService = new ChannelUserService();
    private final MessageService messageService = new MessageService();

    private FilteredList<Message> filteredList;

    private final List<Node> hiddenList = new ArrayList<>();

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
    private HBox messageHB;
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

    private String currentSearchText = null;

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

    private List<Message> baseSelection = new ArrayList<>();

    private VirtualFlow<?> currentFlow;

    private int lastProcessedDragIndex = -1;
    private boolean dragStartSelected = false;

    private ObservableList<Message> messages = FXCollections.observableArrayList();

    private MessageListener messageListener;

    private EventHandler<MouseEvent> removeControlsWindow;

    @FXML
    private Button searchSubstringInMessageBtn, createControlsWindowBtn, showAllPinnedMessagesBtn, hideUpdateMessageHBBtn, addFileBtn;

    private final List<Node> hiddenNodesOnSelection = new ArrayList<>();

    private final List<Node> hiddenNodesOnPinned = new ArrayList<>();

    private final UsersInSilentModeService usersInSilentModeService = new UsersInSilentModeService();

    private final MessageContentService messageContentService = new MessageContentService();

    private SortedList<File> fileSortedList;

    public final List<File> delFiles = new ArrayList<>();

    @FXML
    private StackPane leftSP;

    private ProfileWindow profileWindow;

    private CreateChannelWindow createChannelWindow;

    private OtherProfileWindow otherProfileWindow;

    private ChannelGroupWindow channelGroupWindow;

    private final ChannelService channelService = new ChannelService();

    private final HiddenChannelUserService hiddenChannelUserService = new HiddenChannelUserService();

    private VBox controlsWindowRootVB;
    private CustomButton searchBtn;
    private CustomButton editBtn;
    private CustomButton muteBtn;
    private CustomButton enableNotificationsBtn;
    private CustomButton selectMessagesBtn;
    private CustomButton selectBackgroundListViewBtn;
    private CustomButton setDefaultBackgroundListViewBtn;
    private CustomButton addSharingBtn;
    private CustomButton disableSharingBtn;
    private CustomButton deleteChatBtn;

    public void initializeData(Stage stage, User currentUser) {
        this.stage = stage;
        this.currentUser = currentUser;

        Thread updateStatusThread = new Thread(() -> {
            currentUser.setStatusUser("в сети");
            new UserService().update(currentUser);

            List<Channel> channels = channelUserService.getAllChatsByUser(currentUser).stream()
                    .map(cU -> cU.getChannel())
                    .filter(channel -> channel.getChannelType().getChannelTypeId() == 3)
                    .toList();
            for (Channel channel : channels) {
                ChannelUser channelUser = channelUserService
                        .getInterlocutorUserChannelInChannel(channel.getChannelID(), currentUser.getIdUser());
                channelUser.setStatusOfTheInterlocutor(currentUser.getStatusUser());
                channelUserService.update(channelUser);
            }
        });
        updateStatusThread.setDaemon(true);
        updateStatusThread.start();

        applyPromptWithTF(messageTA);
        messages.addListener((ListChangeListener<Message>) change -> {
            while (change.next()) {
                if (change.wasAdded() || change.wasRemoved()) {
                    Platform.runLater(() -> {
                        if (!messagesLV.getItems().isEmpty()) {
                            messagesLV.scrollTo(messagesLV.getItems().size());
                        }
                    });
                    refreshVisibleMessageCells();
                }
            }
        });

        HelpfulClass.setImageWithButton(searchSubstringInMessageBtn, "imageSearchButton.png");
        HelpfulClass.setImageWithButton(createControlsWindowBtn, "imageMenuButton.png");

        HelpfulClass.setImageWithButton(showAllPinnedMessagesBtn, "pin.png");

        double hideUpdateMessageHBBtnSize = 30;

        HelpfulClass.setImageWithButton(
                hideUpdateMessageHBBtn, "image-delete-chat-not-focused.png", "delete-chat-btn",
                hideUpdateMessageHBBtnSize, hideUpdateMessageHBBtnSize);

        hideUpdateMessageHBBtn.setOnMouseEntered(e -> {
            HelpfulClass.setImageWithButton(
                    hideUpdateMessageHBBtn, "image-delete-chat-focused.png", "delete-chat-btn",
                    hideUpdateMessageHBBtnSize, hideUpdateMessageHBBtnSize);
        });

        hideUpdateMessageHBBtn.setOnMouseExited(e -> {
            HelpfulClass.setImageWithButton(
                    hideUpdateMessageHBBtn, "image-delete-chat-not-focused.png", "delete-chat-btn",
                    hideUpdateMessageHBBtnSize, hideUpdateMessageHBBtnSize);
        });

        HelpfulClass.setImageWithButton(addFileBtn, "imageAddFileButton.png");
        HelpfulClass.setImageWithButton(sendMessageBtn, "imageSendButton.png");
        HelpfulClass.setImageWithButton(emojiBtn, "imageEmojiButton.png");

        filteredList = new FilteredList<>(messages, m -> true);

        messagesLV.setItems(filteredList);
        setupFullScreenListener(stage, rootAnchorPane);
        initializeListViewChats();
        setupMessageTextAreaListener();

        messageListener = new MessageListener("jdbc:postgresql://192.168.1.103:5432/speechdb"
                , currentUser.getNameUser(), currentUser.getPasswordUser(),
                messageID -> {
                    Platform.runLater(() -> messagesListViewRefresh(messageID));
                }, userChats,
                this);

        Thread listenerThread = new Thread(messageListener, "pg-listener");
        listenerThread.setDaemon(true);
        listenerThread.start();

        messagesSP.widthProperty().addListener((ch, oldValue, newValue) -> {
            double newWidth = newValue.doubleValue() - 310;

            if (newWidth <= 450) {
                if (selectedChatVB.isVisible()) {
                    AnchorPane.setLeftAnchor(rightSP, 0.0);
                    leftVB.setVisible(false);
                }
            } else {
                AnchorPane.setLeftAnchor(rightSP, 310.0);
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
                    System.err.println(e.getMessage());
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
            if (e.isShiftDown() && e.getCode() == KeyCode.ENTER) {
                int caretPos = messageTA.getCaretPosition();
                messageTA.insertText(caretPos, "\n");
                return;
            }

            if (e.getCode() == KeyCode.ENTER && sendMessageBtn.isVisible())
                sendMessageBtn.fire();
        });

        selectedFile = FXCollections.observableArrayList();
        fileSortedList = new SortedList<>(selectedFile, (f1, f2) -> Long.compare(f1.length(), f2.length()));
        fileListView = new ListView<>();
        fileListView.setMaxHeight(75);
        fileListView.setMinHeight(75);
        fileListView.setPrefHeight(75);
        fileListView.setVisible(false);
        fileListView.setManaged(false);
        fileListView.setSelectionModel(null);
        fileListView.setOrientation(Orientation.HORIZONTAL);
        fileListView.prefWidthProperty().bind(selectedChatVB.widthProperty());
        fileListView.setCellFactory(f -> new FileCell(this));
        fileListView.setItems(fileSortedList);
        selectedChatVB.getChildren().add(4, fileListView);

        selectedFile.addListener((ListChangeListener<File>) change -> {
            while (change.next()) {
            }
            int size = selectedFile.size();
            Platform.runLater(() -> {
                if (size > 0) {
                    sendMessageBtn.setVisible(true);
                    sendMessageBtn.setManaged(true);
                    if (fileListView != null) {
                        fileListView.setVisible(true);
                        fileListView.setManaged(true);
                    }
                    delFiles.clear();
                    Platform.runLater(() -> {
                        for (int i = 10; i < size; i++)
                            if (!delFiles.contains(fileSortedList.get(i)))
                                delFiles.add(fileSortedList.get(i));

                        fileListView.refresh();
                    });
                } else {
                    if (!updateMessageHB.isVisible() || (updateMessageHB.isVisible() && contextPopUpBar != ContextPopUpBar.FORWARD_MESSAGE)) {
                        if (messageTA != null && (messageTA.getText() == null || messageTA.getText().isEmpty()
                                || messageTA.getText().equals("Сообщение..."))) {
                            sendMessageBtn.setVisible(false);
                            sendMessageBtn.setManaged(false);
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
                        System.err.println(ex.getMessage());
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
                    if (!selectionModeActive)
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

        Platform.runLater(() -> {
            HelpfulStylingClass.scrollPaneAnimation(messagesLV);
            HelpfulStylingClass.scrollPaneAnimation(chatsView);
            HelpfulStylingClass.scrollPaneAnimation(fileListView, true);
        });

        createLeftUserPane();

        channelStateAP.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.PRIMARY) {
                List<User> allUserInSelectedChat = channelService.getAllUserInChannel(selectedChannelUser.getChannel().getChannelID());
                if (selectedChannelUser.getChannel().getChannelType().getChannelTypeId() == 3) {
                    if (otherProfileWindow == null)
                        otherProfileWindow = new OtherProfileWindow(this);

                    otherProfileWindow.showOtherProfileWindow(
                            allUserInSelectedChat.stream().filter(u -> !u.equals(currentUser)).toList().getFirst());
                } else {
                    if (channelGroupWindow == null)
                        channelGroupWindow = new ChannelGroupWindow(this);

                    channelGroupWindow.showChannelGroupWidow(allUserInSelectedChat);
                }
            }
        });
    }

    public void initializeListViewChats() {
        chatsView.setFixedCellSize(60);
        chatsView.setCellFactory(lv -> new ListChannelsCellController(this));
        userChats = FXCollections.observableArrayList(channelUserService.getAllChatsByUser(currentUser)
                .stream().filter(cU -> hiddenChannelUserService.isHiddenUserFromChannel(cU.getChannel(), currentUser) == null).toList());
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
        Platform.runLater(() -> {
            messageCellCreator.clearCache();
            selectedChatVB.setVisible(true);
            channelName.setText(selectedChat.getVisibleNameChat());
            messageTA.setText("");
            if (selectedChat.getChannel().getChannelType().getChannelTypeId() == 3)
                channelStatus.setText(selectedChat.getStatusOfTheInterlocutor());
            else
                channelStatus.setText(String.format("Число участников: %d", selectedChat.getChannel().getChannelCountUser()));
            messageTA.requestFocus();

            hideTheListOfPinnedMessages();

            hideEmojiWindow();

            updateVisibleChangeMessageHB();

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


            if (pinnedMessagesHB.isVisible()) {
                pinnedMessagesHB.setVisible(false);
                pinnedMessagesHB.setManaged(false);
            }
            lastPinnedMessage = null;

            if (selectedChat.getBackgroundImageBytes() != null && selectedChat.getBackgroundImageBytes().length >= 1)
                messagesLV.setBackground(new Background(new BackgroundImage(
                        selectedChannelUser.getBackgroundImage(),
                        BackgroundRepeat.NO_REPEAT,
                        BackgroundRepeat.NO_REPEAT,
                        BackgroundPosition.CENTER,
                        BackgroundSize.DEFAULT)));
            else
                messagesLV.setBackground(new Background(new BackgroundImage(
                        defaultBackgroundImage,
                        BackgroundRepeat.REPEAT,
                        BackgroundRepeat.REPEAT,
                        BackgroundPosition.CENTER,
                        BackgroundSize.DEFAULT)));

            setSelectionModeActive(false);
        });

        Thread loadChannelMessagesThread = new Thread(() -> {
            List<Message> messagesList = messageService.getAllMessageInChannel(
                    selectedChannelUser.getChannel().getChannelID());
            List<Message> filteredAll = messagesList.stream()
                    .filter(message -> !message.getDeletedByUsers()
                            .contains(Long.valueOf(currentUser.getIdUser())))
                    .toList();

            int startCountMessages = 20;
            int total = filteredAll.size();
            int takeLast = Math.min(total, startCountMessages);
            List<Message> remaining;
            try {
                remaining = new ArrayList<>(filteredAll.subList(0, total - startCountMessages));
            } catch (IllegalArgumentException e) {
                remaining = null;
            }

            try {
                Thread.sleep(150);
            } catch (InterruptedException ignore) {
            }
            List<Message> finalRemaining = remaining;
            Platform.runLater(() -> {
                messages.clear();
                for (int i = total - takeLast; i < total; i++) {
                    messages.add(filteredAll.get(i));
                }

                PauseTransition pauseTransition = new PauseTransition(Duration.millis(400));
                pauseTransition.play();
                pauseTransition.setOnFinished(e -> {
                    if (!messagesLV.getItems().isEmpty())
                        messagesLV.scrollTo(messagesLV.getItems().size());
                    if (firstVisible == 0)
                        setPinnedMessagesHBVisible(messagesLV.getItems().size());
                    else
                        setPinnedMessagesHBVisible(firstVisible);
                    messagesLV.refresh();
                });

                ChannelUser channelUser = selectedChannelUser;

                if (finalRemaining != null && !finalRemaining.isEmpty()) {
                    Thread addRemainingThread = new Thread(() -> {
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException ignored) {
                        }
                        if (channelUser.equals(selectedChannelUser)) {
                            Platform.runLater(() -> {
                                messages.addAll(0, finalRemaining);
                                setPinnedMessagesHBVisible(messagesLV.getItems().size());
                            });
                        }
                    });
                    addRemainingThread.setDaemon(true);
                    addRemainingThread.start();
                }
            });
        });

        loadChannelMessagesThread.setDaemon(true);
        loadChannelMessagesThread.start();
    }

    private void setupMessageTextAreaListener() {
        messageTA.textProperty().addListener((observable, oldValue, newValue) -> {
            boolean hasVisibleText = newValue != null &&
                    !newValue.trim().isEmpty() &&
                    !newValue.matches("^[\\n\\r\\s]*$") &&
                    !newValue.equals("Сообщение...");

            if (!updateMessageHB.isVisible() || (updateMessageHB.isVisible() && contextPopUpBar != ContextPopUpBar.FORWARD_MESSAGE)) {
                if (!hasVisibleText) {
                    if (selectedFile == null || selectedFile.isEmpty()) {
                        sendMessageBtn.setVisible(false);
                        sendMessageBtn.setManaged(false);
                    }
                } else {
                    sendMessageBtn.setVisible(true);
                    sendMessageBtn.setManaged(true);
                }
            }

            adjustTextAreaHeight(newValue);
        });
    }

    private void adjustTextAreaHeight(String newValue) {
        int countLinesNewValue = countTextAreaLines(messageTA);

        int setMinHeightByCountLines = Math.max(countLinesNewValue, 2) * 20;

        if (countLinesNewValue > countLinesOldValue) {
            messageHB.setMinHeight(Math.min(setMinHeightByCountLines, 200));
        } else if (countLinesNewValue < countLinesOldValue) {
            messageHB.setMinHeight(Math.max(40, Math.min(setMinHeightByCountLines, 200)));
        }

        if (newValue == null || newValue.trim().isEmpty()) {
            messageHB.setMinHeight(40);
            messageHB.setPrefHeight(40);
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
        sendMessageBtn.setManaged(false);
        sendMessageBtn.setVisible(false);

        // ----------------------- ОБЫЧНАЯ ОТПРАВКА -----------------------
        if (((!text.isEmpty() && !text.equals("Сообщение...")) || (selectedFile != null && !selectedFile.isEmpty()))
                && chatsView.getSelectionModel().getSelectedItem() != null
                && (!updateMessageHB.isVisible() || contextPopUpBar == ContextPopUpBar.REPLY_MESSAGE)) {

            ChannelUser selectedChat = chatsView.getSelectionModel().getSelectedItem();

            Message messageToSave = new Message();
            addContentsForMessage(messageToSave, text);
            messageToSave.setChannelUser(selectedChat);
            messageToSave.setMessageDatetime(LocalDateTime.now());
            boolean replyMessage = updateMessageHB.isVisible() && contextPopUpBar == ContextPopUpBar.REPLY_MESSAGE;
            if (replyMessage)
                messageToSave.setMessageIdReplyTo(messageIdReplyTo);

            Platform.runLater(() -> {
                if (replyMessage) updateVisibleChangeMessageHB();
                messages.add(messageToSave);
                messagesLV.scrollTo(messagesLV.getItems().size());
            });

            Thread sendThread = new Thread(() -> {
                try {
                    Thread.sleep(150);
                } catch (InterruptedException ignore) {
                }
                try {
                    boolean saved = messageService.save(messageToSave);
                    if (saved) {
                        messageToSave.setMessageStatus("отправлено");
                        messageService.update(messageToSave);
                    }
                } catch (Exception e) {
                    messageToSave.setMessageStatus("ошибка отправки");
                }
            });
            sendThread.setDaemon(true);
            sendThread.start();

            return;
        }

        // ----------------------- ИЗМЕНЕНИЕ СООБЩЕНИЯ -----------------------
        if (((!text.isEmpty() && !text.equals("Сообщение...")) || (selectedFile != null && !selectedFile.isEmpty()))
                && chatsView.getSelectionModel().getSelectedItem() != null
                && updateMessageHB.isVisible()
                && contextPopUpBar == ContextPopUpBar.CHANGE_MESSAGE) {
            Platform.runLater(this::updateVisibleChangeMessageHB);


            String oldText = updateMessage.getMessageString();
            List<File> oldMessageContentList = updateMessage.getMessageContent().stream()
                    .map(mC -> FileUtils.getFileFromDefaultDir(mC.getMessageContentFileName()))
                    .sorted((f1, f2) -> {
                        assert f1 != null;
                        return f1.getName().compareTo(f2.getName());
                    })
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
                updateMessage = null;
                addContentsForMessage(messageToUpdate, text);
                messageToUpdate.setModifiedMessage(true);

                Thread sendThread = new Thread(() -> {
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException ignore) {
                    }

                    messageService.update(messageToUpdate);
                });
                sendThread.setDaemon(true);
                sendThread.start();

                return;
            }
        }

        // ----------------------- Пересылка -----------------------
        if (chatsView.getSelectionModel().getSelectedItem() != null
                && updateMessageHB.isVisible()
                && contextPopUpBar == ContextPopUpBar.FORWARD_MESSAGE) {

            Platform.runLater(this::updateVisibleChangeMessageHB);

            List<Message> tempMessages = new ArrayList<>();

            for (Message original : forwardMessages) {
                Message temp = new Message();
                temp.setMessageDatetime(LocalDateTime.now());
                temp.setMessageString(original.getMessageString());
                temp.setMessageContent(original.getMessageContent());
                temp.setChannelUser(selectedChannelUser);
                temp.setMessageStatus("загружается");
                temp.setForwardedFrom((long) original.getChannelUser().getUser().getIdUser());
                tempMessages.add(temp);
            }

            boolean addNewMessage = ((!text.isEmpty() && !text.equals("Сообщение...")) || (selectedFile != null && !selectedFile.isEmpty()))
                    && chatsView.getSelectionModel().getSelectedItem() != null;
            Message messageToSave = new Message();
            if(addNewMessage) {
                ChannelUser selectedChat = chatsView.getSelectionModel().getSelectedItem();
                addContentsForMessage(messageToSave, text);
                messageToSave.setChannelUser(selectedChat);
                messageToSave.setMessageDatetime(LocalDateTime.now());
                boolean replyMessage = updateMessageHB.isVisible() && contextPopUpBar == ContextPopUpBar.REPLY_MESSAGE;
                if (replyMessage)
                    messageToSave.setMessageIdReplyTo(messageIdReplyTo);
            }

            Platform.runLater(() -> {
                if(addNewMessage)
                    messages.add(messageToSave);
                messages.addAll(tempMessages);
                messagesLV.scrollTo(messagesLV.getItems().size());
            });

            // Асинхронное сохранение
            Thread sendThread = new Thread(() -> {
                try {
                    Thread.sleep(150);
                } catch (InterruptedException ignore) {
                }
                if(addNewMessage) {
                    try {
                        boolean saved = messageService.save(messageToSave);
                        if (saved) {
                            messageToSave.setMessageStatus("отправлено");
                            messageService.update(messageToSave);
                        }
                    } catch (Exception e) {
                        messageToSave.setMessageStatus("ошибка отправки");
                    }
                }

                for (Message newMsg : tempMessages) {
                    List<MessageContent> newContents = null;
                    if (newMsg.getMessageContent() != null) {
                        newContents = new ArrayList<>();
                        for (MessageContent origContent : newMsg.getMessageContent()) {
                            String fileName = origContent.getMessageContentFileName();
                            Path localFile = FileUtils.DEFAULT_STORAGE_DIR.resolve(fileName);
                            byte[] bytes;
                            if (Files.exists(localFile)) {
                                try {
                                    bytes = FileUtils.readFileToByteArrayStream(localFile.toFile());
                                } catch (IOException e) {
                                    bytes = new byte[0];
                                }
                            } else {
                                bytes = messageContentService.getContentBytes(origContent.getMessageContentId());
                            }
                            if (bytes != null) {
                                MessageContent newContent = new MessageContent();
                                newContent.setMessageContentBytes(bytes);
                                newContent.setMessageContentFileName(fileName);
                                newContents.add(newContent);
                            }
                        }
                    }
                    if (newContents != null)
                        newMsg.setMessageContent(newContents);

                    boolean saved = messageService.save(newMsg);
                    if (saved) {
                        Message savedMsg = messageService.getRowById(newMsg.getMessageId());
                        savedMsg.setMessageStatus("отправлено");
                        messageService.update(savedMsg);
                    } else
                        Platform.runLater(() -> newMsg.setMessageStatus("ошибка отправки"));
                }
            });
            sendThread.setDaemon(true);
            sendThread.start();
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
        if (messagesLV.getItems().isEmpty()) return;
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
                (lastPinnedMessage.getMessageContent() != null && !lastPinnedMessage.getMessageContent().isEmpty()) ||
                (lastPinnedMessage.getChannelInvitations() != null)) {
            String contentMessage = "Содержимое";

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
            } else if (lastPinnedMessage.getChannelInvitations() != null)
                contentMessage = "Приглашение на вступление в " + lastPinnedMessage.getChannelInvitations().getChannel_name_unique();

            if (contentMessage.length() > 50) {
                contentMessage = contentMessage.substring(0, 47) + "...";
            }
            contentPinnedMessageLB.setText(contentMessage);
        }
    }

    private void scrollToMessage(Message message) {
        messagesLV.scrollTo(message);
        PauseTransition pause = new PauseTransition(Duration.millis(100));
        pause.setOnFinished(e -> {
            if (messageCellCreator.getControllerCache(message) != null)
                messageCellCreator.getControllerCache(message).highlightMessageTemporarily();
        });
        pause.play();
    }

    @FXML
    private void showPinnedContent() {
        flag = false;

        filteredList.setPredicate(mes -> mes != null && Boolean.TRUE.equals(mes.getPinMessage()));

        hiddenNodesOnPinned.clear();

        for (Node node : channelStateAP.getChildren()) {
            if (node.isVisible()) {
                hiddenNodesOnPinned.add(node);
                node.setVisible(false);
                node.setManaged(false);
            }
        }

        if (backBtn == null) {
            backBtn = new Button();
            backBtn.setId("backBtn");
            HelpfulClass.setImageWithButton(backBtn, "back.png");
            backBtn.setOnAction(e -> {
                hideTheListOfPinnedMessages();
            });
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
            countPinnedMessages.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #00C49A;");
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
            hiddenNodesOnPinned.add(updateMessageHB);
            updateMessageHB.setVisible(false);
            updateMessageHB.setManaged(false);
        }

        for (Node node : messageHB.getChildren()) {
            hiddenNodesOnPinned.add(node);
            node.setVisible(false);
            node.setManaged(false);
        }

        if (unpinnedAllMessagesBtn == null) {
            unpinnedAllMessagesBtn = new Button();
            unpinnedAllMessagesBtn.setId("unpinnedAllMessagesBtn");
            unpinnedAllMessagesBtn.prefWidthProperty().bind(messageHB.widthProperty());
            unpinnedAllMessagesBtn.setPrefHeight(40);
            unpinnedAllMessagesBtn.getStyleClass().add("unpin-all-btn");
            HBox.setHgrow(unpinnedAllMessagesBtn, Priority.ALWAYS);
            unpinnedAllMessagesBtn.setOnAction(e -> {
                new ConfirmationOfMessageDeletion().initializeShape(this);
            });
            messageHB.getChildren().add(unpinnedAllMessagesBtn);
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

        for (Node node : hiddenNodesOnPinned) {
            node.setVisible(true);
            node.setManaged(true);
        }

        setPinnedMessagesHBVisible(firstVisible);

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

    public ProfileWindow getProfileWindow() {
        return profileWindow;
    }

    public void setProfileWindow(ProfileWindow profileWindow) {
        this.profileWindow = profileWindow;
    }

    public CreateChannelWindow getCreateChannelWindow() {
        return createChannelWindow;
    }

    public void setCreateChannelWindow(CreateChannelWindow createChannelWindow) {
        this.createChannelWindow = createChannelWindow;
    }

    public OtherProfileWindow getOtherProfileWindow() {
        return otherProfileWindow;
    }

    public void setOtherProfileWindow(OtherProfileWindow otherProfileWindow) {
        this.otherProfileWindow = otherProfileWindow;
    }

    public ChannelGroupWindow getChannelGroupWindow() {
        return channelGroupWindow;
    }

    public void setChannelGroupWindow(ChannelGroupWindow channelGroupWindow) {
        this.channelGroupWindow = channelGroupWindow;
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
            hiddenNodesOnSelection.clear();
            for (Node node : channelStateAP.getChildren()) {
                if (node.isVisible()) {
                    hiddenNodesOnSelection.add(node);
                    node.setVisible(false);
                    node.setManaged(false);
                }
            }
            for (Node node : channelStateAP.getChildren()) {
                node.setVisible(false);
                node.setManaged(false);
            }

            selectionForwardBtn = new Button("ПЕРЕСЛАТЬ");
            selectionForwardBtn.getStyleClass().add("selected-message-mode-buttons");
            selectionForwardBtn.setOnAction(e -> {
                forwardMessages.setAll(selectedMessages);
                new ChatSelectionController(this, forwardMessages);
                setSelectionModeActive(false);
            });
            selectionForwardBtn.setPrefWidth(120);
            AnchorPane.setLeftAnchor(selectionForwardBtn, 10.0);
            AnchorPane.setTopAnchor(selectionForwardBtn, 5.0);
            AnchorPane.setBottomAnchor(selectionForwardBtn, 5.0);

            selectionDeleteBtn = new Button("УДАЛИТЬ");
            selectionDeleteBtn.getStyleClass().add("selected-message-mode-buttons");
            selectionDeleteBtn.setOnAction(e -> {
                new ConfirmationOfMessageDeletion().initializeShape(channelName.getText(), this
                        , selectedMessages);
            });
            selectionDeleteBtn.setPrefWidth(120);
            AnchorPane.setLeftAnchor(selectionDeleteBtn, 140.0);
            AnchorPane.setTopAnchor(selectionDeleteBtn, 5.0);
            AnchorPane.setBottomAnchor(selectionDeleteBtn, 5.0);

            selectionCancelBtn = new Button("ОТМЕНА");
            selectionCancelBtn.getStyleClass().add("selected-message-mode-buttons");
            selectionCancelBtn.setOnAction(e -> {
                setSelectionModeActive(false);
            });
            selectionCancelBtn.setPrefWidth(120);
            AnchorPane.setRightAnchor(selectionCancelBtn, 10.0);
            AnchorPane.setTopAnchor(selectionCancelBtn, 5.0);
            AnchorPane.setBottomAnchor(selectionCancelBtn, 5.0);
            if (selectedChannelUser.getChannel().isDisable_sharing()) {
                AnchorPane.setLeftAnchor(selectionDeleteBtn, 10.0);
                AnchorPane.setTopAnchor(selectionDeleteBtn, 5.0);
                AnchorPane.setBottomAnchor(selectionDeleteBtn, 5.0);
                channelStateAP.getChildren().addAll(selectionDeleteBtn, selectionCancelBtn);
            } else
                channelStateAP.getChildren().addAll(selectionForwardBtn, selectionDeleteBtn, selectionCancelBtn);
        } else {
            channelStateAP.getChildren().removeAll(selectionForwardBtn, selectionDeleteBtn, selectionCancelBtn);
            for (Node node : hiddenNodesOnSelection) {
                node.setVisible(true);
                node.setManaged(true);
            }
            hiddenNodesOnSelection.clear();
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

        topSearchModeHB = new HBox(10);
        topSearchModeHB.getStyleClass().add("search-top-pane");
        topSearchModeHB.setPadding(new Insets(0, 10, 0, 10));
        StackPane.setAlignment(topSearchModeHB, Pos.TOP_LEFT);
        topSearchModeHB.setPrefHeight(40);
        topSearchModeHB.setMaxHeight(40);
        topSearchModeHB.setAlignment(Pos.CENTER_LEFT);
        rightSP.getChildren().add(topSearchModeHB);

        Button closeSearchModeBtn = new Button();
        topSearchModeHB.getChildren().add(closeSearchModeBtn);

        HelpfulClass.setImageWithButton(closeSearchModeBtn, "arrow.png", "close-search-btn", 30, 30);

        TextField searchFromChatTF = new TextField();
        searchFromChatTF.setPromptText("Поиск...");
        searchFromChatTF.setAlignment(Pos.CENTER_LEFT);
        searchFromChatTF.setPrefHeight(35);
        applyPromptWithTF(searchFromChatTF);
        HBox.setHgrow(searchFromChatTF, Priority.ALWAYS);
        topSearchModeHB.getChildren().add(searchFromChatTF);
        searchFromChatTF.requestFocus();
        searchFromChatTF.getStyleClass().setAll("search-text-field");

        Button searchContainsStrFromMessage = new Button();
        topSearchModeHB.getChildren().add(searchContainsStrFromMessage);
        searchFromChatTF.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER)
                searchContainsStrFromMessage.fire();
        });

        HelpfulClass.setImageWithButton(searchContainsStrFromMessage, "imageSearchButton.png", "search-action-btn", 30, 30);

        bottomSearchModeHB = new HBox(10);
        bottomSearchModeHB.getStyleClass().add("search-bottom-pane");
        bottomSearchModeHB.setPadding(new Insets(0, 10, 0, 10));
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

        Label countResult = new Label();
        countResult.getStyleClass().add("count-result-label");
        countResult.setAlignment(Pos.CENTER_LEFT);
        countResult.minWidthProperty().bind(bottomSearchModeHB.widthProperty().subtract(150));
        countResult.setMinHeight(35);
        bottomSearchModeHB.getChildren().add(countResult);

        Button listOfBtn = new Button("Списком");
        listOfBtn.setDisable(true);
        bottomSearchModeHB.getChildren().add(listOfBtn);
        listOfBtn.getStyleClass().setAll("list-of-btn");

        buttonsVB = new VBox(10);
        StackPane.setAlignment(buttonsVB, Pos.BOTTOM_RIGHT);
        buttonsVB.setVisible(false);
        StackPane.setMargin(buttonsVB, new Insets(0, 10, 10 + 40, 0));
        buttonsVB.setPrefWidth(40);
        buttonsVB.setMaxWidth(40);
        buttonsVB.setAlignment(Pos.CENTER);
        buttonsVB.setMaxHeight(Region.USE_PREF_SIZE);
        buttonsVB.getStyleClass().add("nav-buttons-box");
        buttonsVB.setPadding(new Insets(5, 0, 5, 0));
        rightSP.getChildren().add(buttonsVB);

        Button upBtn = new Button();
        upBtn.setVisible(false);
        upBtn.setManaged(false);
        buttonsVB.getChildren().add(upBtn);

        HelpfulClass.setImageWithButton(upBtn, "up.png", "nav-btn", 30, 30);

        Button downBtn = new Button();
        buttonsVB.getChildren().add(downBtn);
        downBtn.setVisible(false);
        downBtn.setManaged(false);
        downBtn.setOnAction(e -> {
            //Кнопка вниз
            currInd -= 1;
            scrollToMessage(resultList.get(resultList.size() - currInd));
            if (currInd == resultList.size()) {
                upBtn.setVisible(false);
                upBtn.setManaged(false);
            } else {
                upBtn.setVisible(true);
                upBtn.setManaged(true);
            }
            if (currInd == 1) {
                downBtn.setVisible(false);
                downBtn.setManaged(false);
            } else {
                downBtn.setVisible(true);
                downBtn.setManaged(true);
            }
            countResult.setText(currInd + "/" + resultList.size());
            Platform.runLater(() -> {
                PauseTransition pauseTransition = new PauseTransition(Duration.millis(100));
                pauseTransition.setOnFinished(e1 -> {
                    messageCellCreator.getControllerCache().get(resultList.get(resultList.size() - currInd)).highlightText(searchFromChatTF.getText());
                });
                pauseTransition.playFromStart();
            });
        });
        upBtn.setOnAction(e -> {
            //Кнопка вверх
            currInd += 1;
            scrollToMessage(resultList.get(resultList.size() - currInd));
            if (currInd == resultList.size()) {
                upBtn.setVisible(false);
                upBtn.setManaged(false);
            } else {
                upBtn.setVisible(true);
                upBtn.setManaged(true);
            }
            if (currInd == 1) {
                downBtn.setVisible(false);
                downBtn.setManaged(false);
            } else {
                downBtn.setVisible(true);
                downBtn.setManaged(true);
            }
            countResult.setText(currInd + "/" + resultList.size());
            Platform.runLater(() -> {
                PauseTransition pauseTransition = new PauseTransition(Duration.millis(100));
                pauseTransition.setOnFinished(e1 -> {
                    messageCellCreator.getControllerCache().get(resultList.get(resultList.size() - currInd)).highlightText(searchFromChatTF.getText());
                });
                pauseTransition.playFromStart();
            });
        });

        HelpfulClass.setImageWithButton(downBtn, "down.png", "nav-btn", 30, 30);

        listOfBtn.setOnAction(e -> {
            //Вывод только нужных сообщений
            if (listOfBtn.getText().equals("Списком")) {
                filteredList.setPredicate(message -> message.getMessageString() != null && message.getMessageString().toLowerCase()
                        .contains(searchFromChatTF.getText().trim().toLowerCase()));
                listOfBtn.setText("Чатом");
                countResult.setText("Всего записей: " + resultList.size());
                buttonsVB.setVisible(false);
            } else {
                resultList = messages.stream().filter(message ->
                        message.getMessageString() != null &&
                                message.getMessageString().toLowerCase().contains(
                                        searchFromChatTF.getText().trim().toLowerCase())).toList();
                filteredList.setPredicate(message -> true);
                listOfBtn.setText("Списком");
                scrollToMessage(resultList.getLast());
                currInd = 1;
                if (currInd == resultList.size()) {
                    upBtn.setVisible(false);
                    upBtn.setManaged(false);
                } else {
                    upBtn.setVisible(true);
                    upBtn.setManaged(true);
                }
                if (currInd == 1) {
                    downBtn.setVisible(false);
                    downBtn.setManaged(false);
                } else {
                    downBtn.setVisible(true);
                    downBtn.setManaged(true);
                }
                countResult.setText(currInd + "/" + resultList.size());
                buttonsVB.setVisible(true);
                Platform.runLater(() -> {
                    PauseTransition pauseTransition = new PauseTransition(Duration.millis(150));
                    pauseTransition.setOnFinished(e1 -> {
                        if(messageCellCreator.getControllerCache().get(resultList.getLast()) != null)
                            messageCellCreator.getControllerCache().get(resultList.getLast()).highlightText(searchFromChatTF.getText());
                    });
                    pauseTransition.playFromStart();
                });
            }
        });

        searchContainsStrFromMessage.setOnAction(e -> {
            //Кнопка поиска вхождений подстроки в сообщения
            if (searchFromChatTF.getText() != null && !searchFromChatTF.getText().isEmpty()) {
                currentSearchText = searchFromChatTF.getText().trim();

                resultList = messages.stream().filter(message ->
                        message.getMessageString() != null &&
                                message.getMessageString().toLowerCase().contains(
                                        searchFromChatTF.getText().trim().toLowerCase())).toList();

                if (resultList.isEmpty()) {
                    countResult.setText("Нету результатов");
                    listOfBtn.setDisable(true);
                    buttonsVB.setVisible(false);
                } else {
                    countResult.setText("1/" + resultList.size());
                    buttonsVB.setVisible(true);
                    listOfBtn.setDisable(false);
                    scrollToMessage(resultList.getLast());
                    currInd = 1;
                    if (currInd == resultList.size()) {
                        upBtn.setVisible(false);
                        upBtn.setManaged(false);
                    } else {
                        upBtn.setVisible(true);
                        upBtn.setManaged(true);
                    }
                    if (currInd == 1) {
                        downBtn.setVisible(false);
                        downBtn.setManaged(false);
                    } else {
                        downBtn.setVisible(true);
                        downBtn.setManaged(true);
                    }
                    Platform.runLater(() -> {
                        PauseTransition pauseTransition = new PauseTransition(Duration.millis(100));
                        pauseTransition.setOnFinished(e1 -> {
                            messageCellCreator.getControllerCache().get(resultList.getLast()).highlightText(searchFromChatTF.getText());
                        });
                        pauseTransition.playFromStart();
                    });
                }
            }
        });

        closeSearchModeBtn.setOnAction(e -> {
            currentSearchText = null;
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

    public boolean isSearchModeActive() {
        return searchModeActive;
    }

    public String getCurrentSearchText() {
        return currentSearchText;
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
        }
    }

    private void addContentsForMessage(Message message, String actualTextInTextArea) throws IOException {
        List<MessageContent> contents = new ArrayList<>();
        delFiles.clear();
        if (selectedFile != null && !selectedFile.isEmpty()) {
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
        emojiVB.setMaxWidth(260);
        emojiVB.setAlignment(Pos.TOP_RIGHT);
        emojiSP.setContent(emojiVB);

        Label frequentlyUsed = new Label("Доступные эмодзи");
        frequentlyUsed.getStyleClass().add("emoji-label");
        emojiVB.getChildren().add(frequentlyUsed);

        TilePane pane = new TilePane();
        pane.setStyle("-fx-background-color: #E0F2EF;");
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
                messageTA.positionCaret(messageTA.getText().length());
                messageTA.requestFocus();
            });
            pane.getChildren().add(emojiBtn);
        }

        rightSP.getChildren().add(emojiSP);

        messageHB.heightProperty().addListener((obs, o, n) -> {
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
        // Загружаем сообщение в фоне
        CompletableFuture.supplyAsync(() -> messageService.getRowById(messageID))
                .thenAccept(message -> {
                    Platform.runLater(() -> {
                        if (message == null) {
                            messages.removeIf(m -> m.getMessageId() == messageID);
                            if (!messages.isEmpty())
                                updateChatsViewLastMessage(messages.getLast());
                            else chatsView.refresh();
                        } else {
                            boolean isCurrentChat = selectedChannelUser != null &&
                                    message.getChannelUser().getChannel().equals(selectedChannelUser.getChannel());
                            if (!isCurrentChat) {
                                if (!messages.isEmpty())
                                    updateChatsViewLastMessage(message);
                                else chatsView.refresh();
                                return;
                            }

                            int idx = messages.indexOf(message);
                            if (idx >= 0) {
                                messages.set(idx, message);
                                if (message.getPinMessage()) setPinnedMessagesHBVisible(messages.size());
                            } else {
                                if ((message.getForwardedFrom() == null ||
                                        (message.getForwardedFrom() != null && !message.getChannelUser().getUser().equals(currentUser))) &&
                                        (message.getDeletedByUsers() == null ||
                                                !message.getDeletedByUsers().contains((long) currentUser.getIdUser()))) {
                                    messages.add(message);
                                    messagesLV.scrollTo(message);

                                    if (!message.getChannelUser().getUser().equals(currentUser) &&
                                            !usersInSilentModeService.isUserSetSilentMode(
                                                    message.getChannelUser().getChannel().getChannelID(), currentUser.getIdUser()) &&
                                            (hiddenChannelUserService.isHiddenUserFromChannel(message.getChannelUser().getChannel(), currentUser) == null)) {
                                        Toolkit.getDefaultToolkit().beep();
                                    }
                                }
                            }
                            updateChatsViewLastMessage(message);
                        }
                    });
                })
                .exceptionally(ex -> {
                    ex.printStackTrace();
                    return null;
                });
    }

    private void updateChatsViewLastMessage(Message message) {
        if (message == null) return;
        ChannelUser relatedChat = message.getChannelUser();
        int chatIndex = userChats.indexOf(relatedChat);
        if (chatIndex >= 0) {
            userChats.set(chatIndex, relatedChat);
        } else {
            chatsView.refresh();
        }
    }

    @FXML
    private void createControlsWidow() {
        if (controlsWindowRootVB == null) {
            initControlsWindow();
        }
        if (controlsWindowRootVB.isVisible()) {
            controlsWindowRootVB.setVisible(false);
            controlsWindowRootVB.setManaged(false);
        } else {
            updateControlsWindowState();
            controlsWindowRootVB.setVisible(true);
            controlsWindowRootVB.setManaged(true);
        }
    }

    private void updateEmojiWindowPosition() {
        if (emojiSP == null) return;

        double top = 40;
        double bottom = 0;
        double right = 0;
        double left = 0;

        double messageAnchorHeight = messageHB.getHeight();

        if (pinnedMessagesHB.isVisible()) {
            top += 40;
        }

        if (updateMessageHB.isVisible()) {
            bottom += 40;
        }

        if (fileListView.isVisible())
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

    private void createLeftUserPane() {
        BorderPane leftUserBP = new BorderPane();
        leftUserBP.setMaxWidth(10);
        leftUserBP.getStyleClass().add("left-border-pane");
        leftUserBP.setPadding(new Insets(10, 0, 10, 0));
        StackPane.setAlignment(leftUserBP, Pos.TOP_LEFT);
        leftSP.getChildren().add(leftUserBP);

        VBox topVB = new VBox();
        topVB.setVisible(false);
        topVB.setManaged(false);
        leftUserBP.setTop(topVB);

        CustomButton profileButton = new CustomButton(
                new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/example/speech/image/user.png"))),
                "Профиль");
        profileButton.setPrefHeight(40);
        profileButton.prefWidthProperty().bind(leftUserBP.widthProperty());
        profileButton.setOnAction(e -> {
            if (profileWindow == null)
                profileWindow = new ProfileWindow(this);

            profileWindow.showProfileWidow();
        });
        topVB.getChildren().add(profileButton);

        CustomButton createChannelButton = new CustomButton(
                new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/example/speech/image/create-group.png"))),
                "Создать группу");
        createChannelButton.setPrefHeight(40);
        createChannelButton.prefWidthProperty().bind(leftUserBP.widthProperty());
        createChannelButton.setOnAction(e -> {
            if (createChannelWindow == null)
                createChannelWindow = new CreateChannelWindow(this);

            createChannelWindow.showCreateChannelWidow();
        });
        topVB.getChildren().add(createChannelButton);

        CustomButton logOutButton = new CustomButton(
                new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/example/speech/image/logout.png"))),
                "Выйти");
        leftUserBP.setBottom(logOutButton);
        logOutButton.setPrefHeight(40);
        logOutButton.setVisible(false);
        logOutButton.setManaged(false);
        logOutButton.addStyleText("clear-all-message");
        logOutButton.prefWidthProperty().bind(leftUserBP.widthProperty());
        logOutButton.setOnAction(e -> {
            Thread updateStatusThread = new Thread(() -> {
                currentUser.setStatusUser("не в сети");
                new UserService().update(currentUser);

                List<Channel> channels = channelUserService.getAllChatsByUser(currentUser).stream()
                        .map(cU -> cU.getChannel())
                        .filter(channel -> channel.getChannelType().getChannelTypeId() == 3)
                        .toList();
                for (Channel channel : channels) {
                    ChannelUser channelUser = channelUserService
                            .getInterlocutorUserChannelInChannel(channel.getChannelID(), currentUser.getIdUser());
                    channelUser.setStatusOfTheInterlocutor(currentUser.getStatusUser());
                    channelUserService.update(channelUser);
                }
            });
            updateStatusThread.setDaemon(true);
            updateStatusThread.start();

            CONFIG_MANAGER.setUserEmail("");
            CONFIG_MANAGER.setUserPassword("");
            CONFIG_MANAGER.save();

            FXMLLoader fxmlLoader = new FXMLLoader(EntranceController.class.getResource(
                    "/com/example/speech/shape/EntranceShape.fxml"
            ));
            Parent entranceWindowRoot = null;
            try {
                entranceWindowRoot = fxmlLoader.load();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }

            EntranceController controller = fxmlLoader.getController();
            controller.initializeData(stage);
            stage.getScene().setRoot(entranceWindowRoot);
        });

        leftUserBP.setOnMouseEntered(e -> {
            leftUserBP.setMaxWidth(200);
            topVB.setVisible(true);
            topVB.setManaged(true);
            logOutButton.setVisible(true);
            logOutButton.setManaged(true);
        });

        leftUserBP.setOnMouseExited(e -> {
            topVB.setVisible(false);
            topVB.setManaged(false);
            logOutButton.setVisible(false);
            logOutButton.setManaged(false);
            leftUserBP.setMaxWidth(10);
        });
    }

    public void updateNameAndStatusChannel(ChannelUser channelUser) {
        if (selectedChannelUser != null && selectedChannelUser.equals(channelUser)) {
            channelName.setText(channelUser.getVisibleNameChat());
            if (selectedChannelUser.getChannel().getChannelType().getChannelTypeId() == 3)
                channelStatus.setText(channelUser.getStatusOfTheInterlocutor());
            else
                channelStatus.setText(String.format("Число участников: %d", channelUser.getChannel().getChannelCountUser()));
        }
    }

    private void refreshVisibleMessageCells() {
        if (currentFlow == null) return;
        ObservableList<Message> items = messagesLV.getItems();
        if (items.isEmpty()) return;
        IndexedCell<?> firstCell = currentFlow.getFirstVisibleCell();
        IndexedCell<?> lastCell = currentFlow.getLastVisibleCell();
        if (firstCell == null || lastCell == null) return;
        int first = firstCell.getIndex();
        int last = lastCell.getIndex();
        if (first < 0 || last < 0) return;
        if (first >= items.size()) return;
        try {
            for (int i = first; i <= last && i < items.size(); i++) {
                Message msg = items.get(i);
                TextMessageCellController ctrl = messageCellCreator.getControllerCache(msg);
                if (ctrl != null) {
                    ctrl.updateAvatarVisibility();
                }
            }
        } catch (IndexOutOfBoundsException ignore) {
        }
    }

    private void initControlsWindow() {
        double vBoxWidth = 200;
        controlsWindowRootVB = new VBox();
        controlsWindowRootVB.setMaxWidth(vBoxWidth);
        controlsWindowRootVB.setMaxHeight(Region.USE_PREF_SIZE);
        controlsWindowRootVB.setUserData("controlsWindowRootVB");
        controlsWindowRootVB.getStyleClass().add("working-with-a-message-root-pane");
        StackPane.setAlignment(controlsWindowRootVB, Pos.TOP_RIGHT);
        StackPane.setMargin(controlsWindowRootVB, new Insets(41, 10, 0, 0));
        rightSP.getChildren().add(controlsWindowRootVB);
        controlsWindowRootVB.setVisible(false);
        controlsWindowRootVB.setManaged(false);

        // Поиск
        searchBtn = createCustomButton("imageSearchButton.png", "Поиск");
        searchBtn.setOnAction(e -> actionSearchBtn());
        controlsWindowRootVB.getChildren().add(searchBtn);

        // Ввод
        editBtn = createCustomButton("change.png", "Ввод");
        editBtn.setOnAction(e -> {
            messageTA.requestFocus();
            messageTA.positionCaret(messageTA.getText().length());
        });
        controlsWindowRootVB.getChildren().add(editBtn);

        // Уведомления
        muteBtn = createCustomButton("mute.png", "Отключить звук");
        enableNotificationsBtn = createCustomButton("notification-bell.png", "Включить уведомления");
        muteBtn.setOnAction(e -> {
            UsersInSilentMode newMode = new UsersInSilentMode();
            newMode.setChannelID(selectedChannelUser.getChannel().getChannelID());
            newMode.setUserID(currentUser.getIdUser());
            usersInSilentModeService.save(newMode);
            updateMuteButtonsState();
        });
        enableNotificationsBtn.setOnAction(e -> {
            usersInSilentModeService.deleteUserSetSilentMode(
                    selectedChannelUser.getChannel().getChannelID(),
                    currentUser.getIdUser()
            );
            updateMuteButtonsState();
        });
        controlsWindowRootVB.getChildren().addAll(enableNotificationsBtn, muteBtn);

        // Выбор сообщений
        selectMessagesBtn = createCustomButton("select.png", "Выбрать сообщения");
        selectMessagesBtn.setOnAction(e -> setSelectionModeActive(true));
        controlsWindowRootVB.getChildren().add(selectMessagesBtn);

        // Обои
        selectBackgroundListViewBtn = createCustomButton("background-btn.png", "Выбрать обои");
        setDefaultBackgroundListViewBtn = createCustomButton("delete-backgorund.png", "Обои по умолчанию");
        selectBackgroundListViewBtn.setOnAction(e -> chooseBackgroundImage());
        setDefaultBackgroundListViewBtn.setOnAction(e -> resetBackgroundToDefault());
        controlsWindowRootVB.getChildren().addAll(selectBackgroundListViewBtn, setDefaultBackgroundListViewBtn);

        // Управление пересылкой и очистка чата
        addSharingBtn = createCustomButton("forward.png", "Разрешить пересылку");
        disableSharingBtn = createCustomButton("disable_sharing.png", "Запретить пересылку");
        deleteChatBtn = createCustomButton("delete-red.png", "Отчистить переписку");
        deleteChatBtn.addStyleText("clear-all-message");
        deleteChatBtn.setOnAction(e -> {
            List<Message> allMessage = messageService.getAllMessageInChannel(
                    selectedChannelUser.getChannel().getChannelID()
            );
            if (allMessage != null && !allMessage.isEmpty()) {
                new ConfirmationOfMessageDeletion().initializeShape(this, allMessage);
            }
        });
        addSharingBtn.setOnAction(e -> {
            Channel channel = selectedChannelUser.getChannel();
            channel.setDisable_sharing(false);
            new ChannelService().update(channel);
            updateSharingButtonsState();
        });
        disableSharingBtn.setOnAction(e -> {
            Channel channel = selectedChannelUser.getChannel();
            channel.setDisable_sharing(true);
            new ChannelService().update(channel);
            updateSharingButtonsState();
        });
        controlsWindowRootVB.getChildren().addAll(addSharingBtn, disableSharingBtn, deleteChatBtn);

        // Слушатель закрытия панели при клике вне кнопки
        if (removeControlsWindow == null) {
            removeControlsWindow = e -> {
                Point2D point = createControlsWindowBtn.screenToLocal(e.getScreenX(), e.getScreenY());
                if (point != null && createControlsWindowBtn.contains(point)) return;
                PauseTransition pause = new PauseTransition(Duration.millis(300));
                pause.playFromStart();
                pause.setOnFinished(ev -> {
                    controlsWindowRootVB.setVisible(false);
                    controlsWindowRootVB.setManaged(false);
                });
            };
            messagesSP.addEventFilter(MouseEvent.MOUSE_PRESSED, removeControlsWindow);
        }
    }

    private CustomButton createCustomButton(String imageName, String text) {
        Image image = new Image(Objects.requireNonNull(
                getClass().getResourceAsStream("/com/example/speech/image/" + imageName)
        ));
        CustomButton btn = new CustomButton(image, text);
        btn.setPrefWidth(200);
        btn.setPrefHeight(40);
        return btn;
    }

    private void updateMuteButtonsState() {
        if (selectedChannelUser == null) return;
        boolean isMuted = usersInSilentModeService.isUserSetSilentMode(
                selectedChannelUser.getChannel().getChannelID(),
                currentUser.getIdUser()
        );
        enableNotificationsBtn.setVisible(isMuted);
        enableNotificationsBtn.setManaged(isMuted);
        muteBtn.setVisible(!isMuted);
        muteBtn.setManaged(!isMuted);
    }

    private void updateSharingButtonsState() {
        if (selectedChannelUser == null) return;
        boolean sharingDisabled = selectedChannelUser.getChannel().isDisable_sharing();
        addSharingBtn.setVisible(sharingDisabled);
        addSharingBtn.setManaged(sharingDisabled);
        disableSharingBtn.setVisible(!sharingDisabled);
        disableSharingBtn.setManaged(!sharingDisabled);
    }

    private void updateBackgroundButtonState() {
        if (selectedChannelUser == null) return;
        boolean hasBackground = selectedChannelUser.getBackgroundImageBytes() != null &&
                selectedChannelUser.getBackgroundImageBytes().length > 0;
        setDefaultBackgroundListViewBtn.setVisible(hasBackground);
        setDefaultBackgroundListViewBtn.setManaged(hasBackground);
    }

    private void updateControlsWindowState() {
        if (selectedChannelUser == null) return;
        updateMuteButtonsState();
        updateBackgroundButtonState();

        boolean isDialog = selectedChannelUser.getChannel().getChannelType().getChannelTypeId() == 3;
        boolean isOwner = selectedChannelUser.getChannel().getOwnerUser() != null &&
                selectedChannelUser.getChannel().getOwnerUser().equals(currentUser);
        boolean showChannelControls = isDialog || isOwner;

        addSharingBtn.setVisible(showChannelControls);
        addSharingBtn.setManaged(showChannelControls);
        disableSharingBtn.setVisible(showChannelControls);
        disableSharingBtn.setManaged(showChannelControls);
        deleteChatBtn.setVisible(showChannelControls);
        deleteChatBtn.setManaged(showChannelControls);

        if (showChannelControls) {
            updateSharingButtonsState();
        }
    }

    private void chooseBackgroundImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Выбор изображение для фона");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.gif")
        );
        File chosenFile = fileChooser.showOpenDialog(rootAnchorPane.getScene().getWindow());
        if (chosenFile == null) return;
        try {
            selectedChannelUser.setBackgroundImageBytes(Files.readAllBytes(chosenFile.toPath()));
            channelUserService.update(selectedChannelUser);
            selectedChannelUser.setBackgroundImage(null);
            messagesLV.setBackground(new Background(new BackgroundImage(
                    selectedChannelUser.getBackgroundImage(),
                    BackgroundRepeat.NO_REPEAT,
                    BackgroundRepeat.NO_REPEAT,
                    BackgroundPosition.CENTER,
                    BackgroundSize.DEFAULT
            )));
            updateBackgroundButtonState();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private void resetBackgroundToDefault() {
        messagesLV.setBackground(new Background(new BackgroundImage(
                defaultBackgroundImage,
                BackgroundRepeat.NO_REPEAT,
                BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.CENTER,
                BackgroundSize.DEFAULT
        )));
        selectedChannelUser.setBackgroundImageBytes(null);
        channelUserService.update(selectedChannelUser);
        updateBackgroundButtonState();
    }
}