package com.example.speech.control;

import com.example.speech.model.User;
import com.example.speech.util.HelpfulClass;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import oshi.SystemInfo;
import oshi.hardware.HardwareAbstractionLayer;

import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Map;

import static com.example.speech.control.EntranceController.CONFIG_MANAGER;
import static com.example.speech.control.EntranceController.HARDWARE_ABSTRACTION_LAYER;
import static com.example.speech.util.HelpfulInitializationClass.setValuesComboBox;
import static com.example.speech.util.HelpfulStylingClass.setRedEndChar;
import static com.example.speech.util.HelpfulStylingClass.setupFullScreenListener;
import static com.example.speech.util.HelpfulValidationClass.updateStyleValidation;
import static com.example.speech.util.NavigateListener.setEnterPressed;

//Класс будет реализовывать интерфейс Window и переопределять метод setStage
//Потому что нам важно получить окно, в котором будем работать
public class RegistrationController {

    private Stage stage;

    @FXML
    private AnchorPane rootAnchorPane;

    @FXML
    private Label mailLb, visibleNameLb, userNameLb, passwordLb, birthdayLb;

    @FXML
    private TextField mailTF, visibleNameTF, userNameTF;

    @FXML
    private PasswordField passwordF;

    @FXML
    private ComboBox<Integer> dayBirthdayCB, yearBirthdayCB;

    @FXML
    private ComboBox<String> monthBirthdayCB;

    @FXML
    private HBox birthdayHBox;

    @FXML
    private Button registrationBtn;

    private String startValueEmail, startValueVisibleName, startValueUserName, startValuePassword, startValueBirthday;

    public void initializeData(Stage stage) {
        this.stage = stage;
        //Добавляем обработчик состояния окна(fullScreen или нет)
        setupFullScreenListener(stage, rootAnchorPane);
        setRedEndChar(mailLb, userNameLb, passwordLb, birthdayLb);
        setValuesComboBox(dayBirthdayCB, monthBirthdayCB, yearBirthdayCB);
        setEnterPressed(registrationBtn);
        Platform.runLater(() -> mailTF.requestFocus());
    }

    @FXML
    private void onRegistrationBtn() throws IOException {
        if (startValueEmail == null)
            startValueEmail = mailLb.getText();

        if (startValueVisibleName == null)
            startValueVisibleName = visibleNameLb.getText();

        if (startValueUserName == null)
            startValueUserName = userNameLb.getText();

        if (startValuePassword == null)
            startValuePassword = passwordLb.getText();

        if (startValueBirthday == null)
            startValueBirthday = birthdayLb.getText();

        //При нажатии на кнопку проводим валидацию данных в TextField
        updateStyleValidation(Map.of(mailTF, mailLb, visibleNameTF, visibleNameLb,
                userNameTF, userNameLb, passwordF, passwordLb, birthdayHBox, birthdayLb));

        //Если данные заполнены корректно
        if (mailLb.getText().equals(startValueEmail) && visibleNameLb.getText().equals(startValueVisibleName)
                && userNameLb.getText().equals(startValueUserName) && passwordLb.getText().equals(startValuePassword)
                && birthdayLb.getText().equals(startValueBirthday)) {
            User newUser = new User(null, mailTF.getText(), visibleNameTF.getText(), userNameTF.getText(), passwordF.getText(),
                    HelpfulClass.getLocalDate(dayBirthdayCB.getValue(), monthBirthdayCB.getValue(), yearBirthdayCB.getValue()),
                    null, null, passwordF.getText(), HARDWARE_ABSTRACTION_LAYER.getComputerSystem().getHardwareUUID());
            CONFIG_MANAGER.setUserEmail(mailTF.getText());
            CONFIG_MANAGER.setUserPassword(passwordF.getText());
            CONFIG_MANAGER.save();
            new ConfirmationEmailController().showModalConfirmationEmailStage(stage, newUser);
        }
    }

    @FXML
    private void onEntranceBtn() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(EntranceController.class.getResource(
                "/com/example/speech/shape/EntranceShape.fxml"
        ));
        Parent entranceWindowRoot = fxmlLoader.load();

        EntranceController controller = fxmlLoader.getController();
        controller.initializeData(stage);
        //И меняем разметку на разметку EntranceShape.fxml
        stage.getScene().setRoot(entranceWindowRoot);
    }

    @FXML
    private void onTermsOfUse() {
        //Открываем страницу с документацией на тему условия использования
        if (Desktop.isDesktopSupported()) {
            Desktop desktop = Desktop.getDesktop();
            File file = Paths.get("Документы/ПОЛЬЗОВАТЕЛЬСКОЕ СОГЛАШЕНИЕ.docx").toFile();
            if (file.exists()) {
                try {
                    desktop.open(file);
                } catch (IOException ignore) {
                }
            }
        } else
            HelpfulClass.openWebPage("https://metanit.com/java/javafx/3.2.php");
    }

    @FXML
    private void onSecurityPolicy() {
        //Открываем страницу с документацией на тему политика безопасности
        if (Desktop.isDesktopSupported()) {
            Desktop desktop = Desktop.getDesktop();
            File file = Paths.get("Документы/ПОЛИТИКА КОНФИДЕНЦИАЛЬНОСТИ.docx").toFile();
            if (file.exists()) {
                try {
                    desktop.open(file);
                } catch (IOException ignore) {
                }
            }
        } else
            HelpfulClass.openWebPage("https://metanit.com/java/javafx/3.2.php");
    }
}
