package com.example.speech.util;

import javafx.application.Platform;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class HelpfulValidationClassTest {
    @BeforeAll
    static void initJavaFX() {
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            // уже запущен
        }
    }

    // ==================== validateEmail (только формат, без проверки существования) ====================
    @Test
    void testValidateEmail_Valid() {
        assertThat(HelpfulValidationClass.validationField(createField("mailTF", "user@gmail.com"))).isNull();
        assertThat(HelpfulValidationClass.validationField(createField("mailTF", "name.surname@yandex.ru"))).isNull();
        assertThat(HelpfulValidationClass.validationField(createField("mailTF", "test123@mail.com"))).isNull();
    }

    @Test
    void testValidateEmail_Empty() {
        String result = HelpfulValidationClass.validationField(createField("mailTF", ""));
        assertThat(result).isEqualTo("Email не может быть пустым");
    }

    @Test
    void testValidateEmail_NoAtSymbol() {
        String result = HelpfulValidationClass.validationField(createField("mailTF", "usergmail.com"));
        assertThat(result).isEqualTo("Адрес электронной почты должен содержать символ @");
    }

    @Test
    void testValidateEmail_TooLong() {
        String longEmail = "a".repeat(45) + "@gmail.com"; // общая длина 45+10=55 > 50
        String result = HelpfulValidationClass.validationField(createField("mailTF", longEmail));
        assertThat(result).isEqualTo("Адрес электронной почты не может быть длиннее 50-ти символов.");
    }

    @Test
    void testValidateEmail_InvalidLocalPart() {
        String result = HelpfulValidationClass.validationField(createField("mailTF", ".user@gmail.com"));
        assertThat(result).isEqualTo("Email не должен начинаться со специального символа");

        result = HelpfulValidationClass.validationField(createField("mailTF", "user.@gmail.com"));
        assertThat(result).isEqualTo("Email не должен заканчиваться специальным символом");

        result = HelpfulValidationClass.validationField(createField("mailTF", "us$er@gmail.com"));
        assertThat(result).isEqualTo("Email может содержать только англ. буквы, цифры и 1 спец. символ");
    }

    @Test
    void testValidateEmail_InvalidDomain() {
        String result = HelpfulValidationClass.validationField(createField("mailTF", "user@yahoo.com"));
        assertThat(result).isEqualTo("Допустимые домены: yandex, mail, gmail");

        // Некорректная доменная зона: одна буква
        result = HelpfulValidationClass.validationField(createField("mailTF", "user@mail.u"));
        assertThat(result).isEqualTo("Доменная зона должна состоять из 2-3 маленьких латинских букв");

        // Некорректная доменная зона: четыре буквы
        result = HelpfulValidationClass.validationField(createField("mailTF", "user@mail.uaaa"));
        assertThat(result).isEqualTo("Доменная зона должна состоять из 2-3 маленьких латинских букв");
    }

    // ==================== validatePassword ====================
    @Test
    void testValidatePassword_Valid() {
        assertThat(HelpfulValidationClass.validationField(createField("passwordF", "Pass1234"))).isNull();
        assertThat(HelpfulValidationClass.validationField(createField("passwordF", "A1b2C3d4E5"))).isNull();
    }

    @Test
    void testValidatePassword_Empty() {
        String result = HelpfulValidationClass.validationField(createField("passwordF", ""));
        assertThat(result).isEqualTo("Пароль не может быть пустым");
    }

    @Test
    void testValidatePassword_TooShort() {
        String result = HelpfulValidationClass.validationField(createField("passwordF", "Abc12"));
        assertThat(result).isEqualTo("Пароль должен быть не менее 8 и не более 50 символов");
    }

    @Test
    void testValidatePassword_TooLong() {
        String longPass = "a".repeat(51);
        String result = HelpfulValidationClass.validationField(createField("passwordF", longPass));
        assertThat(result).isEqualTo("Пароль должен быть не менее 8 и не более 50 символов");
    }

    @Test
    void testValidatePassword_NoDigit() {
        String result = HelpfulValidationClass.validationField(createField("passwordF", "PasswordNoDigit"));
        assertThat(result).isEqualTo("Пароль должен содержать хотя бы одну цифру");
    }

    @Test
    void testValidatePassword_NoLetter() {
        String result = HelpfulValidationClass.validationField(createField("passwordF", "12345678"));
        assertThat(result).isEqualTo("Пароль должен содержать хотя бы одну английскую букву");
    }

    // ==================== validateVisibleName ====================
    @Test
    void testValidateVisibleName_Valid() {
        assertThat(HelpfulValidationClass.validationField(createField("visibleNameTF", "John"))).isNull();
        assertThat(HelpfulValidationClass.validationField(createField("visibleNameTF", "A very long name"))).isNull();
    }

    @Test
    void testValidateVisibleName_Empty() {
        // Пустое значение допустимо (необязательное поле)
        assertThat(HelpfulValidationClass.validationField(createField("visibleNameTF", ""))).isNull();
    }

    @Test
    void testValidateVisibleName_TooShort() {
        String result = HelpfulValidationClass.validationField(createField("visibleNameTF", "Abc"));
        assertThat(result).isEqualTo("Отображаемое имя не должно быть короче 4-ых символов");
    }

    @Test
    void testValidateVisibleName_TooLong() {
        String result = HelpfulValidationClass.validationField(createField("visibleNameTF", "a".repeat(21)));
        assertThat(result).isEqualTo("Отображаемое имя не должно быть длиннее 20-ти символов");
    }

    // ==================== validateUserName ====================
    @Test
    void testValidateUserName_Valid() {
        assertThat(HelpfulValidationClass.validationField(createField("userNameTF", "JohnDoe"))).isNull();
        assertThat(HelpfulValidationClass.validationField(createField("userNameTF", "user_123"))).isNull();
        assertThat(HelpfulValidationClass.validationField(createField("userNameTF", "a".repeat(4)))).isNull();
    }

    @Test
    void testValidateUserName_Empty() {
        String result = HelpfulValidationClass.validationField(createField("userNameTF", ""));
        assertThat(result).isEqualTo("Имя пользователя не может быть пустым");
    }

    @Test
    void testValidateUserName_TooShort() {
        String result = HelpfulValidationClass.validationField(createField("userNameTF", "abc"));
        assertThat(result).isEqualTo("Имя пользователя не должно быть короче 4-ых символов");
    }

    @Test
    void testValidateUserName_TooLong() {
        String result = HelpfulValidationClass.validationField(createField("userNameTF", "a".repeat(21)));
        assertThat(result).isEqualTo("Имя пользователя не должно быть длиннее 20-ти символов");
    }

    @Test
    void testValidateUserName_InvalidStartEnd() {
        String result = HelpfulValidationClass.validationField(createField("userNameTF", "_user"));
        assertThat(result).isEqualTo("Имя пользователя не должно начинаться со специального символа");

        result = HelpfulValidationClass.validationField(createField("userNameTF", "user_"));
        assertThat(result).isEqualTo("Имя пользователя не должно заканчиваться специальным символом");
    }

    @Test
    void testValidateUserName_InvalidChars() {
        String result = HelpfulValidationClass.validationField(createField("userNameTF", "user$name"));
        assertThat(result).isEqualTo("Имя пользователя может содержать только англ. буквы, цифры и 1 спец. символ");
    }

    // ==================== validateUserNameShort ====================
    @Test
    void testValidateUserNameShort_Valid() {
        assertThat(HelpfulValidationClass.validateUserNameShort("JohnDoe")).isNull();
    }

    @Test
    void testValidateUserNameShort_Empty() {
        assertThat(HelpfulValidationClass.validateUserNameShort("")).isEqualTo("Имя пользователя не может быть пустым");
    }

    @Test
    void testValidateUserNameShort_TooShort() {
        assertThat(HelpfulValidationClass.validateUserNameShort("abc")).isEqualTo("Имя пользователя не должно быть короче 4-ых символов");
    }

    // ==================== validateChannelNameShort ====================
    @Test
    void testValidateChannelNameShort_Valid() {
        assertThat(HelpfulValidationClass.validateChannelNameShort("Channel1")).isNull();
    }

    @Test
    void testValidateChannelNameShort_Empty() {
        assertThat(HelpfulValidationClass.validateChannelNameShort("")).isEqualTo("Поле не может быть пустым");
    }

    @Test
    void testValidateChannelNameShort_TooShort() {
        assertThat(HelpfulValidationClass.validateChannelNameShort("abc")).isEqualTo("Имя группы не должно быть короче 4-ых символов");
    }

    @Test
    void testValidateChannelNameShort_InvalidStart() {
        assertThat(HelpfulValidationClass.validateChannelNameShort("_channel")).isEqualTo("Имя группы не должно начинаться со специального символа");
    }

    // ==================== validateBirthday ====================
    @Test
    void testValidateBirthday_Valid() {
        ComboBox<Integer> day = new ComboBox<>();
        ComboBox<String> month = new ComboBox<>();
        ComboBox<Integer> year = new ComboBox<>();
        day.setValue(15);
        month.setValue("Март");
        year.setValue(2000);

        // Доступа к приватному методу validateBirthday нет, но он вызывается из updateStyleValidation.
        // Для тестирования мы можем вызвать через рефлексию или протестировать через updateStyleValidation с HBox.
        // Поскольку updateStyleValidation сложный, лучше проверить логику через вызов updateStyleValidation с минимальным HBox.
        // Создадим HBox с ComboBox и вызовем updateStyleValidation.
        javafx.scene.layout.HBox hbox = new javafx.scene.layout.HBox(day, month, year);
        day.setId("dayBirthdayCB");
        month.setId("monthBirthdayCB");
        year.setId("yearBirthdayCB");
        Label label = new Label();
        java.util.Map<javafx.scene.Parent, Label> map = java.util.Map.of(hbox, label);
        HelpfulValidationClass.updateStyleValidation(map);
        assertThat(label.getText()).isEqualTo(""); // нет ошибки
    }

    // можно добавить тест на неверную дату (31 февраля)
    @Test
    void testValidateBirthday_InvalidDay() {
        ComboBox<Integer> day = new ComboBox<>();
        ComboBox<String> month = new ComboBox<>();
        ComboBox<Integer> year = new ComboBox<>();
        day.setValue(31);
        month.setValue("Февраль");
        year.setValue(2021);
        javafx.scene.layout.HBox hbox = new javafx.scene.layout.HBox(day, month, year);
        day.setId("dayBirthdayCB");
        month.setId("monthBirthdayCB");
        year.setId("yearBirthdayCB");
        Label label = new Label();
        java.util.Map<javafx.scene.Parent, Label> map = java.util.Map.of(hbox, label);
        HelpfulValidationClass.updateStyleValidation(map);
        assertThat(label.getText()).contains("не более 28 дней");
    }

    // ==================== validConfirmCode ====================
    @Test
    void testValidConfirmCode_Valid() {
        TextField t1 = new TextField("1");
        TextField t2 = new TextField("2");
        Label label = new Label();
        boolean result = HelpfulValidationClass.validConfirmCode(label, t1, t2);
        assertThat(result).isTrue();
        assertThat(label.getText()).isEmpty();
    }

    @Test
    void testValidConfirmCode_EmptyField() {
        TextField t1 = new TextField("1");
        TextField t2 = new TextField("");
        Label label = new Label();
        boolean result = HelpfulValidationClass.validConfirmCode(label, t1, t2);
        assertThat(result).isFalse();
        assertThat(label.getText()).isEqualTo("Все поля должны быть заполнены");
    }

    @Test
    void testValidConfirmCode_MoreThanOneDigit() {
        TextField t1 = new TextField("12");
        TextField t2 = new TextField("3");
        Label label = new Label();
        boolean result = HelpfulValidationClass.validConfirmCode(label, t1, t2);
        assertThat(result).isFalse();
        assertThat(label.getText()).isEqualTo("В каждом поле допускается только 1 цифра");
    }

    @Test
    void testValidConfirmCode_NotDigit() {
        TextField t1 = new TextField("a");
        TextField t2 = new TextField("1");
        Label label = new Label();
        boolean result = HelpfulValidationClass.validConfirmCode(label, t1, t2);
        assertThat(result).isFalse();
        assertThat(label.getText()).isEqualTo("В каждом поле допускается только 1 цифра");
    }

    // Вспомогательный метод для создания текстового поля с id и текстом
    private TextField createField(String id, String text) {
        TextField tf = new TextField(text);
        tf.setId(id);
        return tf;
    }
}