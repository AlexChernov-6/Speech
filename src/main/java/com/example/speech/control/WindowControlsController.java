package com.example.speech.control;

import com.example.speech.util.HelpfulClass;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

public class WindowControlsController {

    private boolean isFullScreen = false;

    @FXML
    private Button wrapBtn, expandBtn, closeBtn;

    @FXML
    private void onWrapBtn() {
        Stage stage = (Stage) wrapBtn.getScene().getWindow();
        stage.setIconified(true);
    }

    @FXML
    private void onExpandBtn() {
        Stage stage = (Stage) expandBtn.getScene().getWindow();
        stage.setFullScreen(!stage.isFullScreen());

        isFullScreen = !isFullScreen;
        updateBackgroundExpandBtn();
    }

    @FXML
    private void onCloseBtn() {
        Stage stage = (Stage) closeBtn.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void initialize() {
        HelpfulClass.setImageWithButton(wrapBtn, "wrap-window.png", "window-control-button", 25, 40);
        updateBackgroundExpandBtn();
        HelpfulClass.setImageWithButton(closeBtn, "close-application.png","window-control-button", 25, 40);
        closeBtn.getStyleClass().add("close-button");
    }

    private void updateBackgroundExpandBtn() {
        if (isFullScreen)
            HelpfulClass.setImageWithButton(expandBtn, "fit-screen.png", "window-control-button", 25, 40);
        else
            HelpfulClass.setImageWithButton(expandBtn, "full-screen-image.png", "window-control-button", 25, 40);
    }
}
