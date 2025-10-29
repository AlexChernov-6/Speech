package com.example.speech;

import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import java.io.IOException;
import java.util.Objects;

public class AuthorizationWindow extends Application {
    //Корневой элемент интерфейса, который загружается с помощью FXMLLoader
    private Parent speechBaseRoot;
    //Окно приложения - то, что видит пользователь
    private Stage stage;
    //Аннотация, указывающая что объекты ниже будут искать в fxml файлe и свяжутся с этими переменными
    @FXML
    private Button lostPasswordBtn, entranceBtn, registerBtn;

    @FXML
    private TextField mailTF, passwordTF;

    @FXML
    private GridPane autorizationGridPane;

    //Метод initialize гарантированно вызывается после инициализации всех полей FXML, поэтому в нём можно работать
    //С различными полями разметки
    @FXML
    public void initialize() {
        setMarginInGridPane(autorizationGridPane, new Insets(10));
    }

    //Переопределённый метод абстрактного класса Application, который вызывается при запуске программы
    //В качестве аргумента принимает окно программы(Stage)
    @Override
    public void start(Stage stage) throws IOException {
        //Загрузчик интерфейса, который собирает внешний вид окна из указанного файла
        //Важно! если файл fxml находиться в другом каталоге, нужно указывать относительный путь к нему
        FXMLLoader loader = new FXMLLoader(getClass().getResource("AutorizationWindowShape.fxml"));
        //Создаёт интерфейс из FXMLLoader, является корневым элементом интерфейса, содержащий все визуальные компоненты
        Parent root = loader.load();

        //Получаем объект контроллера, который управляет действиями в fxml, то есть различные обработчики событий
        AuthorizationWindow controller = loader.getController();
        //Передаём в контроллер экземпляр окна, таким образом контроллер знает с каким окном работает
        controller.setStage(stage);

        //Создаётся контейнер, в котором отображается интерфейс, туда передаём root
        //Ранее созданный интерфейс из файла fxml
        Scene scene = new Scene(root, 800, 800);
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
    private void setStage(Stage stage) throws IOException {
        //Переменной текущего экземпляра присваиваем значение, переданной в метод
        this.stage = stage;
        //Пытаемся загрузить интерфейсы других классов, что бы в дальнейшем совершать переходы между ними
        speechBaseRoot = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("SpeechBaseShape.fxml")));
    }

    public static void setMarginInGridPane(GridPane gridPane, Insets insets) {
        for(Node child : gridPane.getChildren()) {
            GridPane.setMargin(child, insets);
        }
    }

    @FXML
    public void onLostPasswordBtn() {

    }

    //Метод-обработчик нажатия на кнопку "Вход", меняет содержимое текущей сцены
    //Вместо одной формы показывается другую, без лишних созданий ненужных окон и сцен
    @FXML
    public void onEntranceBtn() {
        stage.getScene().setRoot(speechBaseRoot);
    }

    @FXML
    public void onRegisterBtn() {

    }
}
