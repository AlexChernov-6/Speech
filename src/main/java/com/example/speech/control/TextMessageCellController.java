package com.example.speech.control;

import com.example.speech.model.Message;
import com.example.speech.model.User;
import com.example.speech.service.UserService;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.text.TextFlow;
import javafx.util.Duration;

import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.example.speech.control.WorkingWithAMessageListController.replyI;


public class TextMessageCellController {
    @FXML
    public ImageView userPhotoIV;
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
    private RowConstraints replyRow;
    @FXML
    private Button replyMessageBtn;
    @FXML
    private RowConstraints forwardRow;
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

    private SpeechBaseController speechBaseController;
    private Message message;

    private static final Image shipped = new Image(Objects.requireNonNull
            (TextMessageCellController.class.getResourceAsStream("/com/example/speech/image/check.png")));
    private static final Image readIt = new Image(Objects.requireNonNull
            (TextMessageCellController.class.getResourceAsStream("/com/example/speech/image/double_check.png")));
    private static final Image loading = new Image(Objects.requireNonNull
            (TextMessageCellController.class.getResourceAsStream("/com/example/speech/image/preview.gif")));

    private String contentGPStyle = "";

    public GridPane initializeMessage(SpeechBaseController speechBaseController, Message message, boolean drawUserPhoto) {
        this.speechBaseController = speechBaseController;
        this.message = message;
        rootMessageAP.widthProperty().addListener((ob, oldV, newV) -> {
            double newWidth = newV.doubleValue() - 150;
            if (newWidth >= 200) {
                contentGP.setMaxWidth(newWidth);
                messageLabel.setMaxWidth(newWidth - 20);
            }
        });
        String messageContent = new String(message.getMessageContent(), StandardCharsets.UTF_8);
        userPhotoIV.setImage(message.getChannelUser().getUser().getPhotoImage());
        messageLabel.setText(messageContent);
        timeLabel.setText(message.getMessageDatetime().toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm")));
        if (message.getMessageStatus() != null && message.getMessageStatus().equals("отправлено"))
            statusIV.setImage(shipped);
        else if (message.getMessageStatus() != null && message.getMessageStatus().equals("прочитано"))
            statusIV.setImage(readIt);
        else
            statusIV.setImage(loading);

        if (Objects.equals(message.getChannelUser().getUser().getIdUser(), speechBaseController.getCurrentUser()
                .getIdUser()))
            contentGP.getStyleClass().add("message-text-grid-pane-my");
        else {
            contentGP.getStyleClass().add("message-text-grid-pane-other");
            timeStatusHB.getChildren().remove(statusIV);
            HBox.setMargin(timeLabel, new Insets(0, 0, 0, 0));
        }

        if (!drawUserPhoto) {
            userPhotoIV.setVisible(false);
            contentGP.setStyle("-fx-background-radius: 15px 15px 15px 15px; -fx-border-radius: 15px 15px 15px 15px;");
        } else
            contentGP.setStyle("");

        setMouseListener();

        if (message.isModifiedMessage()) {
            columnInfo.setPrefWidth(100);
            columnInfo.setMinWidth(100);
            changeStatusLB.setVisible(true);
            changeStatusLB.setManaged(true);
        } else {
            columnInfo.setPrefWidth(60);
            columnInfo.setMinWidth(60);
        }

        if (message.getMessageIdReplyTo() != null) {
            Message replyMessage = speechBaseController.getMessageService().getRowById(message.getMessageIdReplyTo());
            replyRow.setPrefHeight(50);
            replyMessageBtn.setPrefHeight(45);
            replyMessageBtn.setVisible(true);
            replyMessageBtn.setManaged(true);

            String userName = "";
            String messageContentReply = "";
            if (replyMessage != null) {
                userName = replyMessage.getChannelUser().getUser().getNameUser();
                messageContentReply = new String(replyMessage.getMessageContent(), StandardCharsets.UTF_8);
            }

            String displayUserName = userName.length() > 20 ? userName.substring(0, 20) + "..." : userName;
            String displayMessage = messageContentReply.length() > 50 ? messageContentReply.substring(0, 50) + "..." : messageContentReply;

            Label nameLabel = new Label(displayUserName);
            nameLabel.getStyleClass().add("reply-user-name");

            Label messageLabel = new Label("Удаленное сообщение");
            if (displayMessage.isEmpty()) {
                messageLabel.getStyleClass().add("reply-message-text");
                messageLabel.setStyle("-fx-font-style: italic;");
            } else {
                messageLabel.setText(displayMessage);
                messageLabel.getStyleClass().add("reply-message-text");
            }


            VBox textContainer = new VBox(nameLabel, messageLabel);
            textContainer.setSpacing(2);
            textContainer.setAlignment(Pos.TOP_LEFT);
            textContainer.setMaxWidth(250);
            textContainer.setMaxHeight(40);

            replyMessageBtn.setGraphic(textContainer);
            replyMessageBtn.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);

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
            forwardRow.setPrefHeight(25);
            forwardHB.setVisible(true);
            forwardHB.setManaged(true);
            userLogo.setImage(user.getPhotoImage());
            userInfoBtn.setText(user.getNameUser());
        }

        contentGPStyle = contentGP.getStyle();

        setSelected(speechBaseController.getSelectedMessages().contains(message));

        return contentGP;
    }

    public void setMaxWidthGP(double v) {
        contentGP.setMaxWidth(v - 50);
    }

    public void setMouseListener() {
        /*rootMessageAP.setOnMousePressed(e -> {
            if(!speechBaseController.isDragSelecting() && speechBaseController.isSelectionModeActive()
                    && e.getButton() == MouseButton.PRIMARY) {
                speechBaseController.toggleMessageSelection(message);
            }
        });*/

        contentGP.setOnMouseClicked(event -> {
            if (!speechBaseController.isSelectionModeActive() ||
                    event.getButton() == MouseButton.SECONDARY) {
                if (event.getButton() == MouseButton.SECONDARY) {
                    WorkingWithAMessageListController controller = new WorkingWithAMessageListController();
                    controller.initializeShape(event.getSceneX(), event.getSceneY(), speechBaseController, message);
                    speechBaseController.getMessagesSP().getChildren().add(controller);
                    event.consume();
                }

                if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                    speechBaseController.getHintIV().setImage(replyI);
                    speechBaseController.getHintLB().setText("В ответ " + message.getChannelUser().getUser().getNameUser());
                    speechBaseController.setContextPopUpBar(SpeechBaseController.ContextPopUpBar.REPLY_MESSAGE);
                    speechBaseController.getContentUpdateMessageLB().setText(new String(message.getMessageContent(), StandardCharsets.UTF_8));
                    speechBaseController.getUpdateMessageHB().setVisible(true);
                    speechBaseController.getUpdateMessageHB().setManaged(true);
                    speechBaseController.setMessageIdReplyTo(message.getMessageId());
                    speechBaseController.getMessagesSP().getChildren().remove(this);
                    Platform.runLater(() -> {
                        speechBaseController.getMessageTA().requestFocus();
                    });
                }
            }
        });
    }


    public void highlightMessageTemporarily() {
        Pane highlightPane = new Pane();
        highlightPane.setStyle("-fx-background-color: rgba(100, 149, 237, 0.3)");
        highlightPane.setOpacity(0.0);
        highlightMessageTemporarilySP.getChildren().add(highlightPane);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(1000), highlightPane);
        fadeIn.setToValue(1.0);
        fadeIn.setOnFinished(e1 -> {
            PauseTransition pauseTransition = new PauseTransition(Duration.millis(500));
            pauseTransition.setOnFinished(e2 -> {
                FadeTransition fadeOut = new FadeTransition(Duration.millis(2500), highlightPane);
                fadeOut.setToValue(0.0);
                fadeOut.setOnFinished(e3 -> {
                    highlightMessageTemporarilySP.getChildren().remove(highlightPane);
                });
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

    public void setSelected(boolean selected) {
        selectIV.setVisible(selected);
        if (selected)
            contentGP.setStyle("-fx-background-color: rgba(0,0,255,0.6);");
        else
            contentGP.setStyle(contentGPStyle);
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
}
