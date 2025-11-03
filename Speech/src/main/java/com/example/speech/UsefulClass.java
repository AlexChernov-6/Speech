package com.example.speech;

import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
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

    //Статичный метод, который имеет дженерик, в качестве возвращаемого типа будет тип String(сообщение о валидации)
    //В качестве аргумента может принимать любой объект, который наследуется от TextInputControl(TextField, PasswordField и т.д.)
    public static <T extends TextInputControl> String validationField(T inputField) {
        //Получает значение в экземпляре-наследнике TextInputControl
        String text = inputField.getText().trim();

        //Проверяем соответствие inputField классу PasswordField и TextField, для класса TextField
        //Так же делаем проверки по id, для раздачи нужных правил валидации в зависимости от назначения объекта TextField
        if (inputField.getClass() == TextField.class &&
                inputField.getId() != null && inputField.getId().equals("mailTF")) {
            return validateEmail(text);
        } else if (inputField.getClass() == PasswordField.class) {
            return validatePassword(text);
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
}
