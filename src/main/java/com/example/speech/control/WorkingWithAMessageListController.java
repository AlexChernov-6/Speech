package com.example.speech.control;

import com.example.speech.model.Message;
import javafx.geometry.Insets;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.Objects;

public class WorkingWithAMessageListController extends Pane {

    private static final Image replyI = new Image(Objects.requireNonNull
            (WorkingWithAMessageListController.class.getResourceAsStream("/com/example/speech/image/reply.png")));
    private static final Image pinI = new Image(Objects.requireNonNull
            (WorkingWithAMessageListController.class.getResourceAsStream("/com/example/speech/image/pin.png")));
    private static final Image copyI = new Image(Objects.requireNonNull
            (WorkingWithAMessageListController.class.getResourceAsStream("/com/example/speech/image/copy.png")));
    private static final Image forwardI = new Image(Objects.requireNonNull
            (WorkingWithAMessageListController.class.getResourceAsStream("/com/example/speech/image/forward.png")));
    private static final Image deleteI = new Image(Objects.requireNonNull
            (WorkingWithAMessageListController.class.getResourceAsStream("/com/example/speech/image/delete.png")));
    private static final Image selectI = new Image(Objects.requireNonNull
            (WorkingWithAMessageListController.class.getResourceAsStream("/com/example/speech/image/select.png")));

    public void initializeShape(double xPos, double yPos, ListView<Message> messagesLV, StackPane messagesSP) {
        double vBoxWidth = 200;
        double vBoxHeight = 250;

        VBox rootVB = new VBox();
        rootVB.setMaxWidth(vBoxWidth);
        rootVB.setMaxHeight(vBoxHeight);
        rootVB.getStyleClass().add("working-with-a-message-root-pane");

        double stackPaneWidth = messagesSP.getWidth();
        double stackPaneHeight = messagesSP.getHeight();

        double finalX = xPos;
        if (xPos + vBoxWidth > stackPaneWidth) {
            finalX = xPos - vBoxWidth;
        }

        double finalY = yPos;
        if (yPos + vBoxHeight > stackPaneHeight) {
            finalY = yPos - vBoxHeight;
        }

        if (finalX < 0) {
            finalX = 0;
        }

        if (finalY < 0) {
            finalY = 0;
        }

        rootVB.setLayoutX(finalX);
        rootVB.setLayoutY(finalY);

        CustomButton reply = new CustomButton(replyI, "Ответить");
        reply.setPrefWidth(vBoxWidth);
        reply.setPrefHeight(40);
        VBox.setMargin(reply, new Insets(5, 0, 0, 0));

        CustomButton pin = new CustomButton(pinI, "Закрепить");
        pin.setPrefWidth(vBoxWidth);
        pin.setPrefHeight(40);

        CustomButton copy = new CustomButton(copyI, "Копировать текст");
        copy.setPrefWidth(vBoxWidth);
        copy.setPrefHeight(40);

        CustomButton forward = new CustomButton(forwardI, "Переслать");
        forward.setPrefWidth(vBoxWidth);
        forward.setPrefHeight(40);

        CustomButton delete = new CustomButton(deleteI, "Удалить");
        delete.setPrefWidth(vBoxWidth);
        delete.setPrefHeight(40);

        CustomButton select = new CustomButton(selectI, "Выделить");
        select.setPrefWidth(vBoxWidth);
        select.setPrefHeight(40);
        VBox.setMargin(select, new Insets(0, 0, 5, 0));

        rootVB.getChildren().addAll(reply, pin, copy, forward, delete, select);

        this.getChildren().add(rootVB);

        this.setLayoutX(0);
        this.setLayoutY(0);
    }
}