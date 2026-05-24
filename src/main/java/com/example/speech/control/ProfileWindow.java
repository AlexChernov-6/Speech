package com.example.speech.control;

import com.example.speech.model.User;
import com.example.speech.service.UserService;
import com.example.speech.util.HelpfulStylingClass;
import com.example.speech.util.ImageUtils;
import com.example.speech.util.InputControlMaskFormatter;
import javafx.animation.FadeTransition;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
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
import java.util.Arrays;
import java.util.List;

import static com.example.speech.util.HelpfulValidationClass.validateUserNameShort;
import static com.example.speech.util.InputControlMaskFormatter.MASK_DATE;

public class ProfileWindow extends VBox {
    private final Pane shadowPane;
    private final SpeechBaseController speechBaseController;
    private final StackPane parentStackPane;

    private final User currentUser;

    private HBox bottomHB;

    private TextField visibleNameTF, userNameTF, birthdayTF;
    private ImageView logoUser;

    private final List<Label> hintLabels = new ArrayList<>();

    private byte[] newUserLogo;
    private String newVisibleName;
    private String newUserName;
    private String newUserBirthday;

    private final UserService userService = new UserService();

    private double width = 450.0;

    public ProfileWindow(SpeechBaseController speechBaseController) {
        this.speechBaseController = speechBaseController;
        this.parentStackPane = speechBaseController.getMessagesSP();
        this.currentUser = speechBaseController.getCurrentUser();
        setOpacity(0.0);
        setManaged(false);
        setMouseTransparent(true);
        setMaxWidth(width);
        setMaxHeight(500);
        getStyleClass().add("profile-vbox");

        shadowPane = new Pane();
        shadowPane.setStyle("-fx-background-color: rgba(0,0,0,0.7);");
        shadowPane.setMouseTransparent(false);
        shadowPane.setVisible(false);
        shadowPane.setManaged(false);
        parentStackPane.getChildren().add(shadowPane);

        newUserLogo = currentUser.getPhotoUser();
        newVisibleName = currentUser.getVisibleNameUser();
        newUserName = currentUser.getNameUser();
        newUserBirthday = currentUser.getBirthdayUser();

        createButtonLogo();

        TextField emailTF = createTextFields("E-MAIL", currentUser.getEmailUser());
        emailTF.setDisable(true);

        visibleNameTF = createTextFields("ОТОБРАЖАЕМОЕ ИМЯ", currentUser.getVisibleNameUser());
        visibleNameTF.textProperty().addListener((ob, oldV, newV) -> {
            if(newV.isEmpty() || newV.equals("ОТОБРАЖАЕМОЕ ИМЯ")) {
                if(visibleNameTF.getUserData() != null && visibleNameTF.getUserData() instanceof Label)
                    ((Label) visibleNameTF.getUserData()).setText("Поле не может быть пустым");
            } else {
                if(visibleNameTF.getUserData() != null && visibleNameTF.getUserData() instanceof Label)
                    ((Label) visibleNameTF.getUserData()).setText("ОТОБРАЖАЕМОЕ ИМЯ");
                newVisibleName = newV;
            }
        });

        userNameTF = createTextFields("ИМЯ ПОЛЬЗОВАТЕЛЯ", currentUser.getNameUser());
        userNameTF.textProperty().addListener((ob, oldV, newV) -> {
            if(newV.equals("ИМЯ ПОЛЬЗОВАТЕЛЯ")) {
                if(userNameTF.getUserData() != null && userNameTF.getUserData() instanceof Label)
                    ((Label) userNameTF.getUserData()).setText("Поле не может быть пустым");
            } else {
                if (userNameTF.getUserData() != null && userNameTF.getUserData() instanceof Label) {
                    String valid = validateUserNameShort(newV);
                    if (valid == null) {
                        ((Label) userNameTF.getUserData()).setText("ИМЯ ПОЛЬЗОВАТЕЛЯ");
                        newUserName = newV;
                    } else ((Label) userNameTF.getUserData()).setText(valid);
                }
            }
        });

        birthdayTF = createTextFields("ДАТА РОЖДЕНИЯ", currentUser.getBirthdayUser());
        InputControlMaskFormatter maskBirthday = new InputControlMaskFormatter();
        maskBirthday.apply(birthdayTF, InputControlMaskFormatter.MaskContext.DATE_MASK);
        birthdayTF.setOnMouseEntered(e -> {
            birthdayTF.setPromptText("__.__.____");
        });
        birthdayTF.setOnMouseExited(e -> {
            birthdayTF.setPromptText("ДАТА РОЖДЕНИЯ");
        });
        birthdayTF.textProperty().addListener((ob, oldV, newV) -> {
            newUserBirthday = newV;
            if(newV.equals("ДАТА РОЖДЕНИЯ") || newV.equals(MASK_DATE)) {
                if(birthdayTF.getUserData() != null && birthdayTF.getUserData() instanceof Label)
                    ((Label) birthdayTF.getUserData()).setText("Поле не может быть пустым");
            }
        });

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
        setMouseTransparent(false);
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
            setMouseTransparent(true);
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
        logoSP.setMaxWidth(width);
        logoSP.setMaxHeight(200);
        logoSP.setManaged(false);
        logoSP.setVisible(false);
        logoSP.setStyle("-fx-background-radius: 12 12 0 0;");

        Button logoBtn = new Button();
        logoBtn.getStyleClass().add("button-logo");
        logoBtn.setPrefHeight(200);
        logoBtn.setPrefWidth(width);
        logoBtn.setAlignment(Pos.CENTER);
        logoBtn.setOnAction(e -> {
            if(speechBaseController.getCurrentUser().getPhotoUser() != null && currentUser.getPhotoUser().length > 1)
                ImageUtils.viewingImages(parentStackPane, List.of(currentUser.getPhotoImage()));
        });

        logoUser = new ImageView(currentUser.getPhotoImage());
        logoUser.setFitWidth(width);
        logoUser.setFitHeight(200);
        logoUser.setPreserveRatio(true);
        logoUser.setSmooth(true);

        logoBtn.setGraphic(logoUser);

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
            fileChooser.setTitle("Выбор иконки пользователя");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("images", "*.png", "*.jpg"));

            File chosenFile = fileChooser.showOpenDialog(choseLogoBtn.getScene().getWindow());

            if(chosenFile == null) return;

            try {
                newUserLogo = Files.readAllBytes(chosenFile.toPath());
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }

            try {
                logoUser.setImage(new Image(new FileInputStream(chosenFile)));
            } catch (FileNotFoundException ex) {
                throw new RuntimeException(ex);
            }
            bottomHB.setVisible(true);
            bottomHB.setManaged(true);
        });

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
        hintLabels.add(hintLB);
        hintLB.getStyleClass().add("other-information");
        textFieldVB.getChildren().add(hintLB);
        hintLB.setUserData(true);
        hintLB.textProperty().addListener((ob, oldV, newV) -> {
            if(!promptText.equals(newV)) {
                hintLB.setStyle("-fx-text-fill: rgba(115,0,0);");
                hintLB.setUserData(false);
            }
            else {
                hintLB.setStyle("");
                hintLB.setUserData(true);
            }
        });

        TextField textField = new TextField(content);
        textField.setUserData(hintLB);
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
        bottomHB.getChildren().add(saveBtn);
        saveBtn.setOnAction(e -> {
            boolean isValid = true;
            for (Label label : hintLabels) {
                if(!((Boolean) label.getUserData())) {
                    isValid = false;
                    break;
                }
            }

            if(!currentUser.getNameUser().equals(newUserName) && userService.getUserByUserName(newUserName) != null) {
                ((Label) userNameTF.getUserData()).setText("Пользователь с таким именем уже существует");
                isValid = false;
            }

            if(isValid) {
                boolean isNewData = false;
                if(!Arrays.equals(currentUser.getPhotoUser(), newUserLogo)) {
                    isNewData = true;
                    currentUser.setPhotoUser(newUserLogo);
                }
                if(!currentUser.getVisibleNameUser().equals(newVisibleName)) {
                    isNewData = true;
                    currentUser.setVisibleNameUser(newVisibleName);
                }
                if(!currentUser.getNameUser().equals(newUserName)) {
                    isNewData = true;
                    currentUser.setNameUser(newUserName);
                }
                if(!currentUser.getBirthdayUser().equals(newUserBirthday)) {
                    isNewData = true;
                    currentUser.setBirthdayUser(newUserBirthday);
                }

                if(isNewData)
                    userService.update(currentUser);
                bottomHB.setVisible(false);
                bottomHB.setManaged(false);
            }
        });

        Button resetBtn = new Button("Сбросить");
        resetBtn.getStyleClass().add("login-button");
        bottomHB.getChildren().add(resetBtn);
        resetBtn.setOnAction(e -> {
            newUserLogo = currentUser.getPhotoUser();
            newVisibleName = currentUser.getVisibleNameUser();
            newUserName = currentUser.getNameUser();
            newUserBirthday = currentUser.getBirthdayUser();

            logoUser.setImage(currentUser.getPhotoImage());
            visibleNameTF.setText(currentUser.getVisibleNameUser());
            userNameTF.setText(currentUser.getNameUser());
            birthdayTF.setText(currentUser.getBirthdayUser());
            ((Label) birthdayTF.getUserData()).setText("ДАТА РОЖДЕНИЯ");

            bottomHB.setVisible(false);
            bottomHB.setManaged(false);
        });

        getChildren().add(bottomHB);
    }
}
