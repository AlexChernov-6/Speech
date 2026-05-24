package com.example.speech.util;

import com.example.speech.model.User;
import com.example.speech.service.UserService;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputControl;
import javafx.scene.layout.HBox;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static com.example.speech.util.HelpfulStylingClass.setStyleSheets;

public class HelpfulValidationClass {
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

    private static final Pattern USER_NAME = Pattern.compile("^[A-Za-z]+[\\.-_]?[A-Za-z0-9]+$");

    private static final Pattern CHANNEL_NAME = Pattern.compile("^[A-Za-z]+[\\.-_]?[A-Za-z0-9]+$");

    private static final Pattern CONFIRM_CODE = Pattern.compile("^[0-9]$");

    private static UserService userService = new UserService();

    protected static final List<String> MONTHS = List.of("Январь", "Февраль", "Март", "Апрель", "Май", "Июнь",
            "Июль", "Август", "Сентябрь", "Октябрь", "Ноябрь", "Декабрь");

    public enum ValidationContext {
        REGISTRATION,
        ENTRANCE
    }

    //Статичный метод, в качестве возвращаемого типа будет тип String(сообщение о валидации)
    //В качестве аргумента может принимать любой объект, который наследуется от TextInputControl(TextField, PasswordField и т.д.)
    public static String validationField(TextInputControl inputField) {
        String text = inputField.getText().trim();

        // Определяем логику только по ID
        if (inputField.getId() != null) {
            switch (inputField.getId()) {
                case "mailTF": return validateEmail(text);
                case "passwordF": return validatePassword(text);
                case "visibleNameTF": return validateVisibleName(text);
                case "userNameTF": return validateUserName(text);
            }
        }
        return null;
    }

    //Создаём метод для валидации электронного адреса, в качестве возвращаемого типа будет String, в качестве аргумента
    //Принимает String-значение в TextField
    private static String validateEmail(String email) {
        //Проверяем что значение не пустое
        if (email.isEmpty())
            return "Email не может быть пустым";

        //Быстрая проверка полного формата, если ответ удовлетворяет полному регулярному выражению
        //Прекращаем выполнение метод без ошибок
        if (EMAIL_PATTERN.matcher(email).matches()) {
            ValidationContext context = determineValidationContext();
            User user = userService.getUserByEmail(email);

            if (context == ValidationContext.REGISTRATION)
                if (user != null)
                    return "Пользователь с таким Email уже существует";

            if (context == ValidationContext.ENTRANCE)
                if (user == null)
                    return "Пользователь с таким Email не существует";
        }

        //Проверка на символ @
        if (!email.contains("@"))
            return "Адрес электронной почты должен содержать символ @";

        //Проверка длинны электронного адреса
        if (email.length() > 50)
            return "Адрес электронной почты не может быть длиннее 50-ти символов.";

        //Проверяем что мы имеем две части, как слева от @, так и справа
        String[] parts = email.split("@", 2);
        if (parts.length != 2 || parts[0].isEmpty() || parts[1].isEmpty())
            return "Некорректный формат email";

        //Создадим две переменные, для каждой из частей
        String localPart = parts[0];
        String domainPart = parts[1];

        //Проверка локальной части
        //Проверяем первый и последний символ в части имени email
        if (!EMAIL_NAME_PATTERN.matcher(localPart).matches()) {
            if (!Character.isLetterOrDigit(localPart.charAt(0)))
                return "Email не должен начинаться со специального символа";

            if (!Character.isLetterOrDigit(localPart.charAt(localPart.length() - 1)))
                return "Email не должен заканчиваться специальным символом";

            return "Email может содержать только англ. буквы, цифры и 1 спец. символ";
        }

        //Проверка доменной части
        //Доменная часть обязательно должна содержать символ .
        if (!domainPart.contains("."))
            return "Домен должен содержать точку";

        //Делим доменную часть левую(до символа .) и правую после неё, где указывается доменная зона(ru, com)
        String domain = domainPart.substring(0, domainPart.indexOf('.'));
        String zone = domainPart.substring(domainPart.indexOf('.') + 1);

        //Проверяем что указан допустимый домен
        if (!DOMAIN_NAME_PATTERN.matcher(domain).matches())
            return "Допустимые домены: yandex, mail, gmail";

        //Проверяем что доменная зона корректно указана
        if (!END_EMAIL_PATTERN.matcher("." + zone).matches())
            return "Доменная зона должна состоять из 2-3 маленьких латинских букв";

        return null;
    }

    private static ValidationContext determineValidationContext() {
        //Создаём массив из стека вызовов цепочки различных методов(при нажатии на кнопку вызывается один метод,
        //Внутри этого метода вызывается другой и так далее, мы собираем всю цепочку методов, благодаря StackTraceElement
        //И методу getClassName мы можем узнать класс, откуда вызывался метод, что необходимо нашей логики проекта.
        //Thread - отдельный поток выполнения программы, мы берём текущий и получаем из него стек вызовов методов
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();

        for (int i = 3; i < stackTrace.length; i++) {
            String className = stackTrace[i].getClassName().toLowerCase();

            if (className.contains("registrationcontroller"))
                return ValidationContext.REGISTRATION;
            else if (className.contains("entrancecontroller"))
                return ValidationContext.ENTRANCE;
        }

        // По умолчанию считаем что это регистрация (более строгая проверка)
        return ValidationContext.REGISTRATION;
    }

    //Создаём метод для валидации пароля, в качестве возвращаемого типа будет String, в качестве аргумента
    //Принимает String-значение из PasswordField
    private static String validatePassword(String password) {
        //Пароль не должен быть пустым
        if (password.isEmpty())
            return "Пароль не может быть пустым";

        //Пароль не может быть меньше 8 и больше 50-ти символов
        if (password.length() < 8 || password.length() > 50)
            return "Пароль должен быть не менее 8 и не более 50 символов";

        //Пароль должен содержать хотя бы одну цифру
        if (!PASSWORD_MIN_NUM_PATTERN.matcher(password).matches())
            return "Пароль должен содержать хотя бы одну цифру";

        //Пароль должен содержать хотя бы одну букву
        if (!PASSWORD_MIN_LETTER_PATTERN.matcher(password).matches())
            return "Пароль должен содержать хотя бы одну английскую букву";

        //Если все ошибки валидации прошли мимо, выводим null
        return null;
    }

    private static String validateVisibleName(String visibleName) {
        if (!visibleName.isEmpty()) {
            if (visibleName.length() < 4)
                return "Отображаемое имя не должно быть короче 4-ых символов";

            if (visibleName.length() > 20)
                return "Отображаемое имя не должно быть длиннее 20-ти символов";
        }
        return null;
    }

    private static String validateUserName(String userName) {
        if (userName.isEmpty())
            return "Имя пользователя не может быть пустым";

        if (userService.getUserByUserName(userName) != null)
            return "Пользователь с таким именем уже существует";

        if (userName.length() < 4)
            return "Имя пользователя не должно быть короче 4-ых символов";

        if (userName.length() > 20)
            return "Имя пользователя не должно быть длиннее 20-ти символов";

        if (!USER_NAME.matcher(userName).matches()) {
            if (!Character.isLetterOrDigit(userName.charAt(0)))
                return "Имя пользователя не должно начинаться со специального символа";

            if (!Character.isLetterOrDigit(userName.charAt(userName.length() - 1)))
                return "Имя пользователя не должно заканчиваться специальным символом";

            return "Имя пользователя может содержать только англ. буквы, цифры и 1 спец. символ";
        }

        return null;
    }

    public static String validateUserNameShort(String userName) {
        if (userName.isEmpty())
            return "Имя пользователя не может быть пустым";

        if (userName.length() < 4)
            return "Имя пользователя не должно быть короче 4-ых символов";

        if (userName.length() > 20)
            return "Имя пользователя не должно быть длиннее 20-ти символов";

        if (!USER_NAME.matcher(userName).matches()) {
            if (!Character.isLetterOrDigit(userName.charAt(0)))
                return "Имя пользователя не должно начинаться со специального символа";

            if (!Character.isLetterOrDigit(userName.charAt(userName.length() - 1)))
                return "Имя пользователя не должно заканчиваться специальным символом";

            return "Имя пользователя может содержать только англ. буквы, цифры и 1 спец. символ";
        }

        return null;
    }

    public static String validateChannelNameShort(String channelName) {
        if (channelName.isEmpty())
            return "Поле не может быть пустым";

        if (channelName.length() < 4)
            return "Имя группы не должно быть короче 4-ых символов";

        if (channelName.length() > 20)
            return "Имя группы не должно быть длиннее 20-ти символов";

        if (!CHANNEL_NAME.matcher(channelName).matches()) {
            if (!Character.isLetterOrDigit(channelName.charAt(0)))
                return "Имя группы не должно начинаться со специального символа";

            if (!Character.isLetterOrDigit(channelName.charAt(channelName.length() - 1)))
                return "Имя группы не должно заканчиваться специальным символом";

            return "Доступны только англ. буквы, цифры и 1 спец. символ";
        }

        return null;
    }

    private static String validateBirthday(ComboBox<Integer> day, ComboBox<String> month, ComboBox<Integer> year) {
        int selectedMonth = 0;

        if (day.getValue() == null || month.getValue() == null || year.getValue() == null)
            return "Все поля даты должны быть заполнены";

        try {
            selectedMonth = MONTHS.indexOf(month.getValue()) + 1;//Так к списке счёт начинается с 0, а в LocalDate с 1
            // Пытаемся создать LocalDate
            LocalDate birthDate = LocalDate.of(year.getValue(), selectedMonth, day.getValue());

            return null;
        } catch (DateTimeException e) {
            YearMonth yearMonth = YearMonth.of(year.getValue(), selectedMonth);
            int maxDaysInMonth = yearMonth.lengthOfMonth();

            if (day.getValue() > maxDaysInMonth)
                return "В месяце " + month.getValue() + ", " + year.getValue() + " года может быть не более "
                        + day.getValue() + "дней/дня";

            return "Некорректно указанная дата";
        }
    }

    //Метод, в котором проверяется валидация полей ввода текста и меняются стили в зависимости от корректности значения
    public static void updateStyleValidation(Map<? extends Parent, Label> map) {
        //Пробегаемся по строкам таблице, которую передали нам в качестве аргумента
        for (Map.Entry<? extends Parent, Label> entry : map.entrySet()) {
            //Подгружаем файл, откуда можно взять стили css
            setStyleSheets(entry.getKey(), entry.getValue());
            //Создаём временные переменный для упрощения читаемости кода
            Parent parent = entry.getKey();
            Label label = entry.getValue();

            //Получаем оригинальный текст из userData Label
            //userData-некое хранилище объекта-наследника класса Node, может хранить в себе любой объект.
            //Получаем объект из кармана, если в кармане пусто, то записываем туда текущий текс Label
            String originalText = (String) label.getUserData();
            if (originalText == null) {
                //Сохраняем оригинальный текст при первом вызове
                originalText = label.getText();
                label.setUserData(originalText);
            }

            //Проверяем валидность введённых пользователем значений в наследника TextInputControl
            String validationResult = null;
            if (parent instanceof TextInputControl)
                validationResult = validationField((TextInputControl) parent);

            if (parent instanceof HBox) {
                ComboBox<Integer> days = null;
                ComboBox<String> months = null;
                ComboBox<Integer> years = null;
                for (Node node : parent.getChildrenUnmodifiable()) {
                    switch (node.getId()) {
                        case "dayBirthdayCB":
                            days = (ComboBox<Integer>) node;
                            break;
                        case "monthBirthdayCB":
                            months = (ComboBox<String>) node;
                            break;
                        case "yearBirthdayCB":
                            years = (ComboBox<Integer>) node;
                    }
                }
                validationResult = validateBirthday(days, months, years);
            }

            //Сохраняем оригинальные стили ДО валидации
            String originalLabelStyle = (String) label.getProperties().get("originalStyle");
            String originalInputStyle = (String) parent.getProperties().get("originalStyle");

            if (originalLabelStyle == null) {
                originalLabelStyle = label.getStyle();
                label.getProperties().put("originalStyle", originalLabelStyle);
            }
            if (originalInputStyle == null) {
                originalInputStyle = parent.getStyle();
                parent.getProperties().put("originalStyle", originalInputStyle);
            }

            if (validationResult != null) {
                //Валидация не пройдена
                label.setText(validationResult);
                //Добавляем стили ошибки к существующим стилям
                label.setStyle(originalLabelStyle + " -fx-text-fill: rgba(115,0,0);");
                if (parent instanceof TextInputControl)
                    parent.setStyle(originalInputStyle + " -fx-border-color: rgba(115,0,0);");
            } else {
                //Валидация пройдена - восстанавливаем оригинальные стили
                label.setText(originalText);
                label.setStyle(originalLabelStyle);
                parent.setStyle(originalInputStyle);
            }
        }
    }

    public static boolean validConfirmCode(Label label, TextInputControl... textInputControls) {
        for(TextInputControl tIC : textInputControls) {
            String text = tIC.getText();
            if (text.isEmpty()) {
                label.setText("Все поля должны быть заполнены");
                label.setStyle("-fx-text-fill: rgba(115,0,0);");
                return false;
            }
            if (text.length() > 1 || !CONFIRM_CODE.matcher(text).matches()) {
                label.setText("В каждом поле допускается только 1 цифра");
                label.setStyle("-fx-text-fill: rgba(115,0,0);");
                return false;
            }
        }
        label.setText("");
        label.setStyle("");
        return true;
    }
}
