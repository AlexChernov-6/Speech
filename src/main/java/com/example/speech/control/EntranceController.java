package com.example.speech.control;

import com.example.speech.model.Channel;
import com.example.speech.model.Message;
import com.example.speech.model.User;
import com.example.speech.service.*;
import com.example.speech.util.*;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Callback;
import oshi.SystemInfo;
import oshi.hardware.HardwareAbstractionLayer;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

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

    private UserService userService = new UserService();

    public final static SystemInfo SYSTEM_INFO = new SystemInfo();
    public final static HardwareAbstractionLayer HARDWARE_ABSTRACTION_LAYER = SYSTEM_INFO.getHardware();

    public final static ConfigManager CONFIG_MANAGER = new ConfigManager();

    public static final List<String> EMOJI_LIST = List.of(
            "😀", "😃", "😄", "😁", "😆", "😅", "😂", "🤣", "😊", "😇", "🙂", "🙃", "😉", "😌", "😍", "😘", "😗", "😙", "😚", "😋", "😛", "😝", "😜", "🤓", "😎", "😏", "😒", "😞", "😔", "😟", "😕", "🙁", "😣", "😖", "😫", "😩", "😢", "😭", "😤", "😠", "😡", "😳", "😱", "😨", "😰", "😥", "😓", "🤗", "🤔", "🤥", "😶", "😐", "😑", "😬", "🙄", "😯", "😦", "😧", "😮", "😲", "😴", "🤤", "😪", "😵", "🤐", "🤢", "🤧", "😷", "🤒", "🤕", "🤑", "🤠", "😈", "👿", "👹", "👺", "🤡", "💩", "👻", "💀", "👽", "👾", "🤖", "🎃", "😺", "😸", "😹", "😻", "😼", "😽", "🙀", "😿", "😾", "👐", "🤝", "🙏", "💅", "🤳", "💪", "👂", "👃", "👀", "👅", "👄", "💋"
    );

    private Stage adminStage;

    private TableView<Channel> channelTableView;
    private ObservableList<Channel> channelObservableList;
    private FilteredList<Channel> channelFilteredList;
    private SortedList<Channel> channelSortedList;
    private final ChannelService adminChannelService = new ChannelService(HibernateAdminSessionFactory.getSessionFactory());
    private final Comparator<Channel> channelIdComPlus = (c1, c2) -> Integer.compare(c1.getChannelID(), c2.getChannelID());
    private final Comparator<Channel> channelCountUserComPlus = (c1, c2) -> Integer.compare(c1.getChannelCountUser(), c2.getChannelCountUser());
    private final Comparator<Channel> channelTypeComPlus = (c1, c2) -> c1.getChannelType().getChannelTypeName().compareTo(c2.getChannelType().getChannelTypeName());
    private final Comparator<Channel> channelNameComPlus = (c1, c2) -> c1.getChannel_name_unique().compareTo(c2.getChannel_name_unique());
    private final Comparator<Channel> channelOwnerComPlus = (c1, c2) -> {
        if (c2.getOwnerUser() == null && c1.getOwnerUser() == null)
            return 0;
        else if (c2.getOwnerUser() != null && c1.getOwnerUser() != null)
            return c1.getOwnerUser().getNameUser().compareTo(c2.getOwnerUser().getNameUser());
        else if (c2.getOwnerUser() != null)
            return c2.getOwnerUser().getNameUser().compareTo("");
        else
            return "".compareTo(c1.getOwnerUser().getNameUser());
    };
    private final Comparator<Channel> channelIdComMinus = (c1, c2) -> Integer.compare(c2.getChannelID(), c1.getChannelID());
    private final Comparator<Channel> channelCountUserComMinus = (c1, c2) -> Integer.compare(c2.getChannelCountUser(), c1.getChannelCountUser());
    private final Comparator<Channel> channelTypeComMinus = (c1, c2) -> c2.getChannelType().getChannelTypeName().compareTo(c1.getChannelType().getChannelTypeName());
    private final Comparator<Channel> channelNameComMinus = (c1, c2) -> c2.getChannel_name_unique().compareTo(c1.getChannel_name_unique());
    private final Comparator<Channel> channelOwnerComMinus = ((c1, c2) -> {
        if (c2.getOwnerUser() == null && c1.getOwnerUser() == null)
            return 0;
        else if (c2.getOwnerUser() != null && c1.getOwnerUser() != null)
            return c2.getOwnerUser().getNameUser().compareTo(c1.getOwnerUser().getNameUser());
        else if (c2.getOwnerUser() != null)
            return c2.getOwnerUser().getNameUser().compareTo("");
        else
            return "".compareTo(c1.getOwnerUser().getNameUser());
    });

    private TableView<User> userTableView;
    private ObservableList<User> userObservableList;
    private FilteredList<User> userFilteredList;
    private SortedList<User> userSortedList;
    private final UserService adminUserService = new UserService(HibernateAdminSessionFactory.getSessionFactory());
    private final Comparator<User> userIdComPlus = (c1, c2) -> Integer.compare(c1.getIdUser(), c2.getIdUser());
    private final Comparator<User> userEmailComPlus = (c1, c2) -> c1.getEmailUser().compareTo(c2.getEmailUser());
    private final Comparator<User> userNameComPlus = (c1, c2) -> c1.getNameUser().compareTo(c2.getNameUser());
    private final Comparator<User> userDateComPlus = (c1, c2) -> c1.getBirthdayUser().compareTo(c2.getBirthdayUser());
    private final Comparator<User> userStatusComPlus = (c1, c2) -> c1.getStatusUser().compareTo(c2.getStatusUser());
    private final Comparator<User> userIdComMinus = (c1, c2) -> Integer.compare(c2.getIdUser(), c1.getIdUser());
    private final Comparator<User> userEmailComMinus = (c1, c2) -> c2.getEmailUser().compareTo(c1.getEmailUser());
    private final Comparator<User> userNameComMinus = (c1, c2) -> c2.getNameUser().compareTo(c1.getNameUser());
    private final Comparator<User> userDateComMinus = (c1, c2) -> c2.getBirthdayUser().compareTo(c1.getBirthdayUser());
    private final Comparator<User> userStatusComMinus = (c1, c2) -> c2.getStatusUser().compareTo(c1.getStatusUser());

    private TableView<Message> messageTableView;
    private ObservableList<Message> messageObservableList;
    private FilteredList<Message> messageFilteredList;
    private SortedList<Message> messageSortedList;
    private final MessageService adminMessageService = new MessageService(HibernateAdminSessionFactory.getSessionFactory());
    private final Comparator<Message> messageIdComPlus = (c1, c2) -> Long.compare(c1.getMessageId(), c2.getMessageId());
    private final Comparator<Message> messageDateTimeComPlus = (c1, c2) -> c1.getMessageDatetime().compareTo(c2.getMessageDatetime());
    private final Comparator<Message> messageRecipientComPlus = (c1, c2) -> c1.getChannelUser().getUser().getNameUser().compareTo(c2.getChannelUser().getUser().getNameUser());
    private final Comparator<Message> messageChannelNameComPlus = (c1, c2) -> c1.getChannelUser().getChannel().getChannel_name_unique().compareTo(c2.getChannelUser().getChannel().getChannel_name_unique());
    private final Comparator<Message> messageModTimeComPlus = (c1, c2) -> c1.isModifiedMessage().compareTo(c2.isModifiedMessage());
    private final Comparator<Message> messagePinTimeComPlus = (c1, c2) -> c1.getPinMessage().compareTo(c2.getPinMessage());
    private final Comparator<Message> messageCountContentTimeComPlus = (c1, c2) -> Integer.compare(c1.getMessageContent().size(), c2.getMessageContent().size());
    private final Comparator<Message> messageIdComMinus = (c1, c2) -> Long.compare(c2.getMessageId(), c1.getMessageId());
    private final Comparator<Message> messageDateTimeComMinus = (c1, c2) -> c2.getMessageDatetime().compareTo(c1.getMessageDatetime());
    private final Comparator<Message> messageRecipientComMinus = (c1, c2) -> c2.getChannelUser().getUser().getNameUser().compareTo(c1.getChannelUser().getUser().getNameUser());
    private final Comparator<Message> messageChannelNameComMinus = (c1, c2) -> c2.getChannelUser().getChannel().getChannel_name_unique().compareTo(c1.getChannelUser().getChannel().getChannel_name_unique());
    private final Comparator<Message> messageModTimeComMinus = (c1, c2) -> c2.isModifiedMessage().compareTo(c1.isModifiedMessage());
    private final Comparator<Message> messagePinTimeComMinus = (c1, c2) -> c2.getPinMessage().compareTo(c1.getPinMessage());
    private final Comparator<Message> messageCountContentTimeComMinus = (c1, c2) -> Integer.compare(c2.getMessageContent().size(), c1.getMessageContent().size());

    private AnchorPane baseAP;

    private Label countVisibleRow;

    private String searchStr;

    private HBox searchHB, centralHB;

    private ListView<String> sortedListView;
    private ToggleGroup sortedGroup;
    private RadioButton plusRB;

    private Button filterDataBtn;

    private TextField intTo, intFrom;

    private HBox numberHB, dateHB;

    private DatePicker dateFrom, dateTo;

    private ComboBox<String> comboBox;

    public void initializeData(Stage stage) {
        this.stage = stage;
        //Добавляем обработчик состояния окна(fullScreen или нет)
        setupFullScreenListener(stage, rootAnchorPane);
        setEnterPressed(entranceBtn);

        Platform.runLater(() -> mailTF.requestFocus());
        String email = CONFIG_MANAGER.getUserEmail();
        String password = CONFIG_MANAGER.getUserPassword();
        mailTF.setText(email);
        passwordF.setText(password);
        if (email != null && password != null) {
            try {
                onEntranceBtn();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    //Переопределённый метод абстрактного класса Application, который вызывается при запуске программы
    //В качестве аргумента принимает окно программы(Stage), данный метод так же может выступать в роли
    //Реализатора запуска приложения
    @Override
    public void start(Stage stage) throws IOException, SQLException {
        this.stage = stage;
        FXMLLoader fxmlLoader = new FXMLLoader(EntranceController.class.getResource(
                "/com/example/speech/shape/EntranceShape.fxml"
        ));
        Parent authorizationRoot = fxmlLoader.load();

        EntranceController controller = fxmlLoader.getController();
        controller.initializeData(stage);

        //Создаём сцену-контейнер для всего содержимого окна(Stage может иметь только одну активную сцену)
        //В качестве аргумента принимает Parent(разметку) и размеры сцены
        Scene scene = new Scene(authorizationRoot, CONFIG_MANAGER.getWindowWidth(), CONFIG_MANAGER.getWindowHeight());
        stage.setX(CONFIG_MANAGER.getWindowX());
        stage.setY(CONFIG_MANAGER.getWindowY());
        stage.setFullScreen(CONFIG_MANAGER.getIsFullScreen());
        //В качестве фона будем использовать прозрачный фон, это нужно будет для создания прозрачной рамки окна
        //Что при самом изменении была видимость, что мы растягиваем его за пределами окна(немного дальше границ)
        scene.setFill(Color.TRANSPARENT);

        scene.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            if (e.isControlDown() && e.isShiftDown() && e.getCode() == KeyCode.L) {
                if (adminStage == null)
                    createAdminWindow();
                else
                    adminStage.show();
            }
        });
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
        stage.getIcons().setAll(
                new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/example/speech/image/icon-app.png"))));
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
        if (mailLb.getText().equals("E-MAIL")) {
            Thread sendLostPasswordThread = new Thread(() -> {
                sendPostalDelivery(mailTF.getText(), SendingClass.ContextDelivery.SEND_LOST_PASSWORD);
            });
            sendLostPasswordThread.setDaemon(true);
            sendLostPasswordThread.start();

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

        //При нажатии на кнопку проводим валидацию данных в TextField
        updateStyleValidation(Map.of(mailTF, mailLb, passwordF, passwordLb));
        //Если данные заполнены корректно
        if (mailLb.getText().equals(startValueEmail) && passwordLb.getText().equals(startValuePassword)) {
            //Меняем разметку окна авторизации на разметку основного окна
            User currUser = userService.getUserByEmail(mailTF.getText());

            if (currUser.getPasswordUser().equals(passwordF.getText())) {
                currUser.setUniqueIdentityComputer(HARDWARE_ABSTRACTION_LAYER.getComputerSystem().getHardwareUUID());
                userService.update(currUser);

                CONFIG_MANAGER.setUserEmail(currUser.getEmailUser());
                CONFIG_MANAGER.setUserPassword(currUser.getPasswordUser());
                CONFIG_MANAGER.save();

                Platform.runLater(() -> {
                    controller.initializeData(stage, currUser);
                    stage.getScene().setRoot(speechBaseRoot);
                });
            } else {
                mailLb.setText("Неверная пара логин и пароль");
                mailLb.setStyle("-fx-text-fill: rgba(115,0,0);");
                mailTF.setStyle("-fx-border-color: rgba(115,0,0);");
            }
        }
    }

    @FXML
    public void onRegisterBtn() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(EntranceController.class.getResource(
                "/com/example/speech/shape/RegistrationShape.fxml"
        ));
        Parent registrationRoot = fxmlLoader.load();

        RegistrationController controller = fxmlLoader.getController();
        controller.initializeData(stage);
        //Меняем разметку окна авторизации на разметку окна регистрации
        stage.getScene().setRoot(registrationRoot);
    }

    private void createAdminWindow() {
        stage.setIconified(true);
        Scene adminScene = new Scene(createRootAdminWindowSP(), CONFIG_MANAGER.getWindowWidth(), CONFIG_MANAGER.getWindowHeight());
        String css = getClass().getResource("/com/example/speech/styles.css").toExternalForm();
        adminScene.getStylesheets().add(css);
        adminStage = new Stage();
        adminStage.setMinHeight(ResizeListener.MIN_HEIGHT);
        adminStage.setMinWidth(ResizeListener.MIN_WIDTH);
        adminStage.setX(CONFIG_MANAGER.getWindowX());
        adminStage.setY(CONFIG_MANAGER.getWindowY());
        adminStage.setFullScreen(CONFIG_MANAGER.getIsFullScreen());
        adminStage.setScene(adminScene);
        adminStage.setTitle("Окно администратора(аналитика данных)");
        adminStage.show();
    }

    private StackPane createRootAdminWindowSP() {
        StackPane rootSP = new StackPane();

        Button userGuideBtn = new Button();
        StackPane.setMargin(userGuideBtn, new Insets(20, 20, 0, 0));
        StackPane.setAlignment(userGuideBtn, Pos.TOP_RIGHT);
        rootSP.getChildren().add(userGuideBtn);
        HelpfulClass.setImageWithButton(userGuideBtn, "questions.png");
        userGuideBtn.setTooltip(new Tooltip("Открыть руководство пользователя"));
        userGuideBtn.setOnAction(e -> {
            if (Desktop.isDesktopSupported()) {
                Desktop desktop = Desktop.getDesktop();
                File file = Paths.get("Документы/РУКОВОДСТВО АДМИНИСТРАТОРА.docx").toFile();
                if (file.exists()) {
                    try {
                        desktop.open(file);
                    } catch (IOException ignore) {
                    }
                }
            } else
                HelpfulClass.openWebPage("https://metanit.com/java/javafx/3.2.php");
        });

        VBox authenticationVB = new VBox(10);
        authenticationVB.setPadding(new Insets(15));
        authenticationVB.setMaxHeight(220);
        authenticationVB.setMaxWidth(250);
        authenticationVB.setStyle("-fx-background-color: rgba(250, 250, 250);");
        rootSP.getChildren().add(authenticationVB);

        VBox loginVB = new VBox();
        loginVB.setMaxHeight(Region.USE_PREF_SIZE);
        authenticationVB.getChildren().add(loginVB);

        Label loginHintLB = new Label("Логин");
        loginVB.getChildren().add(loginHintLB);

        TextField loginTF = new TextField();
        loginTF.setPromptText("Логин");
        loginTF.setUserData(loginHintLB);
        loginTF.setPrefWidth(250 - 15 - 15);
        loginVB.getChildren().add(loginTF);

        VBox passwordVB = new VBox();
        passwordVB.setMaxHeight(Region.USE_PREF_SIZE);
        authenticationVB.getChildren().add(passwordVB);

        Label passwordHintLB = new Label("Пароль");
        passwordVB.getChildren().add(passwordHintLB);

        PasswordField passwordTF = new PasswordField();
        passwordTF.setPromptText("Пароль");
        passwordTF.setUserData(passwordHintLB);
        passwordTF.setPrefWidth(250 - 15 - 15);
        passwordVB.getChildren().add(passwordTF);

        VBox bottomVB = new VBox();
        VBox.setVgrow(bottomVB, Priority.ALWAYS);
        bottomVB.setAlignment(Pos.BOTTOM_CENTER);
        authenticationVB.getChildren().add(bottomVB);

        AdminService adminService = new AdminService();

        Button entranceBtn = new Button("Войти");
        entranceBtn.setPrefWidth(250 - 15 - 15);
        bottomVB.getChildren().add(entranceBtn);
        entranceBtn.setOnAction(e -> {
            if (passwordTF.getText() == null || passwordTF.getText().isEmpty()) {
                passwordHintLB.setText("Поле не может быть пустым");
                passwordHintLB.setStyle("-fx-text-fill: rgba(115, 0, 0);");
            } else {
                passwordHintLB.setText("Пароль");
                passwordHintLB.setStyle("");
            }

            if (loginTF.getText() == null || loginTF.getText().isEmpty()) {
                loginHintLB.setText("Поле не может быть пустым");
                loginHintLB.setStyle("-fx-text-fill: rgba(115, 0, 0);");
                return;
            } else {
                loginHintLB.setText("Логин");
                loginHintLB.setStyle("");
            }

            if (adminService.getAdminByLogin(loginTF.getText()) == null) {
                loginHintLB.setText("Админа с таким логином не существует");
                loginHintLB.setStyle("-fx-text-fill: rgba(115, 0, 0);");
            } else {
                if (adminService.authenticationVerification(loginTF.getText(), passwordTF.getText()) == null) {
                    loginHintLB.setText("Неверный логин или пароль");
                    loginHintLB.setStyle("-fx-text-fill: rgba(115, 0, 0);");
                } else {
                    rootSP.getChildren().removeAll(userGuideBtn, authenticationVB);
                    rootSP.getChildren().add(createBaseAP());
                }
            }
        });

        return rootSP;
    }

    private AnchorPane createBaseAP() {
        baseAP = new AnchorPane();

        MenuBar menuBar = new MenuBar();
        menuBar.setPrefHeight(25);
        menuBar.setPadding(new Insets(0, 10, 0, 10));
        AnchorPane.setTopAnchor(menuBar, 0.0);
        AnchorPane.setLeftAnchor(menuBar, 0.0);
        AnchorPane.setRightAnchor(menuBar, 0.0);
        baseAP.getChildren().add(menuBar);

        Menu menuTables = new Menu();
        menuTables.setText("Таблицы");
        menuTables.setMnemonicParsing(false);
        menuBar.getMenus().add(menuTables);

        MenuItem channelsTableMenuItem = new MenuItem();
        channelsTableMenuItem.setMnemonicParsing(false);
        channelsTableMenuItem.setText("Показать таблицу каналов");
        channelsTableMenuItem.setOnAction(e -> showChannelTableView());
        menuTables.getItems().add(channelsTableMenuItem);

        MenuItem usersTableMenuItem = new MenuItem();
        usersTableMenuItem.setMnemonicParsing(false);
        usersTableMenuItem.setText("Показать таблицу пользователей");
        usersTableMenuItem.setOnAction(e -> showUserTableView());
        menuTables.getItems().add(usersTableMenuItem);

        MenuItem messagesTableMenuItem = new MenuItem();
        messagesTableMenuItem.setMnemonicParsing(false);
        messagesTableMenuItem.setText("Показать таблицу сообщений");
        messagesTableMenuItem.setOnAction(e -> showMessageTableView());
        menuTables.getItems().add(messagesTableMenuItem);

        Menu menuExports = new Menu();
        menuExports.setText("Экспорт данных");
        menuExports.setMnemonicParsing(false);
        menuBar.getMenus().add(menuExports);

        MenuItem channelsExportTableMenuItem = new MenuItem();
        channelsExportTableMenuItem.setMnemonicParsing(false);
        channelsExportTableMenuItem.setText("Экспортировать таблицу каналов");
        channelsExportTableMenuItem.setOnAction(e -> {
            List<Channel> actualAllRow = adminChannelService.getAllRow();
            if (actualAllRow == null || actualAllRow.isEmpty()) {
                showAlert("Нет данных", "Таблица каналов пуста.");
                return;
            }
            DirectoryChooser directoryChooser = new DirectoryChooser();
            directoryChooser.setTitle("Выберите папку для сохранения отчёта");
            Path downloadsPath = Paths.get(System.getProperty("user.home"), "Downloads");
            File downloadsDir = downloadsPath.toFile();
            if (downloadsDir.exists() && downloadsDir.isDirectory()) {
                directoryChooser.setInitialDirectory(downloadsDir);
            } else {
                directoryChooser.setInitialDirectory(new File(System.getProperty("user.home")));
            }

            File selectedDir = directoryChooser.showDialog(adminStage);
            if (selectedDir != null) {
                String fileName = "channels_report_" + LocalDate.now().format(DateTimeFormatter.ofPattern("dd_MM_yyyy")) + ".xlsx";
                Path targetPath = selectedDir.toPath().resolve(fileName);
                try {
                    if(channelSortedList == null || channelSortedList.isEmpty())
                        GeneratedXLSX.exportChannelsToExcel(actualAllRow, targetPath);
                    else
                        GeneratedXLSX.exportChannelsToExcel(new ArrayList<>(channelSortedList), targetPath);
                    showAlert("Успех", "Отчёт сохранён:\n" + targetPath);
                } catch (IOException ex) {
                    showAlert("Ошибка", "Не удалось сохранить файл: " + ex.getMessage());
                }
            }
        });
        menuExports.getItems().add(channelsExportTableMenuItem);

        MenuItem usersExportTableMenuItem = new MenuItem();
        usersExportTableMenuItem.setMnemonicParsing(false);
        usersExportTableMenuItem.setText("Экспортировать таблицу пользователей");
        usersExportTableMenuItem.setOnAction(e -> {
            List<User> actualAllRow = adminUserService.getAllRow();
            if (actualAllRow == null || actualAllRow.isEmpty()) {
                showAlert("Нет данных", "Таблица пользователей пуста.");
                return;
            }
            DirectoryChooser directoryChooser = new DirectoryChooser();
            directoryChooser.setTitle("Выберите папку для сохранения отчёта");
            Path downloadsPath = Paths.get(System.getProperty("user.home"), "Downloads");
            File downloadsDir = downloadsPath.toFile();
            if (downloadsDir.exists() && downloadsDir.isDirectory()) {
                directoryChooser.setInitialDirectory(downloadsDir);
            } else {
                directoryChooser.setInitialDirectory(new File(System.getProperty("user.home")));
            }

            File selectedDir = directoryChooser.showDialog(adminStage);
            if (selectedDir != null) {
                String fileName = "users_report_" + LocalDate.now().format(DateTimeFormatter.ofPattern("dd_MM_yyyy")) + ".xlsx";
                Path targetPath = selectedDir.toPath().resolve(fileName);
                try {
                    if(userSortedList == null || userSortedList.isEmpty())
                        GeneratedXLSX.exportUsersToExcel(actualAllRow, targetPath);
                    else
                        GeneratedXLSX.exportUsersToExcel(new ArrayList<>(userSortedList), targetPath);
                    showAlert("Успех", "Отчёт сохранён:\n" + targetPath);
                } catch (IOException ex) {
                    showAlert("Ошибка", "Не удалось сохранить файл: " + ex.getMessage());
                }
            }
        });
        menuExports.getItems().add(usersExportTableMenuItem);

        MenuItem messagesExportTableMenuItem = new MenuItem();
        messagesExportTableMenuItem.setMnemonicParsing(false);
        messagesExportTableMenuItem.setText("Экспортировать таблицу сообщений");
        messagesExportTableMenuItem.setOnAction(e -> {
            List<Message> actualAllRow = adminMessageService.getAllRow();
            if (actualAllRow == null || actualAllRow.isEmpty()) {
                showAlert("Нет данных", "Таблица сообщений пуста.");
                return;
            }
            DirectoryChooser directoryChooser = new DirectoryChooser();
            directoryChooser.setTitle("Выберите папку для сохранения отчёта");
            Path downloadsPath = Paths.get(System.getProperty("user.home"), "Downloads");
            File downloadsDir = downloadsPath.toFile();
            if (downloadsDir.exists() && downloadsDir.isDirectory()) {
                directoryChooser.setInitialDirectory(downloadsDir);
            } else {
                directoryChooser.setInitialDirectory(new File(System.getProperty("user.home")));
            }

            File selectedDir = directoryChooser.showDialog(adminStage);
            if (selectedDir != null) {
                String fileName = "messages_report_" + LocalDate.now().format(DateTimeFormatter.ofPattern("dd_MM_yyyy")) + ".xlsx";
                Path targetPath = selectedDir.toPath().resolve(fileName);
                try {
                    if(messageSortedList == null || messageSortedList.isEmpty())
                        GeneratedXLSX.exportMessagesToExcel(actualAllRow, targetPath);
                    else
                        GeneratedXLSX.exportMessagesToExcel(new ArrayList<>(messageSortedList), targetPath);
                    showAlert("Успех", "Отчёт сохранён:\n" + targetPath);
                } catch (IOException ex) {
                    showAlert("Ошибка", "Не удалось сохранить файл: " + ex.getMessage());
                }
            }
        });
        menuExports.getItems().add(messagesExportTableMenuItem);

        Menu menuUserGuide = new Menu();
        menuUserGuide.setText("Руководство пользователя");
        menuUserGuide.setMnemonicParsing(false);
        menuBar.getMenus().add(menuUserGuide);

        MenuItem userGuideTableMenuItem = new MenuItem();
        userGuideTableMenuItem.setMnemonicParsing(false);
        userGuideTableMenuItem.setText("Открыть руководство пользователя");
        userGuideTableMenuItem.setOnAction(e -> {
            if (Desktop.isDesktopSupported()) {
                Desktop desktop = Desktop.getDesktop();
                File file = Paths.get("Документы/РУКОВОДСТВО АДМИНИСТРАТОРА.docx").toFile();
                if (file.exists()) {
                    try {
                        desktop.open(file);
                    } catch (IOException ignore) { }
                }
            } else
                HelpfulClass.openWebPage("https://metanit.com/java/javafx/3.2.php");
        });
        menuUserGuide.getItems().add(userGuideTableMenuItem);

        searchHB = new HBox(10);
        AnchorPane.setTopAnchor(searchHB, 35.0);
        AnchorPane.setRightAnchor(searchHB, 20.0);
        AnchorPane.setLeftAnchor(searchHB, 20.0);
        searchHB.setAlignment(Pos.CENTER_LEFT);
        searchHB.setPrefHeight(30);
        baseAP.getChildren().add(searchHB);

        TextField searchTF = new TextField();
        searchTF.setPromptText("Введите строку поиска");
        searchTF.setUserData("searchNode");
        searchHB.getChildren().add(searchTF);

        Button searchBtn = new Button("Найти");
        searchHB.getChildren().add(searchBtn);
        searchBtn.setUserData("searchNode");
        searchBtn.setOnAction(e -> {
            searchStr = searchTF.getText();
            if (channelTableView != null && channelTableView.isVisible())
                channelTableView.refresh();
            else if (userTableView != null && userTableView.isVisible())
                userTableView.refresh();
            else if (messageTableView != null && messageTableView.isVisible())
                messageTableView.refresh();
        });

        filterDataBtn = new Button("Фильтровать");
        filterDataBtn.setUserData("filterNode");
        searchHB.getChildren().add(filterDataBtn);
        filterDataBtn.setOnAction(e -> {

        });

        Button showAllBtn = new Button("Показать всё");
        showAllBtn.setUserData("filterNode");
        searchHB.getChildren().add(showAllBtn);
        showAllBtn.setOnAction(e -> {
            if (channelTableView != null && channelTableView.isVisible())
                channelFilteredList.setPredicate(c -> true);
            else if (userTableView != null && userTableView.isVisible())
                userFilteredList.setPredicate(u -> true);
            else if (messageTableView != null && messageTableView.isVisible())
                messageFilteredList.setPredicate(m -> true);

            updateCountVisibleRow();
        });

        centralHB = new HBox(10);
        AnchorPane.setTopAnchor(centralHB, 80.0);
        AnchorPane.setRightAnchor(centralHB, 20.0);
        AnchorPane.setBottomAnchor(centralHB, 60.0);
        AnchorPane.setLeftAnchor(centralHB, 20.0);
        centralHB.setAlignment(Pos.TOP_LEFT);
        baseAP.getChildren().add(centralHB);

        VBox sortedVB = new VBox(5);
        sortedVB.setAlignment(Pos.TOP_LEFT);
        sortedVB.setMaxHeight(Region.USE_PREF_SIZE);
        centralHB.getChildren().add(sortedVB);

        Label choseColumnSortedLB = new Label("Выберите поле сортировки\nи фильтрации");
        choseColumnSortedLB.setWrapText(true);
        sortedVB.getChildren().add(choseColumnSortedLB);

        sortedListView = new ListView<>();
        sortedListView.setMaxHeight(Region.USE_PREF_SIZE);
        sortedListView.setMaxWidth(Region.USE_PREF_SIZE);
        sortedVB.getChildren().add(sortedListView);
        sortedListView.getSelectionModel().selectedItemProperty()
                .addListener((ob, oldV, newV) -> {
                    sortData();
                    updateFilteredControl();
                });

        sortedGroup = new ToggleGroup();

        plusRB = new RadioButton("По возрастанию");
        plusRB.setToggleGroup(sortedGroup);
        sortedGroup.selectToggle(plusRB);
        sortedVB.getChildren().add(plusRB);
        plusRB.setOnAction(e -> sortData());

        RadioButton minusRB = new RadioButton("По убыванию");
        minusRB.setToggleGroup(sortedGroup);
        sortedVB.getChildren().add(minusRB);
        minusRB.setOnAction(e -> sortData());

        countVisibleRow = new Label();
        AnchorPane.setBottomAnchor(countVisibleRow, 20.0);
        AnchorPane.setLeftAnchor(countVisibleRow, 20.0);
        baseAP.getChildren().add(countVisibleRow);

        channelsTableMenuItem.fire();

        return baseAP;
    }

    private void hiddenAllTableViewInAdminBaseAP() {
        for (Node node : centralHB.getChildren()) {
            if (node instanceof TableView<?> && node.isVisible()) {
                node.setVisible(false);
                node.setManaged(false);
            }
        }
    }

    private void showChannelTableView() {
        hiddenAllTableViewInAdminBaseAP();
        if (channelTableView == null) {
            channelTableView = new TableView<>();
            channelTableView.setSelectionModel(null);
            channelTableView.setPlaceholder(new Label("Пока что здесь пусто..."));
            HBox.setHgrow(channelTableView, Priority.ALWAYS);
            centralHB.getChildren().addFirst(channelTableView);

            TableColumn<Channel, String> channelIdCol = new TableColumn<>();
            channelIdCol.setCellValueFactory(c ->
                    new SimpleStringProperty(String.format("%d", c.getValue().getChannelID())));
            channelIdCol.setText("№");
            channelTableView.getColumns().add(channelIdCol);
            channelIdCol.setCellFactory(setHighlightCellFactory());

            TableColumn<Channel, String> channelCountUserCol = new TableColumn<>();
            channelCountUserCol.setCellValueFactory(c ->
                    new SimpleStringProperty(String.format("%d", c.getValue().getChannelCountUser())));
            channelCountUserCol.setText("Число участников");
            channelTableView.getColumns().add(channelCountUserCol);
            channelCountUserCol.setCellFactory(setHighlightCellFactory());

            TableColumn<Channel, String> channelTypeNameCol = new TableColumn<>();
            channelTypeNameCol.setCellValueFactory(c ->
                    new SimpleStringProperty(c.getValue().getChannelType().getChannelTypeName()));
            channelTypeNameCol.setText("Тип канала");
            channelTableView.getColumns().add(channelTypeNameCol);
            channelTypeNameCol.setCellFactory(setHighlightCellFactory());

            TableColumn<Channel, String> channelNameCol = new TableColumn<>();
            channelNameCol.setCellValueFactory(c ->
                    new SimpleStringProperty(c.getValue().getChannel_name_unique()));
            channelNameCol.setText("Уникальное имя");
            channelTableView.getColumns().add(channelNameCol);
            channelNameCol.setCellFactory(setHighlightCellFactory());

            TableColumn<Channel, String> channelOwnerCol = new TableColumn<>();
            channelOwnerCol.setCellValueFactory(c ->
                    new SimpleStringProperty(c.getValue().getOwnerUser() != null
                            ? c.getValue().getOwnerUser().getNameUser() : ""));
            channelOwnerCol.setText("Владелец");
            channelTableView.getColumns().add(channelOwnerCol);
            channelOwnerCol.setCellFactory(setHighlightCellFactory());

            channelObservableList = FXCollections.observableList(adminChannelService.getAllRow());
            channelFilteredList = new FilteredList<>(channelObservableList, c -> true);
            channelSortedList = new SortedList<>(channelFilteredList, channelIdComPlus);

            channelTableView.setItems(channelSortedList);
        } else {
            channelObservableList.setAll(adminChannelService.getAllRow());
            channelTableView.setVisible(true);
            channelTableView.setManaged(true);
        }

        updateSortedList();
        updateCountVisibleRow();
    }

    private void showUserTableView() {
        hiddenAllTableViewInAdminBaseAP();
        if (userTableView == null) {
            userTableView = new TableView<>();
            userTableView.setSelectionModel(null);
            userTableView.setPlaceholder(new Label("Пока что здесь пусто..."));
            HBox.setHgrow(userTableView, Priority.ALWAYS);
            centralHB.getChildren().addFirst(userTableView);

            TableColumn<User, String> userIdCol = new TableColumn<>();
            userIdCol.setCellValueFactory(u ->
                    new SimpleStringProperty(String.format("%d", u.getValue().getIdUser())));
            userIdCol.setText("№");
            userTableView.getColumns().add(userIdCol);
            userIdCol.setCellFactory(setHighlightCellFactory());

            TableColumn<User, String> emailUserCol = new TableColumn<>();
            emailUserCol.setCellValueFactory(u ->
                    new SimpleStringProperty(u.getValue().getEmailUser()));
            emailUserCol.setText("e-mail");
            userTableView.getColumns().add(emailUserCol);
            emailUserCol.setCellFactory(setHighlightCellFactory());

            TableColumn<User, String> nameUserCol = new TableColumn<>();
            nameUserCol.setCellValueFactory(u ->
                    new SimpleStringProperty(u.getValue().getNameUser()));
            nameUserCol.setText("Имя пользователя");
            userTableView.getColumns().add(nameUserCol);
            nameUserCol.setCellFactory(setHighlightCellFactory());

            TableColumn<User, String> birthdayUserCol = new TableColumn<>();
            birthdayUserCol.setCellValueFactory(u ->
                    new SimpleStringProperty(u.getValue().getBirthdayUser()));
            birthdayUserCol.setText("Дата рождения");
            userTableView.getColumns().add(birthdayUserCol);
            birthdayUserCol.setCellFactory(setHighlightCellFactory());

            TableColumn<User, String> userStatusCol = new TableColumn<>();
            userStatusCol.setCellValueFactory(u ->
                    new SimpleStringProperty(u.getValue().getStatusUser()));
            userStatusCol.setText("Статус");
            userTableView.getColumns().add(userStatusCol);
            userStatusCol.setCellFactory(setHighlightCellFactory());

            userObservableList = FXCollections.observableList(adminUserService.getAllRow());
            userFilteredList = new FilteredList<>(userObservableList, u -> true);
            userSortedList = new SortedList<>(userFilteredList, userIdComPlus);

            userTableView.setItems(userSortedList);
        } else {
            userObservableList.setAll(adminUserService.getAllRow());
            userTableView.setVisible(true);
            userTableView.setManaged(true);
        }

        updateSortedList();
        updateCountVisibleRow();
    }

    private void showMessageTableView() {
        hiddenAllTableViewInAdminBaseAP();
        if (messageTableView == null) {
            messageTableView = new TableView<>();
            messageTableView.setSelectionModel(null);
            messageTableView.setPlaceholder(new Label("Пока что здесь пусто..."));
            HBox.setHgrow(messageTableView, Priority.ALWAYS);
            centralHB.getChildren().addFirst(messageTableView);

            TableColumn<Message, String> messageIdCol = new TableColumn<>();
            messageIdCol.setCellValueFactory(m ->
                    new SimpleStringProperty(String.format("%d", m.getValue().getMessageId())));
            messageIdCol.setText("№");
            messageTableView.getColumns().add(messageIdCol);
            messageIdCol.setCellFactory(setHighlightCellFactory());

            TableColumn<Message, String> messageDateTimeCol = new TableColumn<>();
            messageDateTimeCol.setCellValueFactory(m ->
                    new SimpleStringProperty(m.getValue().getMessageDatetime().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))));
            messageDateTimeCol.setText("Дата и время отправки");
            messageTableView.getColumns().add(messageDateTimeCol);
            messageDateTimeCol.setCellFactory(setHighlightCellFactory());

            TableColumn<Message, String> recipientCol = new TableColumn<>();
            recipientCol.setCellValueFactory(m ->
                    new SimpleStringProperty(m.getValue().getChannelUser().getUser().getNameUser()));
            recipientCol.setText("Отправитель");
            messageTableView.getColumns().add(recipientCol);
            recipientCol.setCellFactory(setHighlightCellFactory());

            TableColumn<Message, String> channelCol = new TableColumn<>();
            channelCol.setCellValueFactory(m ->
                    new SimpleStringProperty(m.getValue().getChannelUser().getChannel().getChannel_name_unique()));
            channelCol.setText("Название канала");
            messageTableView.getColumns().add(channelCol);
            channelCol.setCellFactory(setHighlightCellFactory());

            TableColumn<Message, String> modMessageCol = new TableColumn<>();
            modMessageCol.setCellValueFactory(m ->
                    new SimpleStringProperty(m.getValue().isModifiedMessage() ? "Изменено" : ""));
            modMessageCol.setText("Сообщение изменено");
            messageTableView.getColumns().add(modMessageCol);
            modMessageCol.setCellFactory(setHighlightCellFactory());

            TableColumn<Message, String> pinMessageCol = new TableColumn<>();
            pinMessageCol.setCellValueFactory(m ->
                    new SimpleStringProperty(m.getValue().getPinMessage() ? "Закреплено" : ""));
            pinMessageCol.setText("Сообщение закреплено");
            messageTableView.getColumns().add(pinMessageCol);
            pinMessageCol.setCellFactory(setHighlightCellFactory());

            TableColumn<Message, String> countMessageContentCol = new TableColumn<>();
            countMessageContentCol.setCellValueFactory(m ->
                    new SimpleStringProperty(String.format("%d", m.getValue().getMessageContent().size())));
            countMessageContentCol.setText("Число прикреплённых файлов");
            messageTableView.getColumns().add(countMessageContentCol);
            countMessageContentCol.setCellFactory(setHighlightCellFactory());

            messageObservableList = FXCollections.observableList(adminMessageService.getAllRow());
            messageFilteredList = new FilteredList<>(messageObservableList, u -> true);
            messageSortedList = new SortedList<>(messageFilteredList, messageIdComPlus);

            messageTableView.setItems(messageSortedList);
        } else {
            messageObservableList.setAll(adminMessageService.getAllRow());
            messageTableView.setVisible(true);
            messageTableView.setManaged(true);
        }

        updateSortedList();
        updateCountVisibleRow();
    }

    private void updateCountVisibleRow() {
        if (channelTableView != null && channelTableView.isVisible())
            countVisibleRow.setText("Показано записей: " + channelFilteredList.size());
        else if (userTableView != null && userTableView.isVisible())
            countVisibleRow.setText("Показано записей: " + userFilteredList.size());
        else if (messageTableView != null && messageTableView.isVisible())
            countVisibleRow.setText("Показано записей: " + messageFilteredList.size());
    }

    private <T> Callback<TableColumn<T, String>, TableCell<T, String>> setHighlightCellFactory() {
        return column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null || item.isEmpty()) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if (searchStr != null && !searchStr.isEmpty() && item.toLowerCase().contains(searchStr.toLowerCase()))
                        setStyle("-fx-background-color: yellow;");
                    else
                        setStyle("");
                }
            }
        };
    }

    private void updateSortedList() {
        if (channelTableView != null && channelTableView.isVisible())
            sortedListView.getItems().setAll("№", "Число участников", "Тип канала", "Уникальное имя", "Владелец");
        else if (userTableView != null && userTableView.isVisible())
            sortedListView.getItems().setAll("№", "e-mail", "Имя пользователя", "Дата рождения", "Статус");
        else if (messageTableView != null && messageTableView.isVisible())
            sortedListView.getItems().setAll("№", "Дата и время отправки", "Отправитель", "Название канала"
                    , "Сообщение изменено", "Сообщение закреплено", "Число прикреплённых файлов");

        Platform.runLater(() -> sortedListView.getSelectionModel().select(0));
    }

    private void sortData() {
        if (sortedListView.getSelectionModel().getSelectedItem() == null) return;

        if (sortedGroup.getSelectedToggle().equals(plusRB)) {
            if (channelTableView != null && channelTableView.isVisible()) {
                channelSortedList.setComparator(
                        switch (sortedListView.getSelectionModel().getSelectedItem()) {
                            case "Число участников" -> channelCountUserComPlus;
                            case "Тип канала" -> channelTypeComPlus;
                            case "Уникальное имя" -> channelNameComPlus;
                            case "Владелец" -> channelOwnerComPlus;
                            default -> channelIdComPlus;
                        });
            } else if (userTableView != null && userTableView.isVisible()) {
                userSortedList.setComparator(
                        switch (sortedListView.getSelectionModel().getSelectedItem()) {
                            case "e-mail" -> userEmailComPlus;
                            case "Имя пользователя" -> userNameComPlus;
                            case "Дата рождения" -> userDateComPlus;
                            case "Статус" -> userStatusComPlus;
                            default -> userIdComPlus;
                        });
            } else if (messageTableView != null && messageTableView.isVisible()) {
                messageSortedList.setComparator(
                        switch (sortedListView.getSelectionModel().getSelectedItem()) {
                            case "Дата и время отправки" -> messageDateTimeComPlus;
                            case "Отправитель" -> messageRecipientComPlus;
                            case "Название канала" -> messageChannelNameComPlus;
                            case "Сообщение изменено" -> messageModTimeComPlus;
                            case "Сообщение закреплено" -> messagePinTimeComPlus;
                            case "Число прикреплённых файлов" -> messageCountContentTimeComPlus;
                            default -> messageIdComPlus;
                        });
            }
        } else {
            if (channelTableView != null && channelTableView.isVisible()) {
                channelSortedList.setComparator(
                        switch (sortedListView.getSelectionModel().getSelectedItem()) {
                            case "Число участников" -> channelCountUserComMinus;
                            case "Тип канала" -> channelTypeComMinus;
                            case "Уникальное имя" -> channelNameComMinus;
                            case "Владелец" -> channelOwnerComMinus;
                            default -> channelIdComMinus;
                        });
            } else if (userTableView != null && userTableView.isVisible()) {
                userSortedList.setComparator(
                        switch (sortedListView.getSelectionModel().getSelectedItem()) {
                            case "e-mail" -> userEmailComMinus;
                            case "Имя пользователя" -> userNameComMinus;
                            case "Дата рождения" -> userDateComMinus;
                            case "Статус" -> userStatusComMinus;
                            default -> userIdComMinus;
                        });
            } else if (messageTableView != null && messageTableView.isVisible()) {
                messageSortedList.setComparator(
                        switch (sortedListView.getSelectionModel().getSelectedItem()) {
                            case "Дата и время отправки" -> messageDateTimeComMinus;
                            case "Отправитель" -> messageRecipientComMinus;
                            case "Название канала" -> messageChannelNameComMinus;
                            case "Сообщение изменено" -> messageModTimeComMinus;
                            case "Сообщение закреплено" -> messagePinTimeComMinus;
                            case "Число прикреплённых файлов" -> messageCountContentTimeComMinus;
                            default -> messageIdComMinus;
                        });
            }
        }
    }

    private void updateFilteredControl() {
        if (sortedListView.getSelectionModel().getSelectedItem() == null) return;

        searchHB.getChildren().removeIf(node -> node.getUserData() == null);

        if (channelTableView != null && channelTableView.isVisible()) {
            switch (sortedListView.getSelectionModel().getSelectedItem()) {
                case "Число участников" -> {
                    if (numberHB == null)
                        createNumberHB();

                    Platform.runLater(() -> {
                        searchHB.getChildren().add(2, numberHB);
                    });
                    filterDataBtn.setOnAction(e -> {
                        String fromStr = intFrom.getText();
                        String toStr = intTo.getText();

                        if ((fromStr == null || fromStr.isEmpty()) && (toStr == null || toStr.isEmpty())) return;

                        if (toStr == null || toStr.isEmpty())
                            channelFilteredList.setPredicate(c -> c.getChannelCountUser() >= Long.parseLong(fromStr));
                        else if (fromStr == null || fromStr.isEmpty())
                            channelFilteredList.setPredicate(c -> c.getChannelCountUser() <= Long.parseLong(toStr));
                        else channelFilteredList.setPredicate(c ->
                                    c.getChannelCountUser() >= Long.parseLong(fromStr) &&
                                            c.getChannelCountUser() <= Long.parseLong(toStr));

                        updateCountVisibleRow();
                    });
                }
                case "Тип канала" -> {
                    if (comboBox == null)
                        createStringComboBox();

                    comboBox.getItems().setAll(new ChannelTypeService(
                            HibernateAdminSessionFactory.getSessionFactory()).getAllRow().stream()
                            .map(cT -> cT.toString())
                            .distinct().toList());

                    Platform.runLater(() -> {
                        searchHB.getChildren().add(2, comboBox);
                    });
                    filterDataBtn.setOnAction(e -> {
                        if (comboBox.getValue() == null) return;
                        channelFilteredList.setPredicate(c -> c.getChannelType().getChannelTypeName()
                                .equals(comboBox.getValue()));

                        updateCountVisibleRow();
                    });
                }
                case "Уникальное имя" -> {
                    if (comboBox == null)
                        createStringComboBox();

                    comboBox.getItems().setAll(adminChannelService.getAllRow().stream()
                            .map(c -> c.getChannel_name_unique())
                            .distinct().toList());

                    Platform.runLater(() -> {
                        searchHB.getChildren().add(2, comboBox);
                    });
                    filterDataBtn.setOnAction(e -> {
                        if (comboBox.getValue() == null) return;

                        channelFilteredList.setPredicate(c -> c.getChannel_name_unique().equals(comboBox.getValue()));

                        updateCountVisibleRow();
                    });
                }
                case "Владелец" -> {
                    if (comboBox == null)
                        createStringComboBox();

                    comboBox.getItems().setAll(adminChannelService.getAllRow().stream()
                            .filter(c -> c.getOwnerUser() != null)
                            .map(c -> c.getOwnerUser().getNameUser())
                            .distinct().toList());

                    Platform.runLater(() -> {
                        searchHB.getChildren().add(2, comboBox);
                    });
                    filterDataBtn.setOnAction(e -> {
                        if (comboBox.getValue() == null) return;

                        channelFilteredList.setPredicate(c -> c.getOwnerUser() != null &&
                                c.getOwnerUser().getNameUser().equals(comboBox.getValue()));

                        updateCountVisibleRow();
                    });
                }
                default -> {
                    if (numberHB == null)
                        createNumberHB();

                    Platform.runLater(() -> {
                        searchHB.getChildren().add(2, numberHB);
                    });
                    filterDataBtn.setOnAction(e -> {
                        String fromStr = intFrom.getText();
                        String toStr = intTo.getText();

                        if ((fromStr == null || fromStr.isEmpty()) && (toStr == null || toStr.isEmpty())) return;

                        if (toStr == null || toStr.isEmpty())
                            channelFilteredList.setPredicate(c -> c.getChannelID() >= Long.parseLong(fromStr));
                        else if (fromStr == null || fromStr.isEmpty())
                            channelFilteredList.setPredicate(c -> c.getChannelID() <= Long.parseLong(toStr));
                        else channelFilteredList.setPredicate(c ->
                                    c.getChannelID() >= Long.parseLong(fromStr) &&
                                            c.getChannelID() <= Long.parseLong(toStr));

                        updateCountVisibleRow();
                    });
                }
            }
        } else if (userTableView != null && userTableView.isVisible()) {
            switch (sortedListView.getSelectionModel().getSelectedItem()) {
                case "e-mail" -> {
                    if (comboBox == null)
                        createStringComboBox();

                    comboBox.getItems().setAll(adminUserService.getAllRow().stream()
                            .map(c -> c.getEmailUser())
                            .distinct().toList());

                    Platform.runLater(() -> {
                        searchHB.getChildren().add(2, comboBox);
                    });
                    filterDataBtn.setOnAction(e -> {
                        if (comboBox.getValue() == null) return;

                        userFilteredList.setPredicate(u -> u.getEmailUser().equals(comboBox.getValue()));

                        updateCountVisibleRow();
                    });
                }
                case "Имя пользователя" -> {
                    if (comboBox == null)
                        createStringComboBox();

                    comboBox.getItems().setAll(adminUserService.getAllRow().stream()
                            .map(u -> u.getNameUser())
                            .distinct().toList());

                    Platform.runLater(() -> {
                        searchHB.getChildren().add(2, comboBox);
                    });
                    filterDataBtn.setOnAction(e -> {
                        if (comboBox.getValue() == null) return;

                        userFilteredList.setPredicate(u -> u.getNameUser().equals(comboBox.getValue()));

                        updateCountVisibleRow();
                    });
                }
                case "Дата рождения" -> {
                    if (dateHB == null)
                        createDateHB();

                    Platform.runLater(() -> {
                        searchHB.getChildren().add(2, dateHB);
                    });
                    filterDataBtn.setOnAction(e -> {
                        LocalDate fromDate = dateFrom.getValue();
                        LocalDate toDate = dateTo.getValue();

                        if (fromDate == null && toDate == null) return;

                        if (toDate == null)
                            userFilteredList.setPredicate(u -> LocalDate.parse(u.getBirthdayUser()
                                            , DateTimeFormatter.ofPattern("dd.MM.yyyy"))
                                    .isAfter(fromDate.minusDays(1)));
                        else if (fromDate == null)
                            userFilteredList.setPredicate(u -> LocalDate.parse(u.getBirthdayUser()
                                            , DateTimeFormatter.ofPattern("dd.MM.yyyy"))
                                    .isBefore(toDate.plusDays(1)));
                        else userFilteredList.setPredicate(u ->
                                    LocalDate.parse(u.getBirthdayUser(), DateTimeFormatter.ofPattern("dd.MM.yyyy"))
                                            .isAfter(fromDate.minusDays(1)) &&
                                            LocalDate.parse(u.getBirthdayUser(), DateTimeFormatter.ofPattern("dd.MM.yyyy"))
                                                    .isBefore(toDate.plusDays(1)));

                        updateCountVisibleRow();
                    });
                }
                case "Статус" -> {
                    if (comboBox == null)
                        createStringComboBox();

                    comboBox.getItems().setAll(adminUserService.getAllRow().stream()
                            .map(u -> u.getStatusUser())
                            .distinct().toList());

                    Platform.runLater(() -> {
                        searchHB.getChildren().add(2, comboBox);
                    });
                    filterDataBtn.setOnAction(e -> {
                        if (comboBox.getValue() == null) return;

                        userFilteredList.setPredicate(u -> u.getStatusUser().equals(comboBox.getValue()));

                        updateCountVisibleRow();
                    });
                }
                default -> {
                    if (numberHB == null)
                        createNumberHB();

                    Platform.runLater(() -> {
                        searchHB.getChildren().add(2, numberHB);
                    });
                    filterDataBtn.setOnAction(e -> {
                        String fromStr = intFrom.getText();
                        String toStr = intTo.getText();

                        if ((fromStr == null || fromStr.isEmpty()) && (toStr == null || toStr.isEmpty())) return;

                        if (toStr == null || toStr.isEmpty())
                            userFilteredList.setPredicate(u -> u.getIdUser() >= Long.parseLong(fromStr));
                        else if (fromStr == null || fromStr.isEmpty())
                            userFilteredList.setPredicate(u -> u.getIdUser() <= Long.parseLong(toStr));
                        else userFilteredList.setPredicate(u ->
                                    u.getIdUser() >= Long.parseLong(fromStr) &&
                                            u.getIdUser() <= Long.parseLong(toStr));

                        updateCountVisibleRow();
                    });
                }
            }
        } else if (messageTableView != null && messageTableView.isVisible()) {
            switch (sortedListView.getSelectionModel().getSelectedItem()) {
                case "Дата и время отправки" -> {
                    if (dateHB == null)
                        createDateHB();
                    Platform.runLater(() -> {
                        searchHB.getChildren().add(2, dateHB);
                    });
                    filterDataBtn.setOnAction(e -> {
                        LocalDate fromDate = dateFrom.getValue();
                        LocalDate toDate = dateTo.getValue();

                        if (fromDate == null && toDate == null) return;

                        if (toDate == null)
                            messageFilteredList.setPredicate(m -> m.getMessageDatetime().toLocalDate()
                                    .isAfter(fromDate.minusDays(1)));
                        else if (fromDate == null)
                            messageFilteredList.setPredicate(m -> m.getMessageDatetime().toLocalDate()
                                    .isBefore(toDate.plusDays(1)));
                        else messageFilteredList.setPredicate(m ->
                                    m.getMessageDatetime().toLocalDate().isAfter(fromDate.minusDays(1)) &&
                                            m.getMessageDatetime().toLocalDate().isBefore(toDate.plusDays(1)));

                        updateCountVisibleRow();
                    });
                }
                case "Отправитель" -> {
                    if (comboBox == null)
                        createStringComboBox();

                    comboBox.getItems().setAll(adminMessageService.getAllRow().stream()
                            .map(m -> m.getChannelUser().getUser().getNameUser())
                            .distinct().toList());

                    Platform.runLater(() -> {
                        searchHB.getChildren().add(2, comboBox);
                    });
                    filterDataBtn.setOnAction(e -> {
                        if (comboBox.getValue() == null) return;

                        messageFilteredList.setPredicate(m -> m.getChannelUser().getUser().getNameUser()
                                .equals(comboBox.getValue()));

                        updateCountVisibleRow();
                    });
                }
                case "Название канала" -> {
                    if (comboBox == null)
                        createStringComboBox();

                    comboBox.getItems().setAll(adminMessageService.getAllRow().stream()
                            .map(m -> m.getChannelUser().getChannel().getChannel_name_unique())
                            .distinct().toList());

                    Platform.runLater(() -> {
                        searchHB.getChildren().add(2, comboBox);
                    });
                    filterDataBtn.setOnAction(e -> {
                        if (comboBox.getValue() == null) return;

                        messageFilteredList.setPredicate(m -> m.getChannelUser().getChannel().getChannel_name_unique()
                                .equals(comboBox.getValue()));

                        updateCountVisibleRow();
                    });
                }
                case "Сообщение изменено" -> {
                    if (comboBox == null)
                        createStringComboBox();

                    comboBox.getItems().setAll("Изменено", "Не менялось");

                    Platform.runLater(() -> {
                        searchHB.getChildren().add(2, comboBox);
                    });
                    filterDataBtn.setOnAction(e -> {
                        if (comboBox.getValue() == null) return;

                        messageFilteredList.setPredicate(m ->
                                m.isModifiedMessage().equals(comboBox.getValue().equals("Изменено")));

                        updateCountVisibleRow();
                    });
                }
                case "Сообщение закреплено" -> {
                    if (comboBox == null)
                        createStringComboBox();

                    comboBox.getItems().setAll("Закреплено", "Не закреплено");

                    Platform.runLater(() -> {
                        searchHB.getChildren().add(2, comboBox);
                    });
                    filterDataBtn.setOnAction(e -> {
                        if (comboBox.getValue() == null) return;

                        messageFilteredList.setPredicate(m ->
                                m.getPinMessage().equals(comboBox.getValue().equals("Закреплено")));

                        updateCountVisibleRow();
                    });
                }
                case "Число прикреплённых файлов" -> {
                    if (numberHB == null)
                        createNumberHB();

                    Platform.runLater(() -> {
                        searchHB.getChildren().add(2, numberHB);
                    });
                    filterDataBtn.setOnAction(e -> {
                        String fromStr = intFrom.getText();
                        String toStr = intTo.getText();

                        if ((fromStr == null || fromStr.isEmpty()) && (toStr == null || toStr.isEmpty())) return;

                        if (toStr == null || toStr.isEmpty())
                            messageFilteredList.setPredicate(m -> m.getMessageContent().size() >= Long.parseLong(fromStr));
                        else if (fromStr == null || fromStr.isEmpty())
                            messageFilteredList.setPredicate(m -> m.getMessageContent().size() <= Long.parseLong(toStr));
                        else messageFilteredList.setPredicate(m ->
                                    m.getMessageContent().size() >= Long.parseLong(fromStr) &&
                                            m.getMessageContent().size() <= Long.parseLong(toStr));

                        updateCountVisibleRow();
                    });
                }
                default -> {
                    if (numberHB == null)
                        createNumberHB();

                    Platform.runLater(() -> {
                        searchHB.getChildren().add(2, numberHB);
                    });
                    filterDataBtn.setOnAction(e -> {
                        String fromStr = intFrom.getText();
                        String toStr = intTo.getText();

                        if ((fromStr == null || fromStr.isEmpty()) && (toStr == null || toStr.isEmpty())) return;

                        if (toStr == null || toStr.isEmpty())
                            messageFilteredList.setPredicate(m -> m.getMessageId() >= Long.parseLong(fromStr));
                        else if (fromStr == null || fromStr.isEmpty())
                            messageFilteredList.setPredicate(m -> m.getMessageId() <= Long.parseLong(toStr));
                        else messageFilteredList.setPredicate(m ->
                                    m.getMessageId() >= Long.parseLong(fromStr) &&
                                            m.getMessageId() <= Long.parseLong(toStr));

                        updateCountVisibleRow();
                    });
                }
            }
        }
    }

    private void createNumberHB() {
        numberHB = new HBox(5);
        numberHB.setAlignment(Pos.CENTER_LEFT);

        intFrom = new TextField();
        intFrom.setPromptText("от");
        intFrom.setTextFormatter(new TextFormatter<>(change -> {
            String input = change.getText();
            if (input.isEmpty())
                return change;

            if (input.matches("[0-9]*")) {
                try {
                    Long.parseLong(change.getControlNewText());
                    return change;
                } catch (NumberFormatException e) {
                    return null;
                }

            }
            return null;
        }));
        numberHB.getChildren().add(intFrom);

        intTo = new TextField();
        intTo.setPromptText("до");
        intTo.setTextFormatter(new TextFormatter<>(change -> {
            String input = change.getText();
            if (input.isEmpty())
                return change;

            if (input.matches("[0-9]*")) {
                try {
                    Long.parseLong(change.getControlNewText());
                    return change;
                } catch (NumberFormatException e) {
                    return null;
                }
            }
            return null;
        }));
        numberHB.getChildren().add(intTo);
    }

    private void createStringComboBox() {
        comboBox = new ComboBox<>();
        comboBox.setPromptText("Выберите значение");
    }

    private void createDateHB() {
        dateHB = new HBox(5);
        dateHB.setAlignment(Pos.CENTER_LEFT);

        dateFrom = new DatePicker();
        dateFrom.setPromptText("от");
        dateHB.getChildren().add(dateFrom);

        dateTo = new DatePicker();
        dateTo.setPromptText("до");
        dateHB.getChildren().add(dateTo);
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.initOwner(adminStage);
        alert.showAndWait();
    }
}
