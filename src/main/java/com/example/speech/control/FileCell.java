package com.example.speech.control;

import com.example.speech.util.HelpfulClass;
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
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;

import java.awt.*;
import java.io.*;
import java.util.Objects;

public class FileCell extends ListCell<File> {
    private final StackPane rootSP;
    private final Label fileNameLB;
    private final ImageView imageView;
    private final Pane errorPane;

    private File file;
    private final SpeechBaseController speechBaseController;

    public FileCell(SpeechBaseController speechBaseController) {
        this.speechBaseController = speechBaseController;
        getStyleClass().add("list-cell-transparent");
        setPadding(new Insets(0, 5, 0, 5));
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
                    if (Desktop.isDesktopSupported()) {
                        Desktop desktop = Desktop.getDesktop();
                        if (file.exists()) {
                            try {
                                desktop.open(file);
                            } catch (IOException e1) {

                            }
                        }
                    }
                }
            }
        });

        rootSP = new StackPane();
        rootSP.setMaxHeight(65);
        rootSP.setMaxWidth(65);
        rootSP.getStyleClass().add("file-list-cell");

        errorPane = new Pane();
        errorPane.setStyle("-fx-background-color: rgba(230, 0, 0, 0.3); -fx-background-radius: 12px;");
        errorPane.setVisible(false);
        errorPane.toFront();
        errorPane.setMouseTransparent(true);
        errorPane.maxWidthProperty().bind(rootSP.widthProperty().subtract(1));
        errorPane.maxHeightProperty().bind(rootSP.heightProperty().subtract(1));
        rootSP.getChildren().add(errorPane);

        imageView = new ImageView(
                new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/example/speech/image/doc.png"))));
        imageView.setFitHeight(40);
        imageView.setFitWidth(40);
        imageView.setMouseTransparent(true);
        StackPane.setAlignment(imageView, Pos.BOTTOM_CENTER);
        StackPane.setMargin(imageView, new Insets(0, 0, 5, 0));
        rootSP.getChildren().add(imageView);

        fileNameLB = new Label();
        fileNameLB.setMaxWidth(60);
        fileNameLB.setPrefWidth(60);
        fileNameLB.setVisible(false);
        fileNameLB.setStyle("-fx-text-fill: white; -fx-font-size: 14; -fx-font-weight: bold;");
        fileNameLB.setAlignment(Pos.CENTER);
        fileNameLB.setMouseTransparent(true);
        StackPane.setAlignment(fileNameLB, Pos.TOP_CENTER);
        StackPane.setMargin(fileNameLB, new Insets(32.5, 0, 0, 0));
        rootSP.getChildren().add(fileNameLB);

        Button delBtn = new Button();
        delBtn.getStyleClass().add("image-btn");
        StackPane.setAlignment(delBtn, Pos.TOP_RIGHT);
        delBtn.setOnAction(e -> {
            speechBaseController.selectedFile.remove(file);
        });

        ImageView btnImageViewNotFocused = new ImageView(
                new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/example/speech/image/del-file-not-focused.png"))));
        btnImageViewNotFocused.setFitHeight(20);
        btnImageViewNotFocused.setFitWidth(20);
        btnImageViewNotFocused.setPreserveRatio(true);

        ImageView btnImageViewFocused = new ImageView(
                new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/example/speech/image/del-file-focused.png"))));
        btnImageViewFocused.setFitHeight(20);
        btnImageViewFocused.setFitWidth(20);
        btnImageViewFocused.setPreserveRatio(true);

        delBtn.setGraphic(btnImageViewNotFocused);

        delBtn.setOnMouseEntered(e ->{
            delBtn.setGraphic(btnImageViewFocused);
        });

        delBtn.setOnMouseExited(e ->{
            delBtn.setGraphic(btnImageViewNotFocused);
        });

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
                }
            } else {
                fileNameLB.setVisible(true);
                fileNameLB.setText(fileType);
            }


            if(file.length() >= 2 * 1024 * 1024 || speechBaseController.delFiles.contains(file))
                errorPane.setVisible(true);
            else errorPane.setVisible(false);

            setGraphic(rootSP);
        }
    }
}
