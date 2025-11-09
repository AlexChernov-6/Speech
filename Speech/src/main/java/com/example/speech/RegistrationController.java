package com.example.speech;

import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.IOException;

//Класс будет реализовывать интерфейс Window и переопределять метод setStage
//Потому что нам важно получить окно, в котором будем работать
public class RegistrationController implements Window {

    private Stage stage;

    @FXML
    private AnchorPane rootAnchorPane;

    @FXML
    private Label mailLb, visibleNameLb, userNameLb, passwordLb, birthdayLb;

    @FXML
    private TextField mailTF, visibleNameTF, userNameTF, passwordF;

    @FXML
    private Button getMailingList, continueBtn, сonfirmationConditionsBtn, authorizationBtn;

    @FXML
    private ComboBox<Integer> dayBirthdayCB, yearBirthdayCB;

    @FXML
    private ComboBox<String> monthBirthdayCB;

    @FXML
    private HBox birthdayHBox;

    //Метод initialize гарантированно вызывается после инициализации всех полей FXML, поэтому в нём можно работать
    //С различными полями разметки
    @FXML
    public void initialize() {
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
    public void onAuthorizationBtn() throws IOException {
        Parent authorisationRoot = UsefulClass.loadFXML(
                stage, "EntranceShape.fxml", EntranceController.class);
        stage.getScene().setRoot(authorisationRoot);
    }


}
