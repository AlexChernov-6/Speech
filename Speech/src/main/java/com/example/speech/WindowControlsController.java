package com.example.speech;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.stage.Stage;

public class WindowControlsController {
    @FXML
    Button wrapBtn, expandBtn, closeBtn;

    @FXML
    public void onWrapBtn() {
        Stage stage = (Stage) wrapBtn.getScene().getWindow();
        stage.setIconified(true);
    }

    @FXML
    public void onExpandBtn() {
        Stage stage = (Stage) expandBtn.getScene().getWindow();
        stage.setFullScreen(!stage.isFullScreen());
    }

    @FXML
    public void onCloseBtn() {
        Stage stage = (Stage) closeBtn.getScene().getWindow();
        stage.close();
    }
}
