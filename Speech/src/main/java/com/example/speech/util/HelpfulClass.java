package com.example.speech.util;

import java.awt.*;
import java.net.URI;
import java.time.DateTimeException;
import java.time.LocalDate;

import static com.example.speech.util.HelpfulValidationClass.MONTHS;

public class HelpfulClass {

    //Метод для открытия веб страницы по переданному адресу
    public static void openWebPage(String urlAddress) {
        try {
            //Создаётся экземпляр рабочего стола
            Desktop desktop = Desktop.getDesktop();
            //Проверяем, поддерживает ли ОС пользователя открытие браузера
            if (desktop.isSupported(Desktop.Action.BROWSE))
                //Открывает URI в браузере по переданному адресу
                desktop.browse(new URI(urlAddress));
        } catch (Exception e) {
            e.printStackTrace();
            //showError("Не удалось открыть браузер");
        }
    }

    public static LocalDate getLocalDate(int day, String month, int year) {
        try {
            return LocalDate.of(year, MONTHS.indexOf(month) + 1, day);
        } catch (DateTimeException e) {
            System.err.println(e.getMessage());
        }
        return null;
    }
}
