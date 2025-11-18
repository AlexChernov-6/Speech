package com.example.speech.control;

import com.example.speech.util.HelpfulInitializationClass;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import java.io.IOException;

import static com.example.speech.util.HelpfulInitializationClass.applyDimmingEffect;

public class LostPasswordController {

    @FXML
    private Label contentLb;

    @FXML
    private Button closeBtn;

    public void showModalLostPasswordStage(Stage mainStage, String otherParam) throws IOException {
        // Загружаем FXML и получаем контроллер
        FXMLLoader loader = new FXMLLoader(EntranceController.class.getResource(
                "/com/example/speech/shape/LostPasswordShape.fxml"
        ));
        Parent root = loader.load();
        LostPasswordController controller = loader.getController();
        controller.updateContentLabel(otherParam);
        HelpfulInitializationClass.showModalStage(mainStage, root);
    }

    //События для кнопки OK
    @FXML
    private void onOkBtn() {
        Stage stage = (Stage) closeBtn.getScene().getWindow();
        stage.close();
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