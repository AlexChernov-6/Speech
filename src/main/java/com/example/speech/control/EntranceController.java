package com.example.speech.control;

import com.example.speech.util.HibernateSessionFactory;
import com.example.speech.util.ResizeListener;

import com.example.speech.util.SendingClass;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

import static com.example.speech.util.HelpfulStylingClass.setupFullScreenListener;
import static com.example.speech.util.HelpfulValidationClass.updateStyleValidation;
import static com.example.speech.util.NavigateListener.setEnterPressed;
import static com.example.speech.util.SendingClass.sendPostalDelivery;

public class EntranceController extends Application {
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

    @FXML
    private Button entranceBtn;

    private String startValueEmail, startValuePassword;

    public void initializeData(Stage stage) {
        this.stage = stage;
        //Добавляем обработчик состояния окна(fullScreen или нет)
        setupFullScreenListener(stage, rootAnchorPane);
        setEnterPressed(entranceBtn);
        Platform.runLater(() -> mailTF.requestFocus());
    }

    //Переопределённый метод абстрактного класса Application, который вызывается при запуске программы
    //В качестве аргумента принимает окно программы(Stage), данный метод так же может выступать в роли
    //Реализатора запуска приложения
    @Override
    public void start(Stage stage) throws IOException, SQLException {
        FXMLLoader fxmlLoader = new FXMLLoader(EntranceController.class.getResource(
                "/com/example/speech/shape/EntranceShape.fxml"
        ));
        Parent authorizationRoot = fxmlLoader.load();

        EntranceController controller = fxmlLoader.getController();
        controller.initializeData(stage);

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
        //При запуске приложения создаём пул с подключениями(если не создан), что бы у первого запроса был короткий отклик
        HibernateSessionFactory.getSessionFactory();
        //При запуске приложение так же создаём соединение для отправки писем
        new SendingClass();
    }

    //Обработчик кнопки, будет применяться для кнопки lostPasswordBtn
    @FXML
    public void onLostPasswordBtn() throws IOException {
        //Проводим валидацию поля email, оно не должно быть пустым
        updateStyleValidation(Map.of(mailTF, mailLb));
        if(mailLb.getText().equals("E-MAIL")) {
            sendPostalDelivery(mailTF.getText(), SendingClass.ContextDelivery.SEND_LOST_PASSWORD);
            //Если всё нормально показываем окно с информацией по смене пароля
            new LostPasswordController().showModalLostPasswordStage(stage, mailTF.getText());
        }
    }

    //Метод-обработчик нажатия на кнопку "Вход", меняет содержимое текущей сцены
    //Вместо одной формы показывается другую, без лишних созданий ненужных окон и сцен
    @FXML
    public void onEntranceBtn() throws IOException {
        if (startValueEmail == null)
            startValueEmail = mailLb.getText();

        if (startValuePassword == null)
            startValuePassword = passwordLb.getText();

        FXMLLoader fxmlLoader = new FXMLLoader(EntranceController.class.getResource(
                "/com/example/speech/shape/SpeechBaseShape.fxml"
        ));
        Parent speechBaseRoot = fxmlLoader.load();

        SpeechBaseController controller = fxmlLoader.getController();
        controller.initializeData(stage);

        //При нажатии на кнопку проводим валидацию данных в TextField
        updateStyleValidation(Map.of(mailTF, mailLb, passwordF, passwordLb));
        //Если данные заполнены корректно
        if(mailLb.getText().equals(startValueEmail) && passwordLb.getText().equals(startValuePassword))
            //Меняем разметку окна авторизации на разметку основного окна
            stage.getScene().setRoot(speechBaseRoot);
    }

    @FXML
    public void onRegisterBtn() throws IOException{
        FXMLLoader fxmlLoader = new FXMLLoader(EntranceController.class.getResource(
                "/com/example/speech/shape/RegistrationShape.fxml"
        ));
        Parent registrationRoot = fxmlLoader.load();

        RegistrationController controller = fxmlLoader.getController();
        controller.initializeData(stage);
        //Меняем разметку окна авторизации на разметку окна регистрации
        stage.getScene().setRoot(registrationRoot);
    }
}
