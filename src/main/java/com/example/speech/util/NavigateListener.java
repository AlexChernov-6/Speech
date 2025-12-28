package com.example.speech.util;

import javafx.scene.control.Button;
import javafx.scene.control.TextInputControl;
import javafx.scene.input.KeyCode;
import java.util.List;

public class NavigateListener {

    public static void setLinkListener(List<? extends TextInputControl> list) {
        for (int i = 0; i < list.size(); i++) {
            final int index = i;
            TextInputControl field = list.get(index);
            field.setText("");

            // Добавляем обработку клавиш для лучшего UX
            field.setOnKeyPressed(event -> {
                switch (event.getCode()) {
                    case RIGHT:
                        if (index < list.size() - 1) {
                            list.get(index + 1).requestFocus();
                        }
                        break;
                    case LEFT, BACK_SPACE:
                        if (index > 0) {
                            list.get(index - 1).requestFocus();
                        }
                        break;
                }
            });

            field.textProperty().addListener((observable, oldValue, newValue) -> {
                // Ограничение одного символа
                if (newValue.length() > 1) {
                    field.setText(newValue.substring(0, 1));
                    return;
                }

                // Автопереход при вводе
                if (!newValue.isEmpty() && index < list.size() - 1) {
                    list.get(index + 1).requestFocus();
                }

                // Автопереход при удалении (Backspace)
                if (newValue.isEmpty() && !oldValue.isEmpty() && index > 0) {
                    list.get(index - 1).requestFocus();
                }
            });
        }
    }

    public static void setEnterPressed(Button button) {
        button.getParent().setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ENTER) {
                button.fire();
            }
        });
    }
}
