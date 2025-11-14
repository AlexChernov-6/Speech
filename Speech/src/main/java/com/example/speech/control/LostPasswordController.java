package com.example.speech.control;

import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.fxml.FXMLLoader;
import java.io.IOException;

import static com.example.speech.util.HelpfulInitializationClass.applyDimmingEffect;
import static com.example.speech.util.HelpfulInitializationClass.showModalStage;

public class LostPasswordController {

    @FXML
    private Label contentLb;

    private Stage mainStage;
    private Stage modalStage;

    public static void showLostPasswordStage(Stage mainStage, String email) throws IOException {
        // Загружаем FXML и получаем контроллер
        FXMLLoader loader = new FXMLLoader(EntranceController.class.getResource(
                "/com/example/speech/shape/LostPasswordShape.fxml"
        ));
        Parent root = loader.load();
        LostPasswordController controller = loader.getController();
        controller.updateContentLabel(email);
        controller.mainStage = mainStage;

        controller.modalStage = showModalStage(mainStage, root);
    }

    //События для кнопки OK
    @FXML
    private void onOkBtn() {
        applyDimmingEffect(false, mainStage);
        if (modalStage != null) {
            modalStage.close();
        }
    }

    //Метод, который обновляет текст у Label contentLb
    private void updateContentLabel(String mail) {
        if (contentLb != null && mail != null) {
            TextFlow textFlow = new TextFlow();

            Text before = new Text("Мы отправили инструкции по смене пароля на ");
            Text boldEmail = new Text(mail);
            Text after = new Text(", пожалуйста, проверьте папки «Входящие» и «Спам»");

            // Жирный email
            boldEmail.setStyle("-fx-font-weight: bold;");

            // Сохраняем стиль метки для обычного текста
            String labelStyle = contentLb.getStyle();
            before.setStyle(labelStyle);
            after.setStyle(labelStyle);

            textFlow.getChildren().addAll(before, boldEmail, after);

            contentLb.setGraphic(textFlow);
        }
    }
}