package com.example.speech.control;

import com.example.speech.model.Channel;
import com.example.speech.model.ChannelUser;
import com.example.speech.model.User;
import com.example.speech.service.ChannelService;
import com.example.speech.service.ChannelTypeService;
import com.example.speech.service.ChannelUserService;
import com.example.speech.util.HelpfulStylingClass;
import com.example.speech.util.ImageUtils;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.util.Duration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.example.speech.util.HelpfulValidationClass.validateChannelNameShort;

public class ChannelGroupWindow extends StackPane {
    private final Pane shadowPane;
    private final StackPane parentStackPane;

    private final User currentUser;

    private HBox bottomHB, buttonsHB;

    private TextField channelNameTF, channelNameUniqueTF;
    private ImageView logoChannel;

    private final List<Label> hintLabels = new ArrayList<>();

    private final ChannelService channelService = new ChannelService();

    private final ChannelUserService channelUserService = new ChannelUserService();

    private final Channel newChannel = new Channel();

    private boolean isOwner = false;

    private Channel currentChannel;

    private final SpeechBaseController speechBaseController;

    private ListView<User> userListView;
    private ObservableList<User> userObservableList;

    private double width = 450.0;

    public ChannelGroupWindow(SpeechBaseController speechBaseController) {
        this.speechBaseController = speechBaseController;
        this.parentStackPane = speechBaseController.getMessagesSP();
        this.currentUser = speechBaseController.getCurrentUser();

        setOpacity(0.0);
        setManaged(false);
        setMouseTransparent(true);
        setMaxWidth(width);
        setMaxHeight(550);
        getStyleClass().add("profile-vbox");

        shadowPane = new Pane();
        shadowPane.setStyle("-fx-background-color: rgba(0,0,0,0.7);");
        shadowPane.setMouseTransparent(false);
        shadowPane.setVisible(false);
        shadowPane.setManaged(false);
        parentStackPane.getChildren().add(shadowPane);

        createStartVB();

        shadowPane.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> {
            if (!isVisible()) return;

            Point2D point2D = screenToLocal(e.getScreenX(), e.getScreenY());
            if (point2D != null && contains(point2D)) return;
            else if (getOpacity() == 1.0) hideChannelGroupWidow();
        });
        parentStackPane.getChildren().add(this);
    }

    public void showChannelGroupWidow(List<User> userList) {
        userObservableList.setAll(userList);
        currentChannel = speechBaseController.getSelectedChannelUser().getChannel();
        if(currentUser.equals(currentChannel.getOwnerUser()))
            isOwner = true;

        if(isOwner) {
            buttonsHB.setManaged(true);
            buttonsHB.setVisible(true);
        } else {
            buttonsHB.setManaged(false);
            buttonsHB.setVisible(false);
        }
        userListView.setCellFactory(e -> new UserCell(currentChannel.getOwnerUser(), speechBaseController));
        setManaged(true);
        setMouseTransparent(false);
        shadowPane.setVisible(true);
        shadowPane.setManaged(true);
        for (Node node : getChildren()) {
            if (node.getUserData() != null && node.getUserData().equals("bottomHB")) {
                node.setVisible(false);
                node.setManaged(false);
            } else {
                node.setVisible(true);
                node.setManaged(true);
            }
        }
        FadeTransition fadeIn = new FadeTransition(Duration.millis(400), this);
        fadeIn.setToValue(1.0);
        fadeIn.play();
    }

    public void hideChannelGroupWidow() {
        FadeTransition fadeOut = new FadeTransition(Duration.millis(200), this);
        fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(e -> {
            setManaged(false);
            setMouseTransparent(true);
            shadowPane.setVisible(false);
            shadowPane.setManaged(false);
            for (Node node : getChildren()) {
                node.setVisible(false);
                node.setManaged(false);
            }
        });
        fadeOut.play();
    }

    private void createStartVB() {
        VBox startVB = new VBox();
        startVB.setManaged(false);
        startVB.setVisible(false);
        startVB.toFront();
        startVB.setMaxHeight(550);

        createButtonLogo(startVB, false);

        buttonsHB = new HBox(5);
        buttonsHB.setAlignment(Pos.CENTER);
        VBox.setMargin(buttonsHB, new Insets(10, 10, 10, 10));
        startVB.getChildren().add(buttonsHB);

        Button addUserInGroupBtn = new Button("Добавить");
        addUserInGroupBtn.getStyleClass().add("button-chose-logo");
        buttonsHB.getChildren().add(addUserInGroupBtn);

        Button updateGroupBtn = new Button("Изменить");
        updateGroupBtn.getStyleClass().add("button-chose-logo");
        buttonsHB.getChildren().add(updateGroupBtn);

        Button deleteGroupBtn = new Button("Удалить");
        deleteGroupBtn.getStyleClass().add("button-chose-logo");
        buttonsHB.getChildren().add(deleteGroupBtn);

        userListView = new ListView<>();
        VBox.setMargin(userListView, new Insets(10, 10, 10, 10));
        Platform.runLater(() -> HelpfulStylingClass.scrollPaneAnimation(userListView));
        VBox.setVgrow(userListView, Priority.ALWAYS);
        userObservableList = FXCollections.observableArrayList();
        userListView.setItems(userObservableList);
        startVB.getChildren().add(userListView);

        getChildren().add(startVB);
    }

    private void createButtonLogo(Pane parent, boolean isUpdatingMode) {
        StackPane logoSP = new StackPane();
        logoSP.setMaxWidth(width);
        logoSP.setMaxHeight(200);
        logoSP.setStyle("-fx-background-radius: 12 12 0 0;");

        Button logoBtn = new Button();
        logoBtn.getStyleClass().add("button-logo");
        logoBtn.setPrefHeight(200);
        logoBtn.setPrefWidth(width);
        logoBtn.setAlignment(Pos.CENTER);
        logoBtn.setOnAction(e -> {
            if (newChannel.getChannelLogo() != null && newChannel.getChannelLogo().length > 1)
                ImageUtils.viewingImages(parentStackPane, List.of(newChannel.getPhotoImage()));
        });

        logoChannel = new ImageView(newChannel.getPhotoImage());
        logoChannel.setFitWidth(width);
        logoChannel.setFitHeight(200);
        logoChannel.setPreserveRatio(true);
        logoChannel.setSmooth(true);

        logoBtn.setGraphic(logoChannel);

        logoSP.getChildren().add(logoBtn);

        Button choseLogoBtn = new Button("Выбрать фото");
        choseLogoBtn.getStyleClass().add("button-chose-logo");
        StackPane.setAlignment(choseLogoBtn, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(choseLogoBtn, new Insets(0, 10, 10, 0));
        choseLogoBtn.setVisible(false);
        choseLogoBtn.setManaged(false);
        logoSP.getChildren().add(choseLogoBtn);
        choseLogoBtn.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Выбор иконки чата");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("images", "*.png", "*.jpg"));

            File chosenFile = fileChooser.showOpenDialog(choseLogoBtn.getScene().getWindow());

            if (chosenFile == null) return;

            try {
                newChannel.setChannelLogo(Files.readAllBytes(chosenFile.toPath()));
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }

            try {
                logoChannel.setImage(new Image(new FileInputStream(chosenFile)));
            } catch (FileNotFoundException ex) {
                throw new RuntimeException(ex);
            }
            bottomHB.setVisible(true);
            bottomHB.setManaged(true);
        });

        if(isUpdatingMode) {
            logoSP.setOnMouseEntered(e -> {
                choseLogoBtn.setVisible(true);
                choseLogoBtn.setManaged(true);
            });

            logoSP.setOnMouseExited(e -> {
                choseLogoBtn.setVisible(false);
                choseLogoBtn.setManaged(false);
            });
        }

        parent.getChildren().add(logoSP);
    }

    private TextField createTextFields(String promptText) {
        VBox textFieldVB = new VBox();
        textFieldVB.setAlignment(Pos.TOP_LEFT);
        textFieldVB.setVisible(false);
        textFieldVB.setManaged(false);
        VBox.setMargin(textFieldVB, new Insets(7, 10, 7, 10));

        Label hintLB = new Label(promptText);
        hintLabels.add(hintLB);
        hintLB.getStyleClass().add("other-information");
        textFieldVB.getChildren().add(hintLB);
        hintLB.setUserData(true);
        hintLB.textProperty().addListener((ob, oldV, newV) -> {
            if (!promptText.equals(newV)) {
                hintLB.setStyle("-fx-text-fill: rgba(115,0,0);");
                hintLB.setUserData(false);
            } else {
                hintLB.setStyle("");
                hintLB.setUserData(true);
            }
        });

        TextField textField = new TextField();
        textField.setUserData(hintLB);
        textField.setPromptText(promptText);
        textField.getStyleClass().add("text-field");
        HelpfulStylingClass.applyPromptWithTF(textField);
        textFieldVB.getChildren().add(textField);

        textField.textProperty().addListener((ob, o, n) -> {
            if (n != null && !n.equals(o)) {
                bottomHB.setVisible(true);
                bottomHB.setManaged(true);
            }
        });

        getChildren().add(textFieldVB);
        return textField;
    }

    private void createBottomHB() {
        bottomHB = new HBox(10);
        bottomHB.setAlignment(Pos.BOTTOM_CENTER);
        VBox.setVgrow(bottomHB, Priority.ALWAYS);
        bottomHB.setPadding(new Insets(10, 10, 10, 10));
        bottomHB.setVisible(false);
        bottomHB.setManaged(false);
        bottomHB.setUserData("bottomHB");

        Button saveBtn = new Button("Сохранить");
        saveBtn.getStyleClass().add("login-button");
        bottomHB.getChildren().add(saveBtn);
        saveBtn.setOnAction(e -> {
            boolean isValid = true;
            for (Label label : hintLabels) {
                if (!((Boolean) label.getUserData())) {
                    isValid = false;
                    break;
                }
            }

            if (channelService.getChatWithName(newChannel.getChannel_name_unique()) != null) {
                ((Label) channelNameUniqueTF.getUserData()).setText("Группа с таким именем уже существует");
                isValid = false;
            }

            if (isValid) {
                newChannel.setChannelType(new ChannelTypeService().getRowById(2L));
                newChannel.setOwnerUser(currentUser);
                boolean save = channelService.save(newChannel);

                if (save) {
                    ChannelUser newChannelUser = new ChannelUser();
                    newChannelUser.setChannel(newChannel);
                    newChannelUser.setUser(currentUser);
                    channelUserService.save(newChannelUser);
                }

                bottomHB.setVisible(false);
                bottomHB.setManaged(false);

                resetAll();

                hideChannelGroupWidow();
            }
        });

        Button resetBtn = new Button("Сбросить");
        resetBtn.getStyleClass().add("login-button");
        bottomHB.getChildren().add(resetBtn);
        resetBtn.setOnAction(e -> {
            resetAll();
        });

        getChildren().add(bottomHB);
    }

    private void resetAll() {
        newChannel.setChannelName(null);
        newChannel.setChannel_name_unique(null);
        newChannel.setChannelLogo(null);

        logoChannel.setImage(newChannel.getPhotoImage());
        channelNameTF.setText(null);
        channelNameUniqueTF.setText(null);

        bottomHB.setVisible(false);
        bottomHB.setManaged(false);
    }
}
