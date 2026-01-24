package com.example.speech.control;

import com.example.speech.model.ChannelUser;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.ListCell;

import java.io.IOException;

public class ListChannelsCellController extends ListCell<ChannelUser> {

    private final Parent root;
    private final ChannelCellController controller;

    public ListChannelsCellController() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass()
                    .getResource("/com/example/speech/shape/ChannelCellShape.fxml"));
            root = loader.load();
            controller = loader.getController();

            root.minHeight(60);
            root.prefHeight(60);

        } catch (IOException e) {
            System.err.println(e.getMessage());
            throw new RuntimeException("Failed to load ChannelCellShape.fxml", e);
        }
    }

    @Override
    protected void updateItem(ChannelUser channelUser, boolean empty) {
        super.updateItem(channelUser, empty);

        setText(null);
        setGraphic(null);

        if (empty || channelUser == null) {
            setGraphic(null);
        } else {
            try {
                controller.initialize(channelUser);
                setGraphic(root);

                // Установите высоту ячейки
                setPrefHeight(60);
                setMinHeight(60);
            } catch (Exception e) {
                e.printStackTrace();
                setText("Ошибка загрузки данных");
            }
        }
    }
}
