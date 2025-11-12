package com.example.speech.controls;

import com.example.speech.util.UsefulClass;
import com.example.speech.Window;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Map;

//Класс будет реализовывать интерфейс Window и переопределять метод setStage
//Потому что нам важно получить окно, в котором будем работать
public class RegistrationController implements Window {

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
        UsefulClass.setRedEndChar(mailLb, userNameLb, passwordLb, birthdayLb);
        UsefulClass.setValuesComboBox(dayBirthdayCB, monthBirthdayCB, yearBirthdayCB);
    }

    @Override
    public void setStage(Stage stage) {
        this.stage = stage;
        //Добавляем обработчик состояния окна(fullScreen или нет)
        UsefulClass.setupFullScreenListener(stage, rootAnchorPane);
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

        //Аналогично из метода start
        Parent speechBaseRoot = UsefulClass.loadFXML(
                stage, "/com/example/speech/shapes/SpeechBaseShape.fxml", SpeechBaseController.class);

        //При нажатии на кнопку проводим валидацию данных в TextField
        UsefulClass.updateStyleValidation(Map.of(mailTF, mailLb, visibleNameTF, visibleNameLb,
                userNameTF, userNameLb, passwordF, passwordLb, birthdayHBox, birthdayLb));

        //Валидация даты рождения

        //Если данные заполнены корректно
        if(mailLb.getText().equals(startValueEmail) && visibleNameLb.getText().equals(startValueVisibleName)
                && userNameLb.getText().equals(startValueUserName) && passwordLb.getText().equals(startValuePassword))
            //Меняем разметку окна авторизации на разметку основного окна
            stage.getScene().setRoot(speechBaseRoot);
    }

    @FXML
    private void onEntranceBtn() throws IOException {
        //Подгружаем разметку окна входа
        Parent entranceWindowRoot = UsefulClass.loadFXML(
                stage, "/com/example/speech/shapes/EntranceShape.fxml", EntranceController.class);
        //И меняем разметку на разметку EntranceShape.fxml
        stage.getScene().setRoot(entranceWindowRoot);
    }

    @FXML
    private void onTermsOfUse() {
        //Открываем страницу с документацией на тему условия использования
        UsefulClass.openWebPage("https://metanit.com/java/javafx/3.2.php");
    }

    @FXML
    private void onSecurityPolicy() {
        //Открываем страницу с документацией на тему политика безопасности
        UsefulClass.openWebPage("https://metanit.com/java/javafx/3.2.php");
    }
}
