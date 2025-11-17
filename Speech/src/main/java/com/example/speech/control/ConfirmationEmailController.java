package com.example.speech.control;

import com.example.speech.otherClass.AbstractModalStage;
import com.example.speech.util.HelpfulInitializationClass;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;

import java.io.IOException;

import static com.example.speech.util.SendingClass.*;

public class ConfirmationEmailController extends AbstractModalStage {
    @FXML
    private Button closeBtn;

    @FXML
    private Label contentLb;

    @FXML
    private TextField num1, num2, num3, num4, num5, num6;

    private Stage mainStage;
    private Stage modalStage;

    @FXML
    private void onCloseBtn() {
        Stage stage = (Stage) closeBtn.getScene().getWindow();
        //Закрываем окно
        stage.close();
    }

    @Override
    public void showModalStage(Stage mainStage, String otherParam) throws IOException {
        FXMLLoader loader = new FXMLLoader(EntranceController.class.getResource(
                "/com/example/speech/shape/ConfirmationEmailShape.fxml"));
        Parent parent = loader.load();

        ConfirmationEmailController controller = loader.getController();
        controller.updateContentLabel(otherParam);

        controller.mainStage = mainStage;
        controller.modalStage = HelpfulInitializationClass.showModalStage(mainStage, parent);
    }

    private void updateContentLabel(String mail) {
        if (contentLb != null && mail != null) {
            TextFlow textFlow = new TextFlow();

            Text before = new Text("Мы отправили код на адрес: ");
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
        sendPostalDelivery(mail, ContextDelivery.SEND_CONFIRMATION_CODE);
    }
}
