package com.example.speech;

import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import java.io.IOException;
import java.util.Map;

public class EntranceController extends Application implements Window{
    //Корневой контейнер самого высокого уровня, то что пользователь видит как окно приложения(окно ОС)
    private Stage stage;

    //Аннотация, указывающая что объекты ниже будут искать в fxml файлe и свяжутся с этими переменными
    @FXML
    private TextField mailTF;

    @FXML
    private PasswordField passwordF;

    @FXML
    private AnchorPane rootAnchorPane;

    @FXML
    private Label mailLb, passwordLb;

    //Переопределим метод из интерфейса
    @Override
    public void setStage(Stage stage) {
        this.stage = stage;
        //Добавляем обработчик состояния окна(fullScreen или нет)
        UsefulClass.setupFullScreenListener(stage, rootAnchorPane);
    }

    //Переопределённый метод абстрактного класса Application, который вызывается при запуске программы
    //В качестве аргумента принимает окно программы(Stage), данный метод так же может выступать в роли
    //Реализатора запуска приложения
    @Override
    public void start(Stage stage) throws IOException {
        //Метод loadFXML, созданный в UsefulClass, в качестве аргумента принимает три типа:
        //Окно, путь от куда брать разметку, и класс-контроллеров для этой разметки
        //Сам метод подгружает FXML из разметки, создаёт интерфейс(Parent), который и возвращает.
        //А так же, немало важный момент, передаёт окно, в котором будет разметка
        Parent authorizationRoot = UsefulClass.loadFXML(
                stage, "EntranceShape.fxml", EntranceController.class);

        //Создаём сцену-контейнер для всего содержимого окна(Stage может иметь только одну активную сцену)
        //В качестве аргумента принимает Parent(разметку) и размеры сцены
        Scene scene = new Scene(authorizationRoot, 900, 600);
        //В качестве фона будем использовать прозрачный фон, это нужно будет для создания прозрачной рамки окна
        //Что при самом изменении была видимость, что мы растягиваем его за пределами окна(немного дальше границ)
        scene.setFill(Color.TRANSPARENT);
        //Удаляет стандартное оформление окна(нужно, что бы настроить своё оформление окна)
        stage.initStyle(StageStyle.TRANSPARENT);
        //Помещаем созданную сцену в окно, теперь окно знает что ему показывать
        stage.setScene(scene);

        //Добавляем фильтр событий для нашего окна
        //Метод addEventFilter принимает два аргумента 1) какое событие, 2) как обработать
        //Мы указали что у нас будут обрабатываться любые действия мыши(MouseEvent.ANY)
        //А как это делать, мы указали с помощью экземпляра собственного класса ResizeListener
        //Который реализовывает интерфейс EventHandler
        stage.addEventFilter(MouseEvent.ANY, new ResizeListener(stage));
        //Показываем окно на экране пользователя
        stage.show();
    }

    //Обработчик кнопки, будет применяться для кнопки lostPasswordBtn
    @FXML
    public void onLostPasswordBtn() {

    }

    //Метод-обработчик нажатия на кнопку "Вход", меняет содержимое текущей сцены
    //Вместо одной формы показывается другую, без лишних созданий ненужных окон и сцен
    @FXML
    public void onEntranceBtn() throws IOException {
        //Аналогично из метода start
        Parent speechBaseRoot = UsefulClass.loadFXML(
                stage, "SpeechBaseShape.fxml", SpeechBaseController.class);
        //При нажатии на кнопку проводим валидацию данных в TextField
        UsefulClass.updateStyleValidation(Map.of(mailTF, mailLb, passwordF, passwordLb));
        //Если данные заполнены корректно
        if(mailLb.getText().equals("E-MAIL") && passwordLb.getText().equals("ПАРОЛЬ"))
            //Меняем разметку окна авторизации на разметку основного окна
            stage.getScene().setRoot(speechBaseRoot);
    }

    @FXML
    public void onRegisterBtn() throws IOException{
        //Аналогично из метода start
        Parent registrationRoot = UsefulClass.loadFXML(
                stage, "RegistrationShape.fxml", RegistrationController.class);
        //Меняем разметку окна авторизации на разметку окна регистрации
        stage.getScene().setRoot(registrationRoot);
    }
}
