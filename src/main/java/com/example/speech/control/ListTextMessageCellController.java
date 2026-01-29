package com.example.speech.control;

import com.example.speech.model.Message;
import com.example.speech.model.User;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ListCell;

import java.io.IOException;

public class ListTextMessageCellController extends ListCell<Message> {
    private final TextMessageCellController controller;
    private final User currentUser;
    private final boolean drawUserPhoto;
    
    public ListTextMessageCellController(User currentUser, boolean drawUserPhoto) {
        this.currentUser = currentUser;
        this.drawUserPhoto = drawUserPhoto;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/com/example/speech/shape/TextMessageCellShape.fxml"));
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
                double maxWidth = getListView().getWidth() - 100;
                controller.setMaxWidth(maxWidth);
                
                getListView().widthProperty().addListener((observable, oldValue, newValue) -> {
                    controller.setMaxWidth(newValue.doubleValue() - 100);
                });
            }
            
            controller.initializeMessage(message, currentUser, drawUserPhoto);
        }
    }
}