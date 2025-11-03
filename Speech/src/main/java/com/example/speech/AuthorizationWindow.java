package com.example.speech;

import javafx.application.Application;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import java.io.IOException;
import java.util.Objects;

public class AuthorizationWindow extends Application {
    //Корневой элемент интерфейса, который загружается с помощью FXMLLoader
    //Хранит в себе разметку-nodes(узлы)
    private Parent speechBaseRoot;
    //Корневой контейнер самого высокого уровня, то что пользователь видит как окно приложения(окно ОС)
    private Stage stage;
    //Аннотация, указывающая что объекты ниже будут искать в fxml файлe и свяжутся с этими переменными
    @FXML
    private Button lostPasswordBtn, entranceBtn, registerBtn;

    @FXML
    private TextField mailTF;

    @FXML
    private PasswordField passwordF;

    @FXML
    private AnchorPane rootAnchorPane;

    @FXML
    private Label mailLb, passwordLb;

    //Метод initialize гарантированно вызывается после инициализации всех полей FXML, поэтому в нём можно работать
    //С различными полями разметки
    @FXML
    public void initialize() {
        //Подгружаем файл, откуда будут использоваться стили для контроллеров, переданных в качестве аргумента
        setStyleSheets(mailTF, passwordF, mailLb, passwordLb);
    }

    //Переопределённый метод абстрактного класса Application, который вызывается при запуске программы
    //В качестве аргумента принимает окно программы(Stage), данный метод так же может выступать в роли
    //Реализатора запуска приложения
    @Override
    public void start(Stage stage) throws IOException {
        //Загрузчик интерфейса, который собирает внешний вид окна из указанного файла
        FXMLLoader loader = new FXMLLoader(getClass().getResource("AutorizationWindowShape.fxml"));
        //Создаёт интерфейс из FXMLLoader, является корневым элементом интерфейса, содержащий все визуальные компоненты
        Parent root = loader.load();

        //Получаем объект контроллера, который управляет действиями в fxml, то есть различные обработчики событий
        //Данный метод автоматически связывает Java-код контроллера с FXML-разметкой
        AuthorizationWindow controller = loader.getController();
        //Передаём в контроллер экземпляр окна, таким образом контроллер знает в каком окне он работает
        controller.initializeController(stage);

        //Создаём сцену-контейнер для всего содержимого окна(Stage может иметь только одну активную сцену)
        //В качестве аргумента принимает Parent(разметку) и размеры сцены
        Scene scene = new Scene(root, 900, 600);
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

    //Метод для передачи окна внутри контроллера, принимает параметр типа Stage
    //Метод нужен, что бы получить доступ к текущему окну, и в дальнейшем с ним можно было взаимодействовать
    private void initializeController(Stage stage) throws IOException {
        //Переменной текущего экземпляра присваиваем значение, переданной в метод
        this.stage = stage;
        //Пытаемся загрузить интерфейсы других классов, что бы в дальнейшем совершать переходы между ними
        //Для оптимального перехода мы не будем пересоздавать окно и сцену, мы будем менять разметку внутри сцены
        speechBaseRoot = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("SpeechBaseShape.fxml")));

        //Добавляем обработчик состояния окна(fullScreen или нет)
        setupFullScreenListener();
    }

    //Обработчик кнопки, будет применяться для кнопки lostPasswordBtn
    @FXML
    public void onLostPasswordBtn() {

    }

    //Метод-обработчик нажатия на кнопку "Вход", меняет содержимое текущей сцены
    //Вместо одной формы показывается другую, без лишних созданий ненужных окон и сцен
    @FXML
    public void onEntranceBtn() {
        //При нажатии на кнопку проводим валидацию данных в TextField
        updateLabels();
        //Если данные заполнены корректно
        if(mailLb.getText().equals("E-MAIL") && passwordLb.getText().equals("ПАРОЛЬ"))
            //Меняем разметку окна авторизации на разметку основного окна
            stage.getScene().setRoot(speechBaseRoot);
    }

    @FXML
    public void onRegisterBtn() {

    }

    //Метод, который следит за изменениями состояний окна, а конкретно за FullScreen
    private void setupFullScreenListener() {
        //Добавляем обработчика, то что следит за изменениями для fullScreenProperty
        //Вместо создания отдельного класса, который реализовывает интерфейс InvalidationListener
        //Используем лямбда-функцию, в которой переопределим необходимый метод invalidated
        stage.fullScreenProperty().addListener(observable -> {
            //Если у нас окно открыто в полный экран-то есть имеет state FullScreen, то мы убираем отступы у корневого
            //AnchorPane в обратном случае, мы возвращаем ему отступы для его содержимого
            if (stage.isFullScreen()) {
                rootAnchorPane.setPadding(new Insets(0));
            } else {
                rootAnchorPane.setPadding(new Insets(13));
            }
        });

        // Устанавливаем начальное значение
        if (stage.isFullScreen()) {
            rootAnchorPane.setPadding(new Insets(0));
        } else {
            rootAnchorPane.setPadding(new Insets(13));
        }
    }

    //Метод, в котором происходит валидация полей ввода текста
    private void updateLabels() {
        //Создаём две переменные-результата, что бы инициализировать которые, мы воспользуемся статическим методом
        //Из класса-помощника, в методе validationField обработана валидация контроллеров, которые наследуются от TextInputControl
        String resultValidEmail = UsefulClass.validationField(mailTF);
        String resultValidPassword = UsefulClass.validationField(passwordF);

        //Проверяем что значение resultValidEmail != null => данные не удовлетворяют валидации, меняем текста mailLb
        //На описание проблемы, а так же меняем цвет текста и подсвечиваем красным рамку TextField
        if (resultValidEmail != null) {
            mailLb.setText(resultValidEmail);
            mailLb.setStyle("-fx-text-fill: rgba(115,0,0); -fx-font-weight: bold;");
            mailTF.setStyle("-fx-border-color: rgba(115,0,0);");
        }//Если значение null => данные удовлетворяют валидации, возвращаем прежний стиль, перед этим отчистив inline стили
        else {
            //Очищаем inline стили, которые имеют высший приоритет
            mailLb.setStyle("");
            mailTF.setStyle("");

            mailLb.setText("E-MAIL");
            mailLb.getStyleClass().add("other-information");
            mailLb.setStyle("-fx-font-weight: bold;");
            mailTF.getStyleClass().add("password-field");
        }

        //Аналогично как и с mailTF
        if (resultValidPassword != null) {
            passwordLb.setText(resultValidPassword);
            passwordLb.setStyle("-fx-text-fill: rgba(115,0,0); -fx-font-weight: bold;");
            passwordF.setStyle("-fx-border-color: rgba(115,0,0);");
        }
        else {
            passwordLb.setStyle("");
            passwordF.setStyle("");

            passwordLb.setText("ПАРОЛЬ");
            passwordLb.getStyleClass().add("other-information");
            passwordLb.setStyle("-fx-font-weight: bold;");
            passwordF.getStyleClass().add("password-field");
        }
    }

    //Аннотация SafeVarargs показывает что мы знаем, что используем безопасные переменные(не будет исключений)
    @SafeVarargs
    //Сам метод подгружает файл, откуда будут использоваться стили для контроллеров, переданных в качестве аргумента
    //Метод имеет дженирик(обобщение) и в качестве аргумента может принимать любой экземпляр
    //Который является наследником класса Control, в качестве аргумента может принимать неограниченное число типа T
    public final <T extends Control> void setStyleSheets(T... controls) {
        for (T control : controls) {
            control.getStylesheets().add(getClass().getResource("/com/example/speech/styles.css")
                    .toExternalForm());
        }
    }
}
