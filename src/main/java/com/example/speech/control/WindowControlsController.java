package com.example.speech.control;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

public class WindowControlsController {

    //Создаём флаг, в котором будем следить за состояние окна isFullScreen или нет
    private boolean isFullScreen = false;

    //Загрузим кнопки работы с окном из fxml файла
    @FXML
    private Button wrapBtn, expandBtn, closeBtn;

    @FXML
    private GridPane headGP;

    //Создаём обработчик событий, в fxml применим к кнопке wrapBtn
    @FXML
    private void onWrapBtn() {
        //Получаем окно, в котором находиться кнопка свернуть окно
        Stage stage = (Stage) wrapBtn.getScene().getWindow();
        //Сворачиваем окно
        stage.setIconified(true);
    }

    @FXML
    private void onExpandBtn() {
        Stage stage = (Stage) expandBtn.getScene().getWindow();
        //Устанавливаем размер окна в FullScreen, если оно не FullScreen и наоборот
        stage.setFullScreen(!stage.isFullScreen());

        //Меняем значение флага в зависимости от состояния окна, он нужен для корректной работы метода updateBackgroundExpandBtn
        isFullScreen = !isFullScreen;
        //Вызываем метод, который меняет изображение на кнопки
        updateBackgroundExpandBtn();
    }

    @FXML
    private void onCloseBtn() {
        Stage stage = (Stage) closeBtn.getScene().getWindow();
        //Закрываем окно
        stage.close();
    }

    @FXML
    private void initialize() {
        //При инициализации укажем какой стиль дать кнопке
        updateBackgroundExpandBtn();
    }

    //Метод для обновления изображения кнопки expandBtn
    private void updateBackgroundExpandBtn() {
        // Удаляем оба класса - стиля
        expandBtn.getStyleClass().removeAll("expand-button-fullscreen", "expand-button-windowed");

        // Добавляем нужный класс
        if (isFullScreen) {
            expandBtn.getStyleClass().add("expand-button-fullscreen");
        } else {
            expandBtn.getStyleClass().add("expand-button-windowed");
        }
    }
}
