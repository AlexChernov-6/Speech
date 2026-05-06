package com.example.speech.control;

import com.example.speech.util.ImageUtils;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.StackPane;

import java.awt.*;
import java.io.*;
import java.util.Objects;

public class FileCell extends ListCell<File> {
    private final StackPane rootSP;
    private final ImageView imageView;
    private final Label fileNameLB;

    private File file;

    public FileCell(SpeechBaseController speechBaseController) {
        setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.PRIMARY) {
                String fileType = file.getName().split("\\.")[1];
                if (fileType.equals("png") || fileType.equals("jpg") || fileType.equals("gif")) {
                    ImageUtils.viewingImages(speechBaseController.getMessagesSP()
                            , speechBaseController.selectedFile.stream()
                                    .filter(f -> {
                                        String fType = f.getName().split("\\.")[1];
                                        return fType.equals("png") || fType.equals("jpg") || fType.equals("gif");
                                    })
                                    .map(f -> {
                                        try {
                                            return new Image(new FileInputStream(f));
                                        } catch (FileNotFoundException ex) {
                                            throw new RuntimeException(ex);
                                        }
                                    }).toList());
                } else {
                    // 1. Проверяем, поддерживает ли платформа класс Desktop
                    if (Desktop.isDesktopSupported()) {
                        Desktop desktop = Desktop.getDesktop();
                        // 2. Проверяем, существует ли файл
                        if (file.exists()) {
                            try {
                                // 3. Открываем файл в программе по умолчанию
                                desktop.open(file);
                            } catch (IOException e1) {
                                e1.printStackTrace();
                            }
                        }
                    }
                }
            }
        });

        rootSP = new StackPane();
        rootSP.setMaxHeight(80);
        rootSP.setMaxWidth(80);

        imageView = new ImageView();
        imageView.setFitHeight(60);
        imageView.setFitWidth(60);
        StackPane.setAlignment(imageView, Pos.BOTTOM_CENTER);
        rootSP.getChildren().add(imageView);

        fileNameLB = new Label();
        fileNameLB.setMaxWidth(80);
        fileNameLB.setPrefWidth(80);
        fileNameLB.setVisible(false);
        fileNameLB.setStyle("-fx-background-color: rgba(75, 75, 75, 0.3); -fx-text-fill: white; -fx-font-size: 10; -fx-font-weight: bold;");
        fileNameLB.setAlignment(Pos.CENTER);
        fileNameLB.setMouseTransparent(true);
        StackPane.setAlignment(fileNameLB, Pos.CENTER);
        rootSP.getChildren().add(fileNameLB);

        Button delBtn = new Button();
        delBtn.setMaxHeight(10);
        delBtn.setMaxWidth(10);
        delBtn.getStyleClass().add("image-btn");
        StackPane.setAlignment(delBtn, Pos.TOP_RIGHT);
        delBtn.setOnAction(e -> {
            speechBaseController.selectedFile.remove(file);
        });

        ImageView btnImageView = new ImageView(
                new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/example/speech/image/delFile.png"))));
        btnImageView.setFitHeight(20);
        btnImageView.setFitWidth(20);
        btnImageView.setPreserveRatio(true);

        delBtn.setGraphic(btnImageView);

        rootSP.getChildren().add(delBtn);
    }

    @Override
    protected void updateItem(File file, boolean empty) {
        this.file = file;
        super.updateItem(file, empty);

        if (empty || file == null) {
            setGraphic(null);
            setText(null);
        } else {
            String fileType = file.getName().split("\\.")[1];
            if (fileType.equals("txt")) {
                imageView.setImage(
                        new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/example/speech/image/txt.png"))));
                fileNameLB.setVisible(true);
                fileNameLB.setText(file.getName());
            } else if (fileType.equals("png") || fileType.equals("jpg") || fileType.equals("gif")) {
                try {
                    imageView.setImage(new Image(new FileInputStream(file)));
                } catch (FileNotFoundException e) {
                    imageView.setImage(
                            new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/example/speech/image/document.png"))));
                    System.err.println(e.getMessage());
                }
            } else {
                imageView.setImage(
                        new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/example/speech/image/document.png"))));
                fileNameLB.setVisible(true);
                fileNameLB.setText(file.getName());
            }
            setGraphic(rootSP);
        }
    }
}
