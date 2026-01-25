package com.example.speech.control;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class DateMessageCellController {
    @FXML
    private Label dateLabel;
    
    public void setDate(String date) {
        dateLabel.setText(date);
    }
    
    public void setMaxWidth(double width) {
        dateLabel.setMaxWidth(width);
    }
}