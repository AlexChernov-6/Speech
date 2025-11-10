package com.example.speech;

import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.time.LocalDate;
import java.util.*;
import java.util.regex.Pattern;

public class UsefulClass {
    //Создаются переменные типа String, которые в дальнейшем будут выступать в роли регулярного выражения
    private static final String EMAIL_NAME = "[A-Za-z0-9]+(?:[\\.-_]?[A-Za-z0-9]+)*";
    private static final String DOMAIN_NAME = "yandex|mail|gmail";
    private static final String END_EMAIL = "\\.[a-z]{2,3}";

    //Создаются переменные типа Pattern, которые в дальнейшем будут являться главной частью валидации значения
    private static final Pattern EMAIL_NAME_PATTERN = Pattern.compile("^" + EMAIL_NAME + "$");
    private static final Pattern DOMAIN_NAME_PATTERN = Pattern.compile("^(" + DOMAIN_NAME + ")$");
    private static final Pattern END_EMAIL_PATTERN = Pattern.compile("^" + END_EMAIL + "$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^" + EMAIL_NAME + "@(" + DOMAIN_NAME + ")" + END_EMAIL + "$");

    private static final Pattern PASSWORD_MIN_NUM_PATTERN = Pattern.compile("^.*[0-9].*$");
    private static final Pattern PASSWORD_MIN_LETTER_PATTERN = Pattern.compile(".*[a-zA-Z].*$");

    //Статичный метод, в качестве возвращаемого типа будет тип String(сообщение о валидации)
    //В качестве аргумента может принимать любой объект, который наследуется от TextInputControl(TextField, PasswordField и т.д.)
    public static String validationField(TextInputControl inputField) {
        String text = inputField.getText().trim();

        // Определяем логику только по ID
        if (inputField.getId() != null) {
            switch (inputField.getId()) {
                case "mailTF": return validateEmail(text);
                case "passwordF": return validatePassword(text);
                //case "usernameTF": return validateUsername(text);
            }
        }
        return null;
    }

    //Создаём метод для валидации электронного адреса, в качестве возвращаемого типа будет String, в качестве аргумента
    //Принимает String-значение в TextField
    private static String validateEmail(String email) {
        //Проверяем что значение не пустое
        if (email.isEmpty()) {
            return "Email не может быть пустым";
        }

        //Быстрая проверка полного формата, если ответ удовлетворяет полному регулярному выражению
        //Прекращаем выполнение метод без ошибок
        if (EMAIL_PATTERN.matcher(email).matches()) {
            return null;
        }

        //Проверка на символ @
        if (!email.contains("@")) {
            return "Адрес электронной почты должен содержать символ @";
        }

        //Проверяем что мы имеем две части, как слева от @, так и справа
        String[] parts = email.split("@", 2);
        if (parts.length != 2 || parts[0].isEmpty() || parts[1].isEmpty()) {
            return "Некорректный формат email";
        }

        //Создадим две переменные, для каждой из частей
        String localPart = parts[0];
        String domainPart = parts[1];

        //Проверка локальной части
        //Проверяем первый и последний символ в части имени email
        if (!EMAIL_NAME_PATTERN.matcher(localPart).matches()) {
            if (!Character.isLetterOrDigit(localPart.charAt(0))) {
                return "Email не должен начинаться со специального символа";
            }
            if (!Character.isLetterOrDigit(localPart.charAt(localPart.length() - 1))) {
                return "Email не должен заканчиваться специальным символом";
            }
            return "Имя email может содержать только английские буквы, цифры и не более одного символа (. - _) подряд";
        }

        //Проверка доменной части
        //Доменная часть обязательно должна содержать символ .
        if (!domainPart.contains(".")) {
            return "Домен должен содержать точку";
        }

        //Делим доменную часть левую(до символа .) и правую после неё, где указывается доменная зона(ru, com)
        String domain = domainPart.substring(0, domainPart.indexOf('.'));
        String zone = domainPart.substring(domainPart.indexOf('.') + 1);

        //Проверяем что указан допустимый домен
        if (!DOMAIN_NAME_PATTERN.matcher(domain).matches()) {
            return "Допустимые домены: yandex, mail, gmail";
        }

        //Проверяем что доменная зона корректно указана
        if (!END_EMAIL_PATTERN.matcher("." + zone).matches()) {
            return "Доменная зона должна состоять из 2-3 маленьких латинских букв";
        }
        //Если не прошло быструю основную проверку, то значит есть неизвестная ошибка, которую я не обработал
        return "Неизвестная ошибка в формате email";
    }

    //Создаём метод для валидации пароля, в качестве возвращаемого типа будет String, в качестве аргумента
    //Принимает String-значение из PasswordField
    private static String validatePassword(String password) {
        //Пароль не должен быть пустым
        if (password.isEmpty()) {
            return "Пароль не может быть пустым";
        }

        //Пароль не может быть меньше 8 и больше 50-ти символов
        if (password.length() <= 8 || password.length() >= 50) {
            return "Пароль должен быть не менее 8 и не более 50 символов";
        }

        //Пароль должен содержать хотя бы одну цифру
        if (!PASSWORD_MIN_NUM_PATTERN.matcher(password).matches()) {
            return "Пароль должен содержать хотя бы одну цифру";
        }

        //Пароль должен содержать хотя бы одну букву
        if (!PASSWORD_MIN_LETTER_PATTERN.matcher(password).matches()) {
            return "Пароль должен содержать хотя бы одну английскую букву";
        }

        //Если все ошибки валидации прошли мимо, выводим null
        return null;
    }

    //Метод, который следит за изменениями состояний окна, а конкретно за FullScreen
    public static void setupFullScreenListener(Stage stage, Pane rootPane) {
        //Добавляем обработчика, то что следит за изменениями для fullScreenProperty
        //Вместо создания отдельного класса, который реализовывает интерфейс InvalidationListener
        //Используем лямбда-функцию, в которой переопределим необходимый метод invalidated
        stage.fullScreenProperty().addListener(observable -> {
            //Если у нас окно открыто в полный экран-то есть имеет state FullScreen, то мы убираем отступы у корневого
            //Pane в обратном случае, мы возвращаем ему отступы для его содержимого
            if (stage.isFullScreen()) {
                rootPane.setPadding(new Insets(0));
            } else {
                rootPane.setPadding(new Insets(13));
            }
        });

        // Устанавливаем начальное значение
        if (stage.isFullScreen()) {
            rootPane.setPadding(new Insets(0));
        } else {
            rootPane.setPadding(new Insets(13));
        }
    }

    //Сам метод подгружает файл, откуда будут использоваться стили для контроллеров, переданных в качестве аргумента
    //Метод в качестве аргумента может принимать любое количество экземпляров-наследников класса Control.
    public static void setStyleSheets(Control... controls) {
        String cssUrl = UsefulClass.class.getResource("/com/example/speech/styles.css").toExternalForm();
        for (Control control : controls) {
            control.getStylesheets().add(cssUrl);
        }
    }

    //Метод, в котором проверяется валидация полей ввода текста и меняются стили в зависимости от корректности значения
    public static void updateStyleValidation(Map<TextInputControl, Label> map) {

        //Пробегаемся по строкам таблице, которую передали нам в качестве аргумента
        for (Map.Entry<TextInputControl, Label> entry : map.entrySet()) {
            //Подгружаем файл, откуда можно взять стили css
            setStyleSheets(entry.getKey(), entry.getValue());
            //Создаём временные переменный для упрощения читаемости кода
            TextInputControl inputControl = entry.getKey();
            Label label = entry.getValue();

            // Получаем оригинальный текст из userData Label
            //userData-некое хранилище объекта-наследника класса Node, может хранить в себе любой объект.
            //Получаем объект из кармана, если в кармане пусто, то записываем туда текущий текс Label
            String originalText = (String) label.getUserData();
            if (originalText == null) {
                // Сохраняем оригинальный текст при первом вызове
                originalText = label.getText();
                label.setUserData(originalText);
            }

            //Проверяем валидность введённых пользователем значений в наследника TextInputControl
            String validationResult = validationField(inputControl);


            if (validationResult != null) {
                // Валидация не пройдена - показываем ошибку, меняем стили, делаем их в тёмно-красных тонах
                label.setText(validationResult);
                label.setStyle("-fx-text-fill: rgba(115,0,0);");
                inputControl.setStyle("-fx-border-color: rgba(115,0,0);");
            } else {
                // Валидация пройдена - восстанавливаем оригинальный текст и стиль
                label.setText(originalText);
                // Очищаем inline стили
                label.setStyle("");
                inputControl.setStyle("");
            }
        }
    }

    //Метод, который подгружает fxml, и передаёт окно в контроллер, если это нужно(указ. при помощи интерфейса)
    public static <T> Parent loadFXML(Stage stage, String fxmlPath, Class<T> controllerClass) throws IOException {
        //Загрузчик интерфейса, который собирает внешний вид окна из указанного файла
        FXMLLoader fxmlLoader = new FXMLLoader(controllerClass.getResource(fxmlPath));
        //Создаёт интерфейс из FXMLLoader, является корневым элементом интерфейса, содержащий все визуальные компоненты
        Parent parent = fxmlLoader.load();

        //Получаем объект контроллера, который управляет действиями в fxml, то есть различные обработчики событий
        //Данный метод автоматически связывает Java-код контроллера с FXML-разметкой
        T controller = fxmlLoader.getController();

        //Если класс должен получать окно, то мы его передаём, если нет, то просто подгружаем fxml
        if (controller instanceof Window)
            //Передаём в контроллер экземпляр окна, таким образом контроллер знает в каком окне он работает
            ((Window) controller).setStage(stage);
        return parent;
    }

    //Метод, который в качестве аргумента принимает множество объектов класса Label.
    //Метод находит последний элемент и закрашивает его в красный цвет(нужно где есть *,
    //которая показывает обязательное поле)
    public static void setRedEndChar(Label... labels) {
        for(Label label : labels) {
            String text = label.getText();
            if (text == null || text.isEmpty()) return;

            int labelLength = text.length();
            //Вычисляем процент всех символом без * относительно всего текста в Label
            double percent = ((double) (labelLength - 1) / labelLength) * 100;
            //Рисуем градиент, который весь текст закрасит в один цвет, а последний символ в красный
            label.setStyle("-fx-text-fill: linear-gradient(from 0% 0% to 100% 0%, " +
                    //Первый цвет у нас будет от 0% до того значения, которое мы вычислили,
                    //А второй цвет(красный) будет от вычисленного процента до 100% текста
                    "#718096 0%, #718096 " + percent + "%, red " + percent + "%, red 100%);");
        }
    }

    //Метод, который будет устанавливать значения в ComboBox, в качестве аргумента принимает множество ComboBox
    //С неопределённым типом данных, обрабатывается проверка какие значение раздавать по id
    public static void setValuesComboBox(ComboBox<?>... comboBoxes) {
        for(ComboBox<?> comboBox : comboBoxes) {
            String comboId = comboBox.getId();

            switch (comboId) {
                case "dayBirthdayCB":
                    for (int i = 1; i <= 31; i++) {
                        //Максимум в месяце 31 день
                        ((ComboBox<Integer>) comboBox).getItems().add(i);
                    }
                    break;
                case "monthBirthdayCB":
                    //Список месяцев
                    String[] months = {"Январь", "Февраль", "Март", "Апрель", "Май", "Июнь",
                            "Июль", "Август", "Сентябрь", "Октябрь", "Ноябрь", "Декабрь"};
                    ((ComboBox<String>) comboBox).getItems().addAll(months);
                    break;

                case "yearBirthdayCB":
                    //Берём последние 100 лет цикл будет не инкрементировать, а дикрементировать
                    int currentYear = LocalDate.now().getYear();
                    for (int i = currentYear; i >= currentYear - 100; i--) {
                        ((ComboBox<Integer>) comboBox).getItems().add(i);
                    }
                    break;
            }
        }
    }

    //Метод для открытия веб страницы по переданному адресу
    public static void openWebPage(String urlAddress) {
        try {
            //Создаётся экземпляр рабочего стола
            Desktop desktop = Desktop.getDesktop();
            //Проверяем, поддерживает ли ОС пользователя открытие браузера
            if (desktop.isSupported(Desktop.Action.BROWSE)) {
                //Открывает URI в браузере по переданному адресу
                desktop.browse(new URI(urlAddress));
            }
        } catch (Exception e) {
            e.printStackTrace();
            //showError("Не удалось открыть браузер");
        }
    }
}
