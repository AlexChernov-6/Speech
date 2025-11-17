package com.example.speech.control;

import com.example.speech.util.HelpfulClass;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Map;

import static com.example.speech.util.HelpfulInitializationClass.setValuesComboBox;
import static com.example.speech.util.HelpfulStylingClass.setRedEndChar;
import static com.example.speech.util.HelpfulStylingClass.setupFullScreenListener;
import static com.example.speech.util.HelpfulValidationClass.updateStyleValidation;

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

    private String startValueEmail, startValueVisibleName, startValueUserName, startValuePassword;

    //Метод initialize гарантированно вызывается после инициализации всех полей FXML, поэтому в нём можно работать
    //С различными полями разметки
    @FXML
    private void initialize() {
        //После инициализации контроллеров меняем стили, символ * делаем красной, и заполняем ComboBox значениями
        setRedEndChar(mailLb, userNameLb, passwordLb, birthdayLb);
        setValuesComboBox(dayBirthdayCB, monthBirthdayCB, yearBirthdayCB);
    }

    public void setStage(Stage stage) {
        this.stage = stage;
        //Добавляем обработчик состояния окна(fullScreen или нет)
        setupFullScreenListener(stage, rootAnchorPane);
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

        FXMLLoader fxmlLoader = new FXMLLoader(EntranceController.class.getResource(
                "/com/example/speech/shape/SpeechBaseShape.fxml"
        ));
        Parent speechBaseRoot = fxmlLoader.load();

        SpeechBaseController controller = fxmlLoader.getController();
        controller.setStage(stage);

        //При нажатии на кнопку проводим валидацию данных в TextField
        updateStyleValidation(Map.of(mailTF, mailLb, visibleNameTF, visibleNameLb,
                userNameTF, userNameLb, passwordF, passwordLb, birthdayHBox, birthdayLb));

        //Валидация даты рождения

        //Если данные заполнены корректно
        if(mailLb.getText().equals(startValueEmail) && visibleNameLb.getText().equals(startValueVisibleName)
                && userNameLb.getText().equals(startValueUserName) && passwordLb.getText().equals(startValuePassword)) {
            /*User newUser = new User(mailTF.getText(), visibleNameTF.getText(), userNameTF.getText(), passwordF.getText(),
                    UsefulClass.getLocalDate(dayBirthdayCB.getValue(), monthBirthdayCB.getValue(), yearBirthdayCB.getValue()));
            UserService.registerUser(newUser);*/
            //Меняем разметку окна авторизации на разметку основного окна
            new ConfirmationEmailController().showModalStage(stage, mailTF.getText());
            //stage.getScene().setRoot(speechBaseRoot);
        }
    }

    @FXML
    private void onEntranceBtn() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(EntranceController.class.getResource(
                "/com/example/speech/shape/EntranceShape.fxml"
        ));
        Parent entranceWindowRoot = fxmlLoader.load();

        EntranceController controller = fxmlLoader.getController();
        controller.setStage(stage);
        //И меняем разметку на разметку EntranceShape.fxml
        stage.getScene().setRoot(entranceWindowRoot);
    }

    @FXML
    private void onTermsOfUse() {
        //Открываем страницу с документацией на тему условия использования
        HelpfulClass.openWebPage("https://metanit.com/java/javafx/3.2.php");
    }

    @FXML
    private void onSecurityPolicy() {
        //Открываем страницу с документацией на тему политика безопасности
        HelpfulClass.openWebPage("https://metanit.com/java/javafx/3.2.php");
    }
}
