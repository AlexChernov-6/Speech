package com.example.speech;

import javafx.fxml.FXML;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class RegistrationController implements Window{
    private Stage stage;
    @FXML
    private AnchorPane rootAnchorPane;
    //Метод initialize гарантированно вызывается после инициализации всех полей FXML, поэтому в нём можно работать
    //С различными полями разметки
    @FXML
    public void initialize() {

    }

    @Override
    public void setStage(Stage stage) {
        this.stage = stage;
        //Добавляем обработчик состояния окна(fullScreen или нет)
        UsefulClass.setupFullScreenListener(stage, rootAnchorPane);
    }
}
