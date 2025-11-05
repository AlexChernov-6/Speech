package com.example.speech;

import javafx.stage.Stage;

public class SpeechBase implements Window {
    private Stage stage;

    @Override
    public void setStage(Stage stage) {
        this.stage = stage;
    }
}
