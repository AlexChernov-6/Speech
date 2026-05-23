package com.example.speech.util;

import com.example.speech.control.EntranceController;
import com.example.speech.service.UserService;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.awt.Desktop;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
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

    public static String getLocalDate(int day, String month, int year) {
        try {
            return LocalDate.of(year, MONTHS.indexOf(month) + 1, day).format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
        } catch (DateTimeException e) {
            System.err.println(e.getMessage());
        }
        return null;
    }

    public static String loadTemplate(String templateName, Map<String, String> variables) throws IOException {
        String templatePath = "/com/example/speech/html/" + templateName;

        try (InputStream inputStream = EntranceController.class.getResourceAsStream(templatePath)) {
            if (inputStream == null) {
                throw new IOException("Template not found: " + templatePath);
            }

            String template = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);

            // Заменяем переменные в шаблоне
            for (Map.Entry<String, String> entry : variables.entrySet()) {
                template = template.replace("{{" + entry.getKey() + "}}",
                        entry.getValue() != null ? entry.getValue() : "");
            }

            return template;
        }
    }

    public static String loadPasswordResetTemplate(String resetLink, String recipientEmail)
            throws IOException, SQLException {
        Map<String, String> variables = new HashMap<>();
        String userName = Objects.requireNonNull(new UserService().getUserByEmail(recipientEmail)).getNameUser();
        variables.put("userName", userName);
        variables.put("resetLink", resetLink);
        return loadTemplate("lost-password.html", variables);
    }

    public static void setImageWithButton(Button btn, String imageName) {
        btn.getStyleClass().setAll("icon-button");
        double btnHeight = 40;
        double btnWidth = 40;

        btn.setMinHeight(btnHeight);
        btn.setPrefHeight(btnHeight);
        btn.setMaxHeight(btnHeight);

        btn.setMinWidth(btnWidth);
        btn.setPrefWidth(btnWidth);
        btn.setMaxWidth(btnWidth);

        btn.setAlignment(Pos.CENTER);

        ImageView buttonImage = new ImageView(
                new Image(Objects.requireNonNull(HelpfulClass.class.getResourceAsStream("/com/example/speech/image/" + imageName))));
        buttonImage.setFitHeight(btnHeight / 2);
        buttonImage.setFitWidth(btnWidth / 2);
        buttonImage.setPreserveRatio(true);

        btn.setGraphic(buttonImage);
    }

    public static void setImageWithButton(Button btn, String imageName, String styleClassName, double btnHeight, double btnWidth) {
        btn.getStyleClass().setAll(styleClassName);

        btn.setMinHeight(btnHeight);
        btn.setPrefHeight(btnHeight);
        btn.setMaxHeight(btnHeight);

        btn.setMinWidth(btnWidth);
        btn.setPrefWidth(btnWidth);
        btn.setMaxWidth(btnWidth);

        btn.setAlignment(Pos.CENTER);

        ImageView buttonImage = new ImageView(
                new Image(Objects.requireNonNull(HelpfulClass.class.getResourceAsStream("/com/example/speech/image/" + imageName))));
        double fitSize = Math.min(btnHeight, btnWidth);
        buttonImage.setFitHeight(fitSize / 2);
        buttonImage.setFitWidth(fitSize / 2);
        buttonImage.setPreserveRatio(true);

        btn.setGraphic(buttonImage);

        btn.setStyle("-fx-cursor: hand;");
    }
}
