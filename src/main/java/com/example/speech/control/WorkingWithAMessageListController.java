package com.example.speech.control;

import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.Objects;

public class WorkingWithAMessageListController extends Pane {

    private static final Image replyI = new Image(Objects.requireNonNull
            (WorkingWithAMessageListController.class.getResourceAsStream("/com/example/speech/image/check.png")));
    private static final Image pinI = new Image(Objects.requireNonNull
            (WorkingWithAMessageListController.class.getResourceAsStream("/com/example/speech/image/check.png")));
    private static final Image copyI = new Image(Objects.requireNonNull
            (WorkingWithAMessageListController.class.getResourceAsStream("/com/example/speech/image/check.png")));
    private static final Image forwardI = new Image(Objects.requireNonNull
            (WorkingWithAMessageListController.class.getResourceAsStream("/com/example/speech/image/check.png")));
    private static final Image deleteI = new Image(Objects.requireNonNull
            (WorkingWithAMessageListController.class.getResourceAsStream("/com/example/speech/image/check.png")));
    private static final Image selectI = new Image(Objects.requireNonNull
            (WorkingWithAMessageListController.class.getResourceAsStream("/com/example/speech/image/check.png")));

    public void initializeShape(StackPane stackPane, double xPos, double yPos) {
        double vBoxWidth = 200;
        double vBoxHeight = 100;

        VBox rootVB = new VBox();
        rootVB.setMaxWidth(vBoxWidth);
        rootVB.setMaxHeight(vBoxHeight);
        rootVB.getStyleClass().add("working-with-a-message-root-pane");

        rootVB.setLayoutX(xPos);
        rootVB.setLayoutY(yPos);

        CustomButton reply = new CustomButton(replyI, "Ответить");
        reply.setPrefWidth(vBoxWidth);

        CustomButton pin = new CustomButton(pinI, "Закрепить");
        pin.setPrefWidth(vBoxWidth);

        CustomButton copy = new CustomButton(copyI, "Копировать текст");
        copy.setPrefWidth(vBoxWidth);

        CustomButton forward = new CustomButton(forwardI, "Переслать");
        forward.setPrefWidth(vBoxWidth);

        CustomButton delete = new CustomButton(deleteI, "Удалить");
        delete.setPrefWidth(vBoxWidth);

        CustomButton select = new CustomButton(selectI, "Выделить");
        select.setPrefWidth(vBoxWidth);

        rootVB.getChildren().addAll(reply, pin, copy, forward, delete, select);

        this.getChildren().add(rootVB);

        this.setLayoutX(0);
        this.setLayoutY(0);

        stackPane.getChildren().add(this);
    }
}
