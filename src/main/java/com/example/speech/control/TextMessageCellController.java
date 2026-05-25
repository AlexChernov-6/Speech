package com.example.speech.control;

import com.example.speech.model.ChannelUser;
import com.example.speech.model.Message;
import com.example.speech.model.MessageContent;
import com.example.speech.model.User;
import com.example.speech.service.ChannelUserService;
import com.example.speech.service.MessageContentService;
import com.example.speech.service.UserService;
import com.example.speech.util.FileUtils;
import com.example.speech.util.ImageUtils;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.text.TextFlow;
import javafx.util.Duration;

import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.example.speech.control.WorkingWithAMessageListController.replyI;
import static com.example.speech.util.ImageUtils.setCircularImage;


public class TextMessageCellController {
    @FXML
    public ImageView userPhotoIV;
    @FXML
    private VBox contentVB;
    @FXML
    private StackPane highlightMessageTemporarilySP;
    @FXML
    private Label messageLabel, timeLabel;
    @FXML
    private TextFlow contentTextFlow;
    @FXML
    private GridPane contentGP;
    @FXML
    public ImageView statusIV;
    @FXML
    private HBox timeStatusHB;
    @FXML
    private AnchorPane rootMessageAP;
    @FXML
    private Label changeStatusLB;
    @FXML
    private ColumnConstraints columnInfo;
    @FXML
    private Button replyMessageBtn;
    @FXML
    private HBox forwardHB;
    @FXML
    private ImageView userLogo;
    @FXML
    private Button userInfoBtn;
    @FXML
    private StackPane selectSP;
    @FXML
    private ImageView selectIV;
    @FXML
    private HBox invitationHB;
    @FXML
    private Button invitationBtn;

    private SpeechBaseController speechBaseController;
    private Message message;

    private Pane highlightPane;

    private static final Image shipped = new Image(Objects.requireNonNull
            (TextMessageCellController.class.getResourceAsStream("/com/example/speech/image/check.png")));
    private static final Image readIt = new Image(Objects.requireNonNull
            (TextMessageCellController.class.getResourceAsStream("/com/example/speech/image/double_check.png")));
    private static final Image loading = new Image(Objects.requireNonNull
            (TextMessageCellController.class.getResourceAsStream("/com/example/speech/image/preview.gif")));

    private final List<Image> imagesFromMessage = new ArrayList<>();

    private final ChannelUserService channelUserService = new ChannelUserService();

    public GridPane initializeMessage(SpeechBaseController speechBaseController, Message message, boolean drawUserPhoto) {
        this.speechBaseController = speechBaseController;
        this.message = message;
        rootMessageAP.widthProperty().addListener((ob, oldV, newV) -> {
            double newWidth = newV.doubleValue() - 150;
            if (newWidth >= 200) {
                contentVB.setMaxWidth(newWidth);
                messageLabel.setMaxWidth(newWidth - 20);
            }
        });
        if (message.getMessageString() != null && !message.getMessageString().isEmpty())
            messageLabel.setText(message.getMessageString());
        else messageLabel.setManaged(false);

        if (speechBaseController.getCurrentSearchText() != null &&
                message.getMessageString() != null &&
                message.getMessageString().toLowerCase().contains(speechBaseController.getCurrentSearchText().toLowerCase())) {
            highlightText(speechBaseController.getCurrentSearchText());
        }

        timeLabel.setText(message.getMessageDatetime().toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm")));
        if (message.getMessageStatus() != null && message.getMessageStatus().equals("отправлено"))
            statusIV.setImage(shipped);
        else if (message.getMessageStatus() != null && message.getMessageStatus().equals("прочитано"))
            statusIV.setImage(readIt);
        else
            statusIV.setImage(loading);

        if (Objects.equals(message.getChannelUser().getUser().getIdUser(), speechBaseController.getCurrentUser()
                .getIdUser()))
            contentVB.getStyleClass().add("message-text-grid-pane-my");
        else {
            contentVB.getStyleClass().add("message-text-grid-pane-other");
            timeStatusHB.getChildren().remove(statusIV);
            HBox.setMargin(timeLabel, new Insets(0, 0, 0, 0));
        }

        timeStatusHB.minWidthProperty().bind(contentGP.widthProperty());

        if (!drawUserPhoto) {
            userPhotoIV.setVisible(false);
            contentVB.setStyle("-fx-background-radius: 15px 15px 15px 15px; -fx-border-radius: 15px 15px 15px 15px;");
        } else {
            setCircularImage(userPhotoIV, message.getChannelUser().getUser().getPhotoImage(), 45);
            userPhotoIV.setVisible(true);
            contentVB.setStyle("");
        }

        setMouseListener();

        if (message.isModifiedMessage()) {
            columnInfo.setPrefWidth(120);
            columnInfo.setMinWidth(120);
            changeStatusLB.setVisible(true);
            changeStatusLB.setManaged(true);
        } else {
            columnInfo.setPrefWidth(60);
            columnInfo.setMinWidth(60);
        }

        if (message.getMessageIdReplyTo() != null) {
            Message replyMessage = speechBaseController.getMessageService().getRowById(message.getMessageIdReplyTo());
            replyMessageBtn.setVisible(true);
            replyMessageBtn.setManaged(true);

            String userName = "";
            String messageContentReply = "";
            if (replyMessage != null) {
                userName = replyMessage.getChannelUser().getUser().getNameUser();
                if (replyMessage.getMessageString() != null && !replyMessage.getMessageString().isEmpty())
                    messageContentReply = replyMessage.getMessageString();
                else {
                    int countContents = replyMessage.getMessageContent().size();
                    if (countContents == 1)
                        messageContentReply = String.format("%d вложение", countContents);
                    else if (countContents >= 2 && countContents <= 4)
                        messageContentReply = String.format("%d вложения", countContents);
                    else
                        messageContentReply = String.format("%d вложений", countContents);
                }
            }

            String displayUserName = userName.length() > 20 ? userName.substring(0, 20) + "..." : userName;
            String displayMessage = messageContentReply.length() > 50 ? messageContentReply.substring(0, 50) + "..." : messageContentReply;

            Label nameLabel = new Label(displayUserName);
            nameLabel.getStyleClass().add("reply-user-name");

            Label messageLabel = new Label("Удаленное сообщение");
            messageLabel.getStyleClass().add("reply-message-text");
            if (displayMessage.isEmpty())
                messageLabel.setStyle("-fx-font-style: italic;");
            else
                messageLabel.setText(displayMessage);


            VBox textContainer = new VBox(messageLabel);
            textContainer.setSpacing(2);
            textContainer.setAlignment(Pos.CENTER_LEFT);
            textContainer.setMaxWidth(250);
            if (nameLabel.getText() == null || nameLabel.getText().isEmpty())
                textContainer.setMinHeight(25);
            else
                textContainer.getChildren().addFirst(nameLabel);

            replyMessageBtn.prefHeightProperty().bind(textContainer.heightProperty().add(5));
            replyMessageBtn.setGraphic(textContainer);

            replyMessageBtn.setOnAction(e -> {
                Platform.runLater(() -> {
                    if (replyMessage != null) {
                        speechBaseController.getMessagesLV().scrollTo(replyMessage);
                        PauseTransition pause = new PauseTransition(Duration.millis(100));
                        pause.setOnFinished(event -> {
                            speechBaseController.getMessageCellCreator()
                                    .getControllerCache(replyMessage)
                                    .highlightMessageTemporarily();
                        });
                        pause.play();
                    }
                });
            });
        }

        if (message.getForwardedFrom() != null) {
            UserService userService = new UserService();
            User user = userService.getRowById(message.getForwardedFrom());
            forwardHB.setVisible(true);
            forwardHB.setManaged(true);
            setCircularImage(userLogo, user.getPhotoImage(), 15);
            userInfoBtn.setText(user.getNameUser());
            userInfoBtn.setOnAction(e -> {
                if(speechBaseController.getCurrentUser().equals(user)) {
                    if(speechBaseController.getProfileWindow() != null)
                        speechBaseController.getProfileWindow().showProfileWidow();
                    else {
                        speechBaseController.setProfileWindow(new ProfileWindow(speechBaseController));
                        speechBaseController.getProfileWindow().showProfileWidow();
                    }
                } else {
                    if(speechBaseController.getOtherProfileWindow() != null)
                        speechBaseController.getOtherProfileWindow().showOtherProfileWindow(user);
                    else {
                        speechBaseController.setOtherProfileWindow(new OtherProfileWindow(speechBaseController));
                        speechBaseController.getOtherProfileWindow().showOtherProfileWindow(user);
                    }
                }
            });
            //Добавить обработку нажатия на имя переславшего
        }

        if(message.getChannelInvitations() != null) {
            invitationHB.setVisible(true);
            invitationHB.setManaged(true);
            this.messageLabel.setText("Присоединяйся к " + message.getChannelInvitations().getChannel_name_unique() + "!\n" +
                    "С нами жизнь будет интересней)");
            invitationBtn.setText("Вступить в " + message.getChannelInvitations().getChannel_name_unique());
            invitationBtn.setOnAction(e -> {
                ChannelUser channelUser = channelUserService
                        .getChannelUserByUserIdAndChannelId(speechBaseController.getCurrentUser().getIdUser()
                                , message.getChannelInvitations().getChannelID());

                if(channelUser == null) {
                    channelUser = new ChannelUser();
                    channelUser.setUser(speechBaseController.getCurrentUser());
                    channelUser.setChannel(message.getChannelInvitations());
                    channelUser.setVisibleNameChat(message.getChannelInvitations().getChannelName());
                    channelUser.setVisibleLogoChat(message.getChannelInvitations().getChannelLogo());
                    channelUserService.save(channelUser);
                }

                ChannelUser finalChannelUser = channelUser;
                Platform.runLater(() -> {
                    speechBaseController.chatsView.getSelectionModel().select(finalChannelUser);
                });
            });
        }

        boolean isSelected = speechBaseController.isMessageSelected(message);
        setSelected(isSelected);

        return contentGP;
    }

    public void setMaxWidthGP(double v) {
        contentVB.setMaxWidth(v - 50);
    }

    public void setMouseListener() {
        highlightMessageTemporarilySP.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.SECONDARY) {
                WorkingWithAMessageListController controller = new WorkingWithAMessageListController();
                controller.initializeShape(event.getSceneX(), event.getSceneY(), speechBaseController, message);
                speechBaseController.getMessagesSP().getChildren().add(controller);
                event.consume();
            } else if (event.getButton() == MouseButton.PRIMARY) {
                speechBaseController.handleMessageClick(message, event);
            }
        });
    }


    public void highlightMessageTemporarily() {
        if (highlightPane == null) {
            highlightPane = new Pane();
            highlightPane.setStyle("-fx-background-color: rgba(100, 149, 237, 0.3)");
            highlightPane.setOpacity(0.0);
            highlightPane.setMouseTransparent(true);
            highlightMessageTemporarilySP.getChildren().add(highlightPane);
        }

        FadeTransition fadeIn = new FadeTransition(Duration.millis(1000), highlightPane);
        fadeIn.setToValue(1.0);
        fadeIn.setOnFinished(e1 -> {
            PauseTransition pauseTransition = new PauseTransition(Duration.millis(500));
            pauseTransition.setOnFinished(e2 -> {
                FadeTransition fadeOut = new FadeTransition(Duration.millis(2500), highlightPane);
                fadeOut.setToValue(0.0);
                pauseTransition.stop();
                fadeOut.playFromStart();
            });
            fadeIn.stop();
            pauseTransition.playFromStart();
        });
        fadeIn.play();
    }

    public void setSelectionModeActive(boolean active) {
        selectSP.setVisible(active);
        if (!active) {
            selectIV.setVisible(false);
        }
    }

    public boolean isHit(double screenX, double screenY) {
        if (rootMessageAP == null) return false;
        Point2D local = rootMessageAP.screenToLocal(screenX, screenY);
        return local != null && rootMessageAP.contains(local);
    }

    public AnchorPane getRootMessageAP() {
        return rootMessageAP;
    }

    public void highlightText(String searchText) {
        // Очищаем предыдущие выделения
        contentTextFlow.getChildren().clear();

        String fullText = messageLabel.getText();
        if (searchText == null || searchText.isEmpty()) {
            contentTextFlow.getChildren().add(createOrdinaryLabel(fullText));
            return;
        }

        // Важно: (?iu) – CASE_INSENSITIVE + UNICODE_CASE для русских букв
        Pattern pattern = Pattern.compile("(?iu)" + Pattern.quote(searchText));
        Matcher matcher = pattern.matcher(fullText);

        int lastEnd = 0;
        boolean found = false;

        while (matcher.find()) {
            found = true;
            int start = matcher.start();
            int end = matcher.end();

            // Обычный текст до совпадения
            if (start > lastEnd) {
                contentTextFlow.getChildren().add(
                        createOrdinaryLabel(fullText.substring(lastEnd, start))
                );
            }

            // Выделенный фрагмент (с оригинальным регистром из текста)
            String highlightedPart = fullText.substring(start, end);
            contentTextFlow.getChildren().add(createHighlightLabel(highlightedPart));

            lastEnd = end;
        }

        // Остаток после последнего совпадения
        if (lastEnd < fullText.length()) {
            contentTextFlow.getChildren().add(
                    createOrdinaryLabel(fullText.substring(lastEnd))
            );
        }

        // Если ничего не найдено – весь текст обычным стилем
        if (!found) {
            contentTextFlow.getChildren().add(createOrdinaryLabel(fullText));
        }
    }

    private Label createOrdinaryLabel(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-font-size: 14px; -fx-padding: 0;");
        label.setWrapText(true);
        return label;
    }

    private Label createHighlightLabel(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-font-size: 14px; -fx-padding: 0; " +
                "-fx-background-color: rgba(225, 255, 0, 0.2); " +
                "-fx-background-radius: 3;");
        label.setWrapText(true);
        return label;
    }

    public void addFile(MessageContent mC) {
        // Контейнер для строки файла
        HBox fileHB = new HBox();
        fileHB.setAlignment(Pos.CENTER_LEFT);
        fileHB.setPickOnBounds(true);
        contentVB.getChildren().add(2, fileHB);

        fileHB.setOnMouseEntered(e -> {
            fileHB.setStyle("-fx-background-color: rgba(150, 150, 170, 0.3); -fx-background-radius: 10;");
        });
        fileHB.setOnMouseExited(e -> {
            fileHB.setStyle("");
        });

        // Левая часть: иконка + прогресс
        StackPane stackPane = new StackPane();
        stackPane.setMaxWidth(45);
        stackPane.setMaxHeight(45);
        fileHB.getChildren().add(stackPane);
        stackPane.setMouseTransparent(true);

        // Иконка файла по умолчанию
        ImageView fileImage = new ImageView(
                new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/example/speech/image/doc.png"))));
        fileImage.setFitHeight(40);
        fileImage.setFitWidth(40);
        StackPane.setMargin(fileImage, new Insets(0, 0, 0, 5));
        stackPane.getChildren().add(fileImage);
        fileImage.setMouseTransparent(true);

        // Расширение файла для отображения на иконке
        String fileName = mC.getMessageContentFileName();
        String fileType;
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex > 0 && dotIndex < fileName.length() - 1) {
            fileType = fileName.substring(dotIndex + 1).toLowerCase();
        } else {
            fileType = "";
        }

        Label fileNameLB = new Label(fileType);
        fileNameLB.setMaxWidth(40);
        fileNameLB.setPrefWidth(40);
        fileNameLB.setStyle("-fx-text-fill: white; -fx-font-size: 14; -fx-font-weight: bold;");
        fileNameLB.setAlignment(Pos.CENTER);
        fileNameLB.setMouseTransparent(true);
        StackPane.setAlignment(fileNameLB, Pos.TOP_CENTER);
        StackPane.setMargin(fileNameLB, new Insets(20, 0, 0, 0));
        stackPane.getChildren().add(fileNameLB);

        // Индикатор загрузки
        ProgressIndicator progressIndicator = new ProgressIndicator(0);
        StackPane.setAlignment(progressIndicator, Pos.CENTER);
        progressIndicator.setPrefWidth(40);
        progressIndicator.setPrefHeight(40);
        stackPane.getChildren().add(progressIndicator);
        progressIndicator.setVisible(true);
        progressIndicator.setMouseTransparent(true);

        // Правая часть: имя файла и статус
        VBox rightVB = new VBox(3);
        rightVB.setAlignment(Pos.TOP_LEFT);
        rightVB.setPadding(new Insets(10, 10, 10, 20));
        fileHB.getChildren().add(rightVB);
        rightVB.setMouseTransparent(true);

        Label nameLabel = new Label(fileName);
        nameLabel.setStyle("-fx-font-size: 14px;");
        rightVB.getChildren().add(nameLabel);
        nameLabel.setMouseTransparent(true);

        Label statusLabel = new Label();
        rightVB.getChildren().add(statusLabel);
        statusLabel.setMouseTransparent(true);

        // Проверяем, существует ли файл локально
        Path localFile = FileUtils.DEFAULT_STORAGE_DIR.resolve(fileName);
        if (Files.exists(localFile)) {
            // Файл уже на диске – не обращаемся к БД
            progressIndicator.setVisible(false);
            statusLabel.setText("файл на устройстве");

            // Для картинок сразу показываем превью
            if (fileType.equals("png") || fileType.equals("jpg") || fileType.equals("gif")) {
                fileNameLB.setVisible(false);
                try {
                    Image image = new Image(new FileInputStream(localFile.toFile()));
                    imagesFromMessage.add(image);
                    fileImage.setImage(image);
                } catch (FileNotFoundException e) {
                    fileNameLB.setVisible(true);
                    fileNameLB.setText(fileType);
                }
            }

            fileHB.setOnMousePressed(e -> {
                if (e.getButton() == MouseButton.PRIMARY) {
                    String fileType2;
                    File file = localFile.toFile();
                    int dotIndex2 = file.getName().lastIndexOf('.');
                    if (dotIndex2 > 0 && dotIndex2 < file.getName().length() - 1) {
                        fileType2 = file.getName().substring(dotIndex2 + 1).toLowerCase();
                    } else {
                        fileType2 = "";
                    }

                    if (fileType2.equals("png") || fileType2.equals("jpg") || fileType2.equals("gif")) {
                        ImageUtils.viewingImages(speechBaseController.getMessagesSP(), imagesFromMessage);
                    } else {
                        if (Desktop.isDesktopSupported()) {
                            Desktop desktop = Desktop.getDesktop();
                            if (file.exists()) {
                                try {
                                    desktop.open(file);
                                } catch (IOException e1) {

                                }
                            }
                        }
                    }
                    e.consume();
                }
            });
        } else {
            // Файла нет – запускаем асинхронную загрузку из БД
            MessageContentService contentService = new MessageContentService();
            FileUtils.SaveResult result = FileUtils.saveToDefaultDirAsync(
                    fileName,
                    () -> contentService.getContentBytes(mC.getMessageContentId()) // ленивая загрузка байтов
            );

            progressIndicator.progressProperty().bind(result.progressProperty());

            // Отслеживаем прогресс
            result.progressProperty().addListener((obs, oldV, newV) -> {
                if (newV.doubleValue() >= 1.0) {
                    // Завершено
                    Platform.runLater(() -> {
                        progressIndicator.setVisible(false);
                        statusLabel.setText("загрузка завершена");
                        // Показываем превью, если картинка
                        if (fileType.equals("png") || fileType.equals("jpg") || fileType.equals("gif")) {
                            fileNameLB.setVisible(false);
                            try {
                                Image image = new Image(new FileInputStream(localFile.toFile()));
                                imagesFromMessage.add(image);
                                fileImage.setImage(image);
                            } catch (FileNotFoundException e) {
                                fileNameLB.setVisible(true);
                                fileNameLB.setText(fileType);
                                System.err.println(e.getMessage());
                            }
                        }

                        fileHB.setOnMousePressed(e -> {
                            if (e.getButton() == MouseButton.PRIMARY) {
                                String fileType2;
                                File file = result.getResultNow().toFile();
                                int dotIndex2 = file.getName().lastIndexOf('.');
                                if (dotIndex2 > 0 && dotIndex2 < file.getName().length() - 1) {
                                    fileType2 = file.getName().substring(dotIndex2 + 1).toLowerCase();
                                } else {
                                    fileType2 = "";
                                }

                                if (fileType2.equals("png") || fileType2.equals("jpg") || fileType2.equals("gif")) {
                                    ImageUtils.viewingImages(speechBaseController.getMessagesSP(), imagesFromMessage);
                                } else {
                                    if (Desktop.isDesktopSupported()) {
                                        Desktop desktop = Desktop.getDesktop();
                                        if (file.exists()) {
                                            try {
                                                desktop.open(file);
                                            } catch (IOException e1) {

                                            }
                                        }
                                    }
                                }
                                e.consume();
                            }
                        });
                    });
                } else {
                    // Обновляем процент
                    Platform.runLater(() ->
                            statusLabel.setText(String.format("Загрузка: %.0f%%", newV.doubleValue() * 100))
                    );
                }
            });
            // Запускаем запись (start уже вызывается в saveToDefaultDirAsync)
        }
    }

    public void setSelected(boolean selected) {
        selectIV.setVisible(selected);
        highlightMessageTemporarilySP.setStyle(
                selected ? "-fx-background-color: rgba(100, 149, 237, 0.3); -fx-background-radius: 12;" : "");
    }
}
