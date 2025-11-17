package com.example.speech.otherClass;

import javafx.stage.Stage;

import java.io.IOException;

public abstract class AbstractModalStage {
    public abstract void showModalStage(Stage mainStage, String otherParam) throws IOException;
}
