package com.example.speech.control;

import com.example.speech.model.Message;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ListCell;
import javafx.scene.layout.Region;

import java.io.IOException;

public class ListTextMessageCellController extends ListCell<Message> {
    private final Region parent;
    private final TextMessageCellController controller;
    public Message message;

    public ListTextMessageCellController() {
        try {
            FXMLLoader loader = new FXMLLoader(SpeechBaseController.class.getResource(
                    "/com/example/speech/shape/TextMessageCellShape.fxml"));
            parent = loader.load();
            controller = loader.getController();
        } catch (IOException e) {
            System.err.println(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void updateItem(Message message, boolean empty) {
        this.message = message;
        super.updateItem(message, empty);

        if (empty || message == null) {
            setGraphic(null);
        } else {
            controller.setMaxWidth(getListView().getWidth() - 100);
            getListView().widthProperty().addListener((observable, oldValue, newValue) -> {
                controller.setMaxWidth(newValue.doubleValue() - 100);
            });
            controller.initializeMessage(message);

            setGraphic(parent);
        }
    }
}
