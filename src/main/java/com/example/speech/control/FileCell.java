package com.example.speech.control;

import com.example.speech.util.ImageUtils;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
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
    private final Label fileNameLB;
    private final ImageView imageView;

    private File file;

    public FileCell(SpeechBaseController speechBaseController) {
        setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.PRIMARY && file != null) {
                String fileType;
                int dotIndex = file.getName().lastIndexOf('.');
                if (dotIndex > 0 && dotIndex < file.getName().length() - 1) {
                    fileType = file.getName().substring(dotIndex + 1).toLowerCase();
                } else {
                    fileType = "";
                }
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

                            }
                        }
                    }
                }
            }
        });

        rootSP = new StackPane();
        rootSP.setMaxHeight(60);
        rootSP.setMaxWidth(60);

        imageView = new ImageView(
                new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/example/speech/image/doc.png"))));
        imageView.setFitHeight(40);
        imageView.setFitWidth(40);
        StackPane.setAlignment(imageView, Pos.BOTTOM_CENTER);
        rootSP.getChildren().add(imageView);

        fileNameLB = new Label();
        fileNameLB.setMaxWidth(60);
        fileNameLB.setPrefWidth(60);
        fileNameLB.setVisible(false);
        fileNameLB.setStyle("-fx-text-fill: white; -fx-font-size: 14; -fx-font-weight: bold;");
        fileNameLB.setAlignment(Pos.CENTER);
        fileNameLB.setMouseTransparent(true);
        StackPane.setAlignment(fileNameLB, Pos.TOP_CENTER);
        StackPane.setMargin(fileNameLB, new Insets(30, 0, 0, 0));
        rootSP.getChildren().add(fileNameLB);

        Button delBtn = new Button();
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
            String fileType;
            int dotIndex = file.getName().lastIndexOf('.');
            if (dotIndex > 0 && dotIndex < file.getName().length() - 1) {
                fileType = file.getName().substring(dotIndex + 1).toLowerCase();
            } else {
                fileType = "";
            }
            if (fileType.equals("png") || fileType.equals("jpg") || fileType.equals("gif")) {
                fileNameLB.setVisible(false);
                try {
                    imageView.setImage(new Image(new FileInputStream(file)));
                } catch (FileNotFoundException e) {
                    fileNameLB.setVisible(true);
                    fileNameLB.setText(fileType);
                    System.err.println(e.getMessage());
                }
            } else {
                fileNameLB.setVisible(true);
                fileNameLB.setText(fileType);
            }
            setGraphic(rootSP);
        }
    }
}
