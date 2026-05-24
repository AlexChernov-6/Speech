package com.example.speech.control;

import com.example.speech.model.User;
import com.example.speech.util.HelpfulStylingClass;
import com.example.speech.util.ImageConverter;
import com.example.speech.util.ImageUtils;
import javafx.animation.FadeTransition;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.util.Duration;

import java.util.List;

public class OtherProfileWindow extends VBox {
    private final Pane shadowPane;
    private final StackPane parentStackPane;

    private User lastViewingUser;

    private ImageView logoChannel;

    private final TextField emailUserTF, visibleNameTF, nameTF, birthdayTF, statusTF;

    private Button logoBtn;

    public OtherProfileWindow(SpeechBaseController speechBaseController) {
        this.parentStackPane = speechBaseController.getMessagesSP();

        setOpacity(0.0);
        setManaged(false);
        setMaxWidth(300);
        setMaxHeight(500);
        getStyleClass().add("profile-vbox");

        shadowPane = new Pane();
        shadowPane.setStyle("-fx-background-color: rgba(0,0,0,0.7);");
        shadowPane.setMouseTransparent(false);
        shadowPane.setVisible(false);
        shadowPane.setManaged(false);
        parentStackPane.getChildren().add(shadowPane);

        createButtonLogo();

        emailUserTF = createTextFields("E-MAIL");
        visibleNameTF = createTextFields("Отображаемое имя");
        nameTF = createTextFields("Имя пользователя");
        birthdayTF = createTextFields("Дата рождения");
        statusTF = createTextFields("Статус пользователя");

        shadowPane.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> {
            if (!isVisible()) return;

            Point2D point2D = screenToLocal(e.getScreenX(), e.getScreenY());
            if (point2D != null && contains(point2D)) return;
            else if (getOpacity() == 1.0) hideOtherProfileWindow();
        });
        parentStackPane.getChildren().add(this);
    }

    public void showOtherProfileWindow(User user) {
        if(lastViewingUser == null) {
            lastViewingUser = user;
            updateState(user);
        }

        if(!lastViewingUser.equals(user)) {
            lastViewingUser = user;
            updateState(user);
        }
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

    public void hideOtherProfileWindow() {
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

    private void createButtonLogo() {
        StackPane logoSP = new StackPane();
        logoSP.setMaxWidth(300);
        logoSP.setMaxHeight(200);
        logoSP.setManaged(false);
        logoSP.setVisible(false);
        logoSP.setStyle("-fx-background-radius: 12 12 0 0;");

        logoBtn = new Button();
        logoBtn.getStyleClass().add("button-logo");
        logoBtn.setPrefHeight(200);
        logoBtn.setPrefWidth(300);
        logoBtn.setDisable(true);
        logoBtn.setAlignment(Pos.CENTER);
        logoBtn.setOnAction(e -> {
            ImageUtils.viewingImages(parentStackPane, List.of(logoChannel.getImage()));
        });

        logoChannel = new ImageView();
        logoChannel.setFitWidth(300);
        logoChannel.setFitHeight(200);

        logoBtn.setGraphic(logoChannel);

        logoSP.getChildren().add(logoBtn);

        getChildren().add(logoSP);
    }

    private TextField createTextFields(String promptText) {
        VBox textFieldVB = new VBox();
        textFieldVB.setAlignment(Pos.TOP_LEFT);
        textFieldVB.setVisible(false);
        textFieldVB.setManaged(false);
        VBox.setMargin(textFieldVB, new Insets(7, 10, 7, 10));

        Label hintLB = new Label(promptText);
        hintLB.getStyleClass().add("other-information");
        textFieldVB.getChildren().add(hintLB);

        TextField textField = new TextField();
        textField.setDisable(true);
        textField.setUserData(hintLB);
        textField.setPromptText(promptText);
        textField.getStyleClass().add("text-field");
        HelpfulStylingClass.applyPromptWithTF(textField);
        textFieldVB.getChildren().add(textField);

        getChildren().add(textFieldVB);
        return textField;
    }

    private void updateState(User user) {
        logoChannel.setImage(user.getPhotoImage());
        if(user.getPhotoUser() != null && user.getPhotoUser().length > 1)
            logoBtn.setDisable(false);
        emailUserTF.setText(user.getEmailUser());
        visibleNameTF.setText(user.getVisibleNameUser());
        nameTF.setText(user.getNameUser());
        birthdayTF.setText(user.getBirthdayUser());
        statusTF.setText(user.getStatusUser());
    }
}
