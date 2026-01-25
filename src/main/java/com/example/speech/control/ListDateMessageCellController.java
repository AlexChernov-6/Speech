package com.example.speech.control;

import com.example.speech.model.Message;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ListCell;

import java.io.IOException;

public class ListDateMessageCellController extends ListCell<Message> {
    private DateMessageCellController controller;
    
    public ListDateMessageCellController() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/com/example/speech/shape/DateMessageCellShape.fxml"));
            loader.load();
            controller = loader.getController();
            setGraphic(loader.getRoot());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    @Override
    protected void updateItem(Message message, boolean empty) {
        super.updateItem(message, empty);
        
        if (empty || message == null) {
            setText(null);
            setGraphic(null);
        } else {
            if (getListView() != null) {
                double listViewWidth = getListView().getWidth();
                double maxWidth = listViewWidth * 0.6;
                controller.setMaxWidth(maxWidth);
                
                getListView().widthProperty().addListener((obs, oldVal, newVal) -> {
                    controller.setMaxWidth(newVal.doubleValue() * 0.6);
                });
            }
            
            String dateStr = message.getMessageDatetime().toLocalDate()
                .format(java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy"));
            controller.setDate(dateStr);
        }
    }
}