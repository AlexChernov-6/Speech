package com.example.speech.control;

import com.example.speech.model.User;
import com.example.speech.util.HelpfulStylingClass;
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

public class ProfileWindow extends VBox {
    private final Pane shadowPane;
    private final SpeechBaseController speechBaseController;
    private final StackPane parentStackPane;

    private final User currentUser;

    private HBox bottomHB;

    public ProfileWindow(SpeechBaseController speechBaseController) {
        this.speechBaseController = speechBaseController;
        this.parentStackPane = speechBaseController.getMessagesSP();
        this.currentUser = speechBaseController.getCurrentUser();
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

        TextField emailTF = createTextFields("E-MAIL", currentUser.getEmailUser());
        emailTF.setDisable(true);

        createTextFields("ОТОБРАЖАЕМОЕ ИМЯ", currentUser.getVisibleNameUser());

        createTextFields("ИМЯ ПОЛЬЗОВАТЕЛЯ", currentUser.getNameUser());

        createTextFields("ДАТА РОЖДЕНИЯ", currentUser.getBirthdayUser());

        createBottomHB();

        shadowPane.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> {
            if(!isVisible()) return;

            Point2D point2D = screenToLocal(e.getScreenX(), e.getScreenY());
            if(point2D != null && contains(point2D)) return;
            else if(getOpacity() == 1.0) hideProfileWidow();
        });
        parentStackPane.getChildren().add(this);
    }

    public void showProfileWidow() {
        setManaged(true);
        shadowPane.setVisible(true);
        shadowPane.setManaged(true);
        for(Node node : getChildren()) {
            if(node.getUserData() != null && node.getUserData().equals("bottomHB")) {
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

    public void hideProfileWidow() {
        FadeTransition fadeOut = new FadeTransition(Duration.millis(200), this);
        fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(e -> {
            setManaged(false);
            shadowPane.setVisible(false);
            shadowPane.setManaged(false);
            for(Node node : getChildren()) {
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

        Button logoBtn = new Button();
        logoBtn.getStyleClass().add("button-logo");
        logoBtn.setPrefHeight(200);
        logoBtn.setPrefWidth(300);
        logoBtn.setAlignment(Pos.CENTER);
        logoBtn.setOnAction(e -> {
            if(speechBaseController.getCurrentUser().getPhotoUser() != null && currentUser.getPhotoUser().length > 1)
                ImageUtils.viewingImages(parentStackPane, List.of(currentUser.getPhotoImage()));
        });

        ImageView logoUser = new ImageView(currentUser.getPhotoImage());
        logoUser.setFitWidth(300);
        logoUser.setFitHeight(200);

        logoBtn.setGraphic(logoUser);

        logoSP.getChildren().add(logoBtn);

        Button choseLogoBtn = new Button("Выбрать фото");
        choseLogoBtn.getStyleClass().add("login-button");
        StackPane.setAlignment(choseLogoBtn, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(choseLogoBtn, new Insets(0, 10, 10, 0));
        choseLogoBtn.setVisible(false);
        choseLogoBtn.setManaged(false);
        logoSP.getChildren().add(choseLogoBtn);

        logoSP.setOnMouseEntered(e -> {
            choseLogoBtn.setVisible(true);
            choseLogoBtn.setManaged(true);
        });

        logoSP.setOnMouseExited(e -> {
            choseLogoBtn.setVisible(false);
            choseLogoBtn.setManaged(false);
        });

        getChildren().add(logoSP);
    }

    private TextField createTextFields(String promptText, String content) {
        VBox textFieldVB = new VBox();
        textFieldVB.setAlignment(Pos.TOP_LEFT);
        textFieldVB.setVisible(false);
        textFieldVB.setManaged(false);
        VBox.setMargin(textFieldVB, new Insets(7, 10, 7, 10));

        Label hintLB = new Label(promptText);
        hintLB.getStyleClass().add("other-information");
        textFieldVB.getChildren().add(hintLB);

        TextField textField = new TextField(content);
        textField.setPromptText(promptText);
        textField.getStyleClass().add("text-field");
        HelpfulStylingClass.applyPromptWithTF(textField);
        textFieldVB.getChildren().add(textField);

        textField.textProperty().addListener((ob, o, n) -> {
            if(n != null && !n.equals(o)) {
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
        saveBtn.setMinWidth(130);
        bottomHB.getChildren().add(saveBtn);

        Button resetBtn = new Button("Сбросить");
        resetBtn.getStyleClass().add("login-button");
        resetBtn.setMinWidth(130);
        bottomHB.getChildren().add(resetBtn);


        getChildren().add(bottomHB);
    }
}
