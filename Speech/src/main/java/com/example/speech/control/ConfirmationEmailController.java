package com.example.speech.control;

import com.example.speech.otherClass.AbstractModalStage;
import com.example.speech.util.HelpfulInitializationClass;
import com.example.speech.util.SendingClass;
import javafx.application.Platform;
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
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

import static com.example.speech.util.SendingClass.*;

public class ConfirmationEmailController extends AbstractModalStage {
    @FXML
    private Button closeBtn, getCodeBtn;

    @FXML
    private Label contentLb, informationLb;

    @FXML
    private TextField num1, num2, num3, num4, num5, num6;

    private Stage mainStage;
    private Stage modalStage;

    private String mail;

    private Timer countdownTimer;

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
        this.mail = mail;
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
        onGetCodeBtn();
    }

    @FXML
    private void onGetCodeBtn() {
        if (getCodeBtn.getText().equals("Получить код")) {
            // ПРОВЕРЯЕМ можно ли отправлять ДО отправки
            if (!SendingClass.canSendEmail(mail)) {
                int remaining = SendingClass.getRemainingTime(mail);
                startCountdown(remaining);
                return;
            }

            // ОТПРАВЛЯЕМ email
            boolean sent = SendingClass.sendPostalDelivery(mail, ContextDelivery.SEND_CONFIRMATION_CODE);

            if (sent) {
                informationLb.setText("Код отправлен на вашу почту");
                informationLb.setStyle("-fx-text-fill: green");
                startCountdown(60); // Запускаем таймер на 60 секунд
            } else {
                informationLb.setText("Ошибка отправки кода");
                informationLb.setStyle("-fx-text-fill: red");
            }
        }
    }

    private void startCountdown(int startSeconds) {
        // Отменяем предыдущий таймер
        if (countdownTimer != null) {
            countdownTimer.cancel();
        }

        getCodeBtn.setDisable(true);
        final int[] secondsRemaining = {startSeconds};

        countdownTimer = new Timer();
        countdownTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    secondsRemaining[0]--;
                    if (secondsRemaining[0] > 0) {
                        getCodeBtn.setText("Повторно через " + secondsRemaining[0]);
                    } else {
                        getCodeBtn.setText("Получить код");
                        getCodeBtn.setDisable(false);
                        countdownTimer.cancel();
                        countdownTimer = null;
                    }
                });
            }
        }, 1000, 1000);
    }
}
