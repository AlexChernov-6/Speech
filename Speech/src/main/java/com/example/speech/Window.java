package com.example.speech;

import javafx.stage.Stage;

//Создадим интерфейс, который будет реализовываться в классах, где нужно получить окно при инициализации
public interface Window {
    void setStage(Stage stage);
}
