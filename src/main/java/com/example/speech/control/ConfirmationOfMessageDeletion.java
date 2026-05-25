package com.example.speech.control;

import com.example.speech.model.Channel;
import com.example.speech.model.Message;
import com.example.speech.service.ChannelService;
import com.example.speech.service.ChannelTypeService;
import com.example.speech.service.MessageService;
import com.example.speech.util.HibernateSessionFactory;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.control.Label;
import javafx.scene.text.TextFlow;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;
import java.util.Set;

public class ConfirmationOfMessageDeletion extends Pane {
    private static final MessageService MESSAGE_SERVICE = new MessageService();

    public void initializeShape(String nameOfTheInterlocutor, SpeechBaseController speechBaseController
            , List<Message> messages) {
        this.setStyle("-fx-background-color: rgba(0, 0, 0, 0.6);");

        double vBoxWidth = 350;
        double vBoxHeight = 120;

        VBox rootVB = new VBox(15);
        rootVB.setPrefWidth(vBoxWidth);
        rootVB.setPrefHeight(vBoxHeight);
        rootVB.setAlignment(Pos.CENTER_LEFT);
        rootVB.getStyleClass().add("working-with-a-message-root-pane");
        rootVB.setLayoutX(speechBaseController.getMessagesSP().getScene().getWindow().getWidth() / 2 - vBoxWidth / 2);
        rootVB.setLayoutY(speechBaseController.getMessagesSP().getScene().getWindow().getHeight() / 2 - vBoxHeight / 2);
        rootVB.setPadding(new Insets(15));

        Label questionLabel = new Label("Удалить это сообщение?");
        questionLabel.setStyle("-fx-font-size: 14px");

        if(messages.size() == 1)
            questionLabel.setText("Удалить это сообщение?");
        else
            questionLabel.setText("Удалить выбранные сообщения?");

        HBox centralHB = new HBox(10);

        CheckBox checkBox = new CheckBox();
        checkBox.getStyleClass().add("check-box");

        TextFlow informationTextFlow = new TextFlow();
        Label startLabel = new Label("Также удалить для «");
        Label centralLabel = new Label(nameOfTheInterlocutor);
        centralLabel.setStyle("-fx-font-weight: bold;");
        Label endLabel = new Label("»");
        informationTextFlow.getChildren().addAll(startLabel, centralLabel, endLabel);
        informationTextFlow.setStyle("-fx-font-size: 14px");

        centralHB.getChildren().addAll(checkBox, informationTextFlow);

        HBox bottomHB = new HBox(10);
        bottomHB.setAlignment(Pos.CENTER_RIGHT);

        Button cancellationButton = new Button();
        cancellationButton.setText("Отмена");
        cancellationButton.setOnAction(e -> {
            speechBaseController.getMessagesSP().getChildren().remove(this);
        });
        cancellationButton.getStyleClass().add("login-button");

        Button deleteButton = new Button();
        deleteButton.setText("Удалить");
        deleteButton.setOnAction(e -> {
            for (Message message : messages) {
                if (checkBox.isSelected())
                    MESSAGE_SERVICE.delete(message);
                else {
                    message.getDeletedByUsers().add(Long.valueOf(speechBaseController.getCurrentUser().getIdUser()));
                    MESSAGE_SERVICE.update(message);
                }
            }
            speechBaseController.getMessagesSP().getChildren().remove(this);
            if(speechBaseController.flag)
                speechBaseController.setPinnedMessagesHBVisible(speechBaseController.firstVisible);
            else
                speechBaseController.updatePinnedMessagesList();
            Platform.runLater(() -> {
                if (speechBaseController.isSelectionModeActive())
                    speechBaseController.setSelectionModeActive(false);

                speechBaseController.getMessages().removeAll(messages);
            });
        });
        deleteButton.getStyleClass().add("login-button");
        bottomHB.getChildren().addAll(cancellationButton, deleteButton);

        rootVB.getChildren().addAll(questionLabel, centralHB, bottomHB);
        this.getChildren().add(rootVB);

        this.setOnMousePressed(event -> {
            if(rootVB.getBoundsInParent().contains(event.getX(), event.getY()))
                event.consume();
            else
                speechBaseController.getMessagesSP().getChildren().remove(this);
        });

        speechBaseController.getMessagesSP().getChildren().add(this);
    }

    public void initializeShape(SpeechBaseController speechBaseController) {
        StackPane stackPane = speechBaseController.getMessagesSP();
        this.setStyle("-fx-background-color: rgba(0, 0, 0, 0.6);");

        double vBoxWidth = 350;
        double vBoxHeight = 120;

        VBox rootVB = new VBox(15);
        rootVB.setPrefWidth(vBoxWidth);
        rootVB.setPrefHeight(vBoxHeight);
        rootVB.setAlignment(Pos.CENTER_LEFT);
        rootVB.getStyleClass().add("working-with-a-message-root-pane");
        rootVB.setLayoutX(stackPane.getScene().getWindow().getWidth() / 2 - vBoxWidth / 2);
        rootVB.setLayoutY(stackPane.getScene().getWindow().getHeight() / 2 - vBoxHeight / 2);
        rootVB.setPadding(new Insets(15));

        Label questionLabel = new Label("Открепить все сообщения?");
        questionLabel.setStyle("-fx-font-size: 14px");
        questionLabel.setPrefHeight(120);
        questionLabel.setAlignment(Pos.CENTER_LEFT);

        HBox bottomHB = new HBox(10);
        bottomHB.setAlignment(Pos.CENTER_RIGHT);

        Button cancellationButton = new Button();
        cancellationButton.setText("Отмена");
        cancellationButton.setOnAction(e -> {
            stackPane.getChildren().remove(this);
        });
        cancellationButton.getStyleClass().add("login-button");

        Button unpinnedButton = new Button();
        unpinnedButton.setText("Открепить");
        unpinnedButton.setOnAction(e -> {
            MESSAGE_SERVICE.unpinAllMessageInChannel(speechBaseController.getSelectedChannelUser().getChannel()
                    .getChannelID());
            speechBaseController.getMessages().stream()
                    .filter(Message::getPinMessage)
                    .forEach(msg -> msg.setPinMessage(false));
            speechBaseController.hideTheListOfPinnedMessages();
            stackPane.getChildren().remove(this);
        });
        unpinnedButton.getStyleClass().add("login-button");
        bottomHB.getChildren().addAll(cancellationButton, unpinnedButton);

        rootVB.getChildren().addAll(questionLabel, bottomHB);
        this.getChildren().add(rootVB);

        this.setOnMousePressed(event -> {
            if(rootVB.getBoundsInParent().contains(event.getX(), event.getY()))
                event.consume();
            else
                stackPane.getChildren().remove(this);
        });

        stackPane.getChildren().add(this);
    }

    public void initializeShape(SpeechBaseController speechBaseController, List<Message> messages) {
        this.setStyle("-fx-background-color: rgba(0, 0, 0, 0.6);");

        double vBoxWidth = 350;
        double vBoxHeight = 200;

        VBox rootVB = new VBox(15);
        rootVB.setPrefWidth(vBoxWidth);
        rootVB.setPrefHeight(vBoxHeight);
        rootVB.setAlignment(Pos.CENTER_LEFT);
        rootVB.getStyleClass().add("working-with-a-message-root-pane");
        rootVB.setLayoutX(speechBaseController.getMessagesSP().getScene().getWindow().getWidth() / 2 - vBoxWidth / 2);
        rootVB.setLayoutY(speechBaseController.getMessagesSP().getScene().getWindow().getHeight() / 2 - vBoxHeight / 2);
        rootVB.setPadding(new Insets(15));

        Label questionLabel = new Label("Вы уверены, что хотите удалить все сообщения?\nДанное действие невозможно будет отменить.");
        questionLabel.setStyle("-fx-font-size: 14px");
        questionLabel.setWrapText(true);

        HBox centralHB = new HBox(10);

        CheckBox checkBox = new CheckBox();
        checkBox.getStyleClass().add("check-box");

        HBox bottomHB = new HBox(10);
        bottomHB.setAlignment(Pos.CENTER_RIGHT);

        Button cancellationButton = new Button();
        cancellationButton.setText("Отмена");
        cancellationButton.setOnAction(e -> {
            speechBaseController.getMessagesSP().getChildren().remove(this);
        });
        cancellationButton.getStyleClass().add("login-button");

        Button deleteButton = new Button();
        deleteButton.setText("Удалить");
        deleteButton.setOnAction(e -> {
            Platform.runLater(() -> {
                speechBaseController.getMessages().clear();
            });
            Thread deleteThread = new Thread(() -> {
                new MessageService().deleteAllMessage(messages);
            });
            deleteThread.setDaemon(true);
            deleteThread.start();
            speechBaseController.getMessagesSP().getChildren().remove(this);
        });
        deleteButton.getStyleClass().add("login-button");
        bottomHB.getChildren().addAll(cancellationButton, deleteButton);

        rootVB.getChildren().addAll(questionLabel, centralHB, bottomHB);
        this.getChildren().add(rootVB);

        this.setOnMousePressed(event -> {
            if(rootVB.getBoundsInParent().contains(event.getX(), event.getY()))
                event.consume();
            else
                speechBaseController.getMessagesSP().getChildren().remove(this);
        });

        speechBaseController.getMessagesSP().getChildren().add(this);
    }

    public void initializeShapeDeletionGroup(SpeechBaseController speechBaseController, Channel group, ChannelGroupWindow channelGroupWindow) {
        this.setStyle("-fx-background-color: rgba(0, 0, 0, 0.6);");

        double vBoxWidth = 350;
        double vBoxHeight = 200;

        VBox rootVB = new VBox(15);
        rootVB.setPrefWidth(vBoxWidth);
        rootVB.setPrefHeight(vBoxHeight);
        rootVB.setAlignment(Pos.CENTER_LEFT);
        rootVB.getStyleClass().add("working-with-a-message-root-pane");
        rootVB.setLayoutX(speechBaseController.getMessagesSP().getScene().getWindow().getWidth() / 2 - vBoxWidth / 2);
        rootVB.setLayoutY(speechBaseController.getMessagesSP().getScene().getWindow().getHeight() / 2 - vBoxHeight / 2);
        rootVB.setPadding(new Insets(15));

        Label questionLabel = new Label("Вы уверены, что хотите удалить группу?\nДанное действие невозможно будет отменить.");
        questionLabel.setStyle("-fx-font-size: 14px");
        questionLabel.setWrapText(true);

        HBox centralHB = new HBox(10);

        CheckBox checkBox = new CheckBox();
        checkBox.getStyleClass().add("check-box");

        HBox bottomHB = new HBox(10);
        bottomHB.setAlignment(Pos.CENTER_RIGHT);

        Button cancellationButton = new Button();
        cancellationButton.setText("Отмена");
        cancellationButton.setOnAction(e -> {
            speechBaseController.getMessagesSP().getChildren().remove(this);
        });
        cancellationButton.getStyleClass().add("login-button");

        Button deleteButton = new Button();
        deleteButton.setText("Удалить");
        deleteButton.setOnAction(e -> {
            speechBaseController.getMessagesSP().getChildren().remove(this);
            channelGroupWindow.hideChannelGroupWidow();

            Thread deleteThread = new Thread(() -> {
                int channelId = group.getChannelID();
                // внутри deleteThread
                try (Session entityManager = HibernateSessionFactory.getSessionFactory().openSession()) {
                    Transaction transaction = entityManager.beginTransaction();

                    List<Long> channelUserIds = entityManager.createQuery(
                                    "SELECT cu.channelUserId FROM ChannelUser cu WHERE cu.channel.channelID = :channelId", Long.class)
                            .setParameter("channelId", channelId)
                            .getResultList();

                    if (!channelUserIds.isEmpty()) {
                        entityManager.createQuery(
                                        "DELETE FROM MessageContent mc WHERE mc.message.messageId IN " +
                                                "(SELECT m.messageId FROM Message m WHERE m.channelUser.channelUserId IN :userIds)")
                                .setParameter("userIds", channelUserIds)
                                .executeUpdate();

                        entityManager.createQuery(
                                        "DELETE FROM Message m WHERE m.channelUser.channelUserId IN :userIds")
                                .setParameter("userIds", channelUserIds)
                                .executeUpdate();

                        entityManager.createQuery(
                                        "DELETE FROM ChannelUser cu WHERE cu.channel.channelID = :channelId")
                                .setParameter("channelId", channelId)
                                .executeUpdate();
                    }

                    entityManager.createQuery(
                                    "DELETE FROM UsersInSilentMode usm WHERE usm.channelID = :channelId")
                            .setParameter("channelId", channelId)
                            .executeUpdate();

                    // Удаляем сам канал
                    Channel managedChannel = entityManager.find(Channel.class, channelId);
                    if (managedChannel != null) {
                        entityManager.remove(managedChannel);
                    }

                    transaction.commit();
                }
            });
            deleteThread.setDaemon(true);
            deleteThread.start();
        });
        deleteButton.getStyleClass().add("login-button");
        bottomHB.getChildren().addAll(cancellationButton, deleteButton);

        rootVB.getChildren().addAll(questionLabel, centralHB, bottomHB);
        this.getChildren().add(rootVB);

        this.setOnMousePressed(event -> {
            if(rootVB.getBoundsInParent().contains(event.getX(), event.getY()))
                event.consume();
            else
                speechBaseController.getMessagesSP().getChildren().remove(this);
        });

        speechBaseController.getMessagesSP().getChildren().add(this);
    }
}
