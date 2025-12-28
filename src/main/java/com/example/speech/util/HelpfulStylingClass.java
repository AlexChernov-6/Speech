package com.example.speech.util;

import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.image.*;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class HelpfulStylingClass {
    //Сам метод подгружает файл, откуда будут использоваться стили для контроллеров, переданных в качестве аргумента
    //Метод в качестве аргумента может принимать любое количество экземпляров-наследников класса Control.
    public static void setStyleSheets(Parent... parents) {
        String cssUrl = HelpfulClass.class.getResource("/com/example/speech/styles.css").toExternalForm();
        for (Parent parent : parents) {
            parent.getStylesheets().add(cssUrl);
        }
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

    //Метод, который следит за изменениями состояний окна, а конкретно за FullScreen
    public static void setupFullScreenListener(Stage stage, Pane rootPane) {
        //Добавляем обработчика, то что следит за изменениями для fullScreenProperty
        //Вместо создания отдельного класса, который реализовывает интерфейс InvalidationListener
        //Используем лямбда-функцию, в которой переопределим необходимый метод invalidated
        stage.fullScreenProperty().addListener(observable -> {
            //Если у нас окно открыто в полный экран-то есть имеет state FullScreen, то мы убираем отступы у корневого
            //Pane в обратном случае, мы возвращаем ему отступы для его содержимого
            if (stage.isFullScreen())
                rootPane.setPadding(new Insets(0));
            else
                rootPane.setPadding(new Insets(13));
        });

        // Устанавливаем начальное значение
        if (stage.isFullScreen())
            rootPane.setPadding(new Insets(0));
        else
            rootPane.setPadding(new Insets(13));
    }

    public static Image byteArrayToImage(byte[] imageData) {
        if (imageData == null || imageData.length == 0) {
            return null;
        }
        try {
            return new Image(new ByteArrayInputStream(imageData));
        } catch (Exception e) {
            System.err.println("Ошибка преобразования byte[] в Image: " + e.getMessage());
            return null;
        }
    }

    public static byte[] imageViewToByteArray(ImageView imageView) {
        if (imageView == null || imageView.getImage() == null) {
            return null;
        }

        try {
            Image image = imageView.getImage();
            PixelReader pixelReader = image.getPixelReader();

            if (pixelReader == null) {
                // Если PixelReader недоступен (изображение еще не загружено)
                return null;
            }

            int width = (int) image.getWidth();
            int height = (int) image.getHeight();

            // Создаем WritableImage для работы с пикселями
            WritableImage writableImage = new WritableImage(width, height);
            PixelWriter pixelWriter = writableImage.getPixelWriter();

            // Копируем пиксели из исходного изображения
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    pixelWriter.setColor(x, y, pixelReader.getColor(x, y));
                }
            }

            // Конвертируем в PNG формат
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            // Создаем BufferedImage вручную
            BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    Color color = pixelReader.getColor(x, y);
                    int argb = ((int)(color.getOpacity() * 255) << 24) |
                            ((int)(color.getRed() * 255) << 16) |
                            ((int)(color.getGreen() * 255) << 8) |
                            ((int)(color.getBlue() * 255));
                    bufferedImage.setRGB(x, y, argb);
                }
            }

            // Используем ImageIO из Java (не требует сторонних библиотек)
            ImageIO.write(bufferedImage, "png", outputStream);
            return outputStream.toByteArray();
        } catch (Exception e) {
            System.err.println("Ошибка преобразования ImageView в byte[]: " + e.getMessage());
            return null;
        }
    }
}
